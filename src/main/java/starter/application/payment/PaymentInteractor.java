package starter.application.payment;

import starter.domain.Order;
import starter.domain.PaymentMethod;
import starter.domain.PaymentTransactionStatus;
import starter.application.ports.AuditLogRepositoryPort;
import starter.application.ports.InvoicePort;
import starter.application.ports.OrderRepositoryPort;
import starter.application.ports.PaymentGatewayPort;

import java.math.BigDecimal;

public class PaymentInteractor implements PaymentInputBoundary {
    private final OrderRepositoryPort orderRepository;
    private final PaymentGatewayPort paymentGateway;
    private final AuditLogRepositoryPort auditLogRepository;
    private final InvoicePort invoicePort;
    private final PaymentSecurityPolicy securityPolicy;
    private final PaymentOutputBoundary outputBoundary;

    public PaymentInteractor(OrderRepositoryPort orderRepository,
                            PaymentGatewayPort paymentGateway,
                            AuditLogRepositoryPort auditLogRepository,
                            InvoicePort invoicePort,
                            PaymentSecurityPolicy securityPolicy,
                            PaymentOutputBoundary outputBoundary) {
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
        this.auditLogRepository = auditLogRepository;
        this.invoicePort = invoicePort;
        this.securityPolicy = securityPolicy;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(PaymentRequest request) {
        String orderId = request.orderId();
        BigDecimal amount = request.amount();
        PaymentMethod paymentMethod = request.paymentMethod();
        String appliedSecurityRule = null;
        String invoiceAction = null;
        String failureMessage = null;
        boolean cashOnDelivery = false;
        try {
            if (amount == null || amount.signum() <= 0) {
                throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0.");
            }
            if (paymentMethod == null) {
                throw new IllegalArgumentException("Phương thức thanh toán không được để trống.");
            }

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy đơn hàng: " + orderId));
            order.ensureCanBePaid();

            appliedSecurityRule = securityPolicy.resolveRule(paymentMethod);
            if (paymentMethod == PaymentMethod.CASH_ON_DELIVERY) {
                cashOnDelivery = true;
                invoicePort.printPaperInvoice(orderId, amount);
                invoiceAction = "In hóa đơn giấy";
                auditLogRepository.logAction(
                        "PAY_ORDER",
                        "Thanh toán COD đang chờ thu tiền cho đơn hàng: " + orderId
                                + ", số tiền: " + amount
                                + ", quy tắc bảo mật: " + appliedSecurityRule);
            } else {
                if (!paymentGateway.charge(orderId, amount, paymentMethod)) {
                    throw new IllegalStateException("Cổng thanh toán từ chối giao dịch.");
                }
                order.markPaid();
                orderRepository.save(order);
            }
        } catch (RuntimeException failure) {
            try {
                invoicePort.requestPaymentRetry(orderId);
                invoiceAction = "Yêu cầu thử lại";
            } catch (RuntimeException invoiceFailure) {
                failure.addSuppressed(invoiceFailure);
            }
            try {
                auditLogRepository.logAction(
                        "PAY_ORDER",
                        "Thanh toán thất bại cho đơn hàng: " + orderId
                                + ". Nguyên nhân: " + failure.getMessage());
            } catch (RuntimeException auditFailure) {
                failure.addSuppressed(auditFailure);
            }
            failureMessage = describeFailure(failure);
        }

        PaymentResponse response;
        if (failureMessage != null) {
            response = new PaymentResponse(
                    PaymentTransactionStatus.FAILED,
                    orderId,
                    amount,
                    paymentMethod,
                    failureMessage,
                    invoiceAction,
                    appliedSecurityRule);
        } else if (cashOnDelivery) {
            response = new PaymentResponse(
                    PaymentTransactionStatus.PENDING,
                    orderId,
                    amount,
                    paymentMethod,
                    "Đang chờ thu tiền",
                    invoiceAction,
                    appliedSecurityRule);
        } else {
            invoicePort.sendElectronicInvoice(orderId, amount);
            invoiceAction = "Gửi hóa đơn điện tử";
            auditLogRepository.logAction(
                    "PAY_ORDER",
                    "Thanh toán thành công cho đơn hàng: " + orderId + ", số tiền: " + amount
                            + ", phương thức: " + paymentMethod.getDisplayName()
                            + ", quy tắc bảo mật: " + appliedSecurityRule);
            response = new PaymentResponse(
                    PaymentTransactionStatus.SUCCEEDED,
                    orderId,
                    amount,
                    paymentMethod,
                    "Thanh toán thành công",
                    invoiceAction,
                    appliedSecurityRule);
        }
        outputBoundary.present(response);
    }

    private String describeFailure(RuntimeException failure) {
        String message = failure.getMessage() == null ? failure.toString() : failure.getMessage();
        for (Throwable suppressed : failure.getSuppressed()) {
            message += " | " + suppressed.getMessage();
        }
        return message;
    }
}
