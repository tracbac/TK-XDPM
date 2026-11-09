package starter.stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import starter.application.payment.PaymentInteractor;
import starter.adapters.security.PaymentSecurityPolicyAdapter;
import starter.domain.Order;
import starter.domain.OrderStatus;
import starter.domain.PaymentMethod;
import starter.domain.PaymentTransactionStatus;
import starter.application.payment.PaymentSecurityPolicy;
import starter.presentation.payment.PaymentEventPublisher;
import starter.presentation.payment.PaymentSwingController;
import starter.presentation.payment.PaymentSwingPresenter;
import starter.presentation.payment.PaymentViewModel;
import starter.application.ports.AuditLogRepositoryPort;
import starter.application.ports.InvoicePort;
import starter.application.ports.OrderRepositoryPort;
import starter.application.ports.PaymentGatewayPort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentSteps {
    private static final String ORDER_ID = "PAY-TEST";

    private BigDecimal amount;
    private Order order;
    private PaymentMethod paymentMethod;
    private PaymentSwingController controller;
    private PaymentViewModel presentedModel;
    private PaymentTransactionStatus transactionStatus;
    private String invoiceAction;
    private String appliedSecurityRule;
    private String gatewayFailureMessage;
    private PaymentViewModel observedModel;
    private BigDecimal chargedAmount;
    private PaymentMethod chargedPaymentMethod;
    private boolean gatewayAcceptsPayment;
    private boolean gatewayCalled;
    private boolean securityPolicyApplied;
    private boolean auditLogged;
    private boolean paymentObserved;

    @Given("đơn hàng bánh kẹo có tổng giá trị là {int} VNĐ")
    public void don_hang_banh_keo_co_tong_gia_tri(int amountValue) {
        initializeScenario(amountValue, OrderStatus.PENDING);
    }

    @Given("khách hàng thực hiện thanh toán số tiền {int} VNĐ bằng phương thức {string}")
    public void khach_hang_thuc_hien_thanh_toan(
            int amountValue, String methodName) {
        initializeScenario(amountValue, OrderStatus.PENDING);
        paymentMethod = PaymentMethod.fromDisplayName(methodName);
    }

    @When("khách hàng chọn phương thức thanh toán là {string} và xác thực giao dịch")
    public void khach_hang_chon_phuong_thuc_va_xac_thuc(String methodName) {
        paymentMethod = PaymentMethod.fromDisplayName(methodName);
        gatewayAcceptsPayment = true;
        createInteractor();
        executePayment();
    }

    @When("khách hàng chọn hình thức thanh toán là {string} khi giao hàng")
    public void khach_hang_chon_thanh_toan_cod(String methodName) {
        paymentMethod = PaymentMethod.fromDisplayName(methodName);
        createInteractor();
        executePayment();
    }

    @When("cổng thanh toán phản hồi lỗi {string}")
    public void cong_thanh_toan_phan_hoi_loi(String failureMessage) {
        gatewayFailureMessage = failureMessage;
        createInteractor();
        executePayment();
    }

    @Then("kết quả giao dịch thanh toán trả về là {string}")
    public void ket_qua_giao_dich_thanh_toan(String expectedState) {
        assertNotNull(presentedModel, "Presenter phải tạo output cho UI");
        assertTrue(paymentObserved, "Observer phải nhận được kết quả thanh toán");
        assertEquals(presentedModel, observedModel, "Observer phải nhận đúng output model");
        if (expectedState.equals("Thành công")) {
            assertEquals(PaymentTransactionStatus.SUCCEEDED, transactionStatus);
        } else if (expectedState.equals("Đang chờ thu tiền")) {
            assertEquals(PaymentTransactionStatus.PENDING, transactionStatus);
        } else if (expectedState.equals("Thất bại")) {
            assertEquals(PaymentTransactionStatus.FAILED, transactionStatus);
            assertEquals(OrderStatus.PENDING, order.getStatus());
        } else {
            fail("Trạng thái giao dịch không hỗ trợ: " + expectedState);
        }
    }

    @Then("trạng thái giao dịch tài chính ban đầu là {string}")
    public void trang_thai_giao_dich_tai_chinh_ban_dau(String expectedState) {
        assertEquals("Đang chờ thu tiền", expectedState);
        assertEquals(PaymentTransactionStatus.PENDING, transactionStatus);
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Then("hệ thống hiển thị thông báo {string} và yêu cầu khách hàng thử lại")
    public void hien_thi_loi_va_yeu_cau_thu_lai(String expectedMessage) {
        assertEquals(expectedMessage, presentedModel.message());
        assertEquals("Yêu cầu thử lại", invoiceAction);
    }

    @And("hệ thống tiến hành kích hoạt tính năng {string}")
    public void he_thong_tien_hanh_kich_hoat_tinh_nang(String expectedAction) {
        assertEquals(expectedAction, invoiceAction);
    }

    @And("quy tắc bảo mật giao dịch tài chính [BR31] được áp dụng {string}")
    public void quy_tac_bao_mat_giao_dich_tai_chinh_duoc_ap_dung(String expectedRule) {
        if (expectedRule.equals("Không áp dụng")) {
            assertFalse(securityPolicyApplied, "Không áp dụng bảo mật khi dữ liệu chưa hợp lệ");
            assertNull(appliedSecurityRule);
        } else {
            assertTrue(securityPolicyApplied, "Quy tắc bảo mật phải được áp dụng");
            assertEquals(expectedRule, appliedSecurityRule);
        }
    }

    @And("đơn hàng được lưu ở trạng thái {string} và audit được ghi nhận")
    public void don_hang_duoc_luu_va_audit(String expectedStatus) {
        assertEquals(OrderStatus.valueOf(expectedStatus), order.getStatus());
        assertTrue(auditLogged, "Giao dịch phải được ghi audit");
    }

    @And("cổng thanh toán được gọi {string}")
    public void cong_thanh_toan_duoc_goi(String expectedCall) {
        assertEquals(expectedCall.equals("Có"), gatewayCalled);
        if (gatewayCalled) {
            assertEquals(paymentMethod, chargedPaymentMethod);
            assertEquals(amount, chargedAmount);
        }
    }

    private void initializeScenario(int amountValue, OrderStatus initialStatus) {
        amount = BigDecimal.valueOf(amountValue);
        order = new Order(ORDER_ID, initialStatus, new ArrayList<>());
        paymentMethod = null;
        controller = null;
        presentedModel = null;
        transactionStatus = null;
        invoiceAction = null;
        appliedSecurityRule = null;
        gatewayFailureMessage = null;
        observedModel = null;
        chargedAmount = null;
        chargedPaymentMethod = null;
        gatewayAcceptsPayment = false;
        gatewayCalled = false;
        securityPolicyApplied = false;
        auditLogged = false;
        paymentObserved = false;
    }

    private void createInteractor() {
        OrderRepositoryPort orderRepository = new OrderRepositoryPort() {
            @Override
            public Optional<Order> findById(String id) {
                return order != null && order.getId().equals(id)
                        ? Optional.of(order)
                        : Optional.empty();
            }

            @Override
            public void save(Order savedOrder) {
                order = savedOrder;
            }
        };

        PaymentGatewayPort paymentGateway = (id, value, method) -> {
            gatewayCalled = true;
            chargedAmount = value;
            chargedPaymentMethod = method;
            if (gatewayFailureMessage != null) {
                throw new IllegalStateException(gatewayFailureMessage);
            }
            return gatewayAcceptsPayment;
        };
        InvoicePort invoicePort = new InvoicePort() {
            @Override
            public void printPaperInvoice(String id, BigDecimal value) {
                invoiceAction = "In hóa đơn giấy";
            }

            @Override
            public void sendElectronicInvoice(String id, BigDecimal value) {
                invoiceAction = "Gửi hóa đơn điện tử";
            }

            @Override
            public void requestPaymentRetry(String id) {
                invoiceAction = "Yêu cầu thử lại";
            }
        };
        PaymentSecurityPolicy policyAdapter = new PaymentSecurityPolicyAdapter();
        PaymentSecurityPolicy securityPolicy = method -> {
            securityPolicyApplied = true;
            appliedSecurityRule = policyAdapter.resolveRule(method);
            return appliedSecurityRule;
        };
        AuditLogRepositoryPort auditRepository = (action, details) -> auditLogged = true;
        PaymentEventPublisher eventPublisher = new PaymentEventPublisher();
        PaymentSwingPresenter presenter = new PaymentSwingPresenter(
                viewModel -> presentedModel = viewModel, eventPublisher);
        eventPublisher.addObserver(viewModel -> {
            paymentObserved = true;
            observedModel = viewModel;
        });

        PaymentInteractor interactor = new PaymentInteractor(
                orderRepository,
                paymentGateway,
                auditRepository,
                invoicePort,
                securityPolicy,
                presenter);
        controller = new PaymentSwingController(interactor, presenter);
    }

    private void executePayment() {
        controller.submitPayment(ORDER_ID, amount.toPlainString(), paymentMethod);
        transactionStatus = presentedModel.status();
    }
}
