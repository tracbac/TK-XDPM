package starter.presentation.payment;

import starter.application.payment.PaymentInputBoundary;
import starter.application.payment.PaymentOutputBoundary;
import starter.application.payment.PaymentRequest;
import starter.application.payment.PaymentResponse;
import starter.domain.PaymentTransactionStatus;
import starter.domain.PaymentMethod;

import java.math.BigDecimal;

public class PaymentSwingController {
    private final PaymentInputBoundary paymentUseCase;
    private final PaymentOutputBoundary outputBoundary;

    public PaymentSwingController(
            PaymentInputBoundary paymentUseCase, PaymentOutputBoundary outputBoundary) {
        this.paymentUseCase = paymentUseCase;
        this.outputBoundary = outputBoundary;
    }

    public void submitPayment(String orderId, String amountText, PaymentMethod paymentMethod) {
        try {
            BigDecimal amount = new BigDecimal(amountText.trim());
            paymentUseCase.execute(new PaymentRequest(orderId.trim(), amount, paymentMethod));
        } catch (NumberFormatException failure) {
            outputBoundary.present(new PaymentResponse(
                    PaymentTransactionStatus.FAILED,
                    orderId,
                    null,
                    paymentMethod,
                    "Số tiền thanh toán không hợp lệ.",
                    null,
                    null));
        }
    }
}
