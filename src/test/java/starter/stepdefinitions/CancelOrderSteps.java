package starter.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.*;

import starter.application.cancelorder.CancelOrderUseCase;
import starter.domain.*;
import starter.application.ports.AuditLogRepositoryPort;
import starter.application.ports.InventoryRepositoryPort;
import starter.application.ports.OrderRepositoryPort;
import starter.presentation.cancelorder.CancelOrderEventPublisher;
import starter.presentation.cancelorder.CancelOrderSwingController;
import starter.presentation.cancelorder.CancelOrderSwingPresenter;
import starter.presentation.cancelorder.CancelOrderViewModel;

import java.util.ArrayList;
import java.util.Optional;

public class CancelOrderSteps {

    private String orderId;
    private Order order;
    private CancelOrderSwingController controller;
    private CancelOrderViewModel presentedModel;
    private CancelOrderViewModel observedModel;
    private boolean inventoryReleased = false;
    private boolean auditLogged = false;
    private boolean cancellationObserved;

    @Given("khách hàng chọn đơn hàng bánh kẹo mã {string} hiện đang ở trạng thái {string}")
    public void khach_hang_chon_don_hang_hien_dang_o_trang_thai(String orderId, String statusStr) {
        this.orderId = orderId;
        presentedModel = null;
        observedModel = null;
        inventoryReleased = false;
        auditLogged = false;
        cancellationObserved = false;

        OrderStatus status = OrderStatus.valueOf(statusStr);
        order = status == OrderStatus.NOT_FOUND
                ? null
                : new Order(orderId, status, new ArrayList<>());

        OrderRepositoryPort orderRepo = new OrderRepositoryPort() {
            @Override
            public Optional<Order> findById(String id) {
                return order != null && order.getId().equals(id)
                        ? Optional.of(order)
                        : Optional.empty();
            }
            @Override
            public void save(Order o) { order = o; }
        };

        InventoryRepositoryPort inventoryRepo = o -> inventoryReleased = true;
        AuditLogRepositoryPort auditRepo = (action, details) -> auditLogged = true;

        CancelOrderEventPublisher eventPublisher = new CancelOrderEventPublisher();
        CancelOrderSwingPresenter presenter = new CancelOrderSwingPresenter(
                viewModel -> presentedModel = viewModel, eventPublisher);
        eventPublisher.addObserver(viewModel -> {
            cancellationObserved = true;
            observedModel = viewModel;
        });
        CancelOrderUseCase useCase =
                new CancelOrderUseCase(orderRepo, inventoryRepo, auditRepo, presenter);
        controller = new CancelOrderSwingController(useCase, presenter);
    }

    @When("hệ thống nhận yêu cầu hủy đơn với lý do là {string}")
    public void he_thong_nhan_yeu_cau_huy_don_voi_ly_do_la(String reason) {
        controller.cancelOrder(orderId, reason);
    }

    @Then("kết quả xử lý trả về phải là {string}")
    public void ket_qua_xu_ly_tra_ve_phai_la(String expectedResult) {
        assertNotNull(presentedModel, "Presenter phải tạo output cho UI");
        assertTrue(cancellationObserved, "Observer phải nhận được kết quả hủy đơn");
        assertEquals(presentedModel, observedModel, "Observer phải nhận đúng output model");
        if (expectedResult.contains("Thành công")) {
            assertTrue(presentedModel.successful());
        } else if (expectedResult.contains("Từ chối") || expectedResult.contains("Thất bại")) {
            assertFalse(presentedModel.successful());
        } else {
            fail("Kết quả mong đợi không được hỗ trợ: " + expectedResult);
        }
    }

    @Then("trạng thái mới của đơn hàng trong hệ thống sẽ là {string}")
    public void trang_thai_moi_cua_don_hang_trong_he_thong_se_la(String expectedStatus) {
        if (expectedStatus.equals("NOT_FOUND")) {
            assertNull(order, "Đơn hàng không tồn tại");
            return;
        }

        assertNotNull(order, "Đơn hàng phải tồn tại");
        assertEquals(OrderStatus.valueOf(expectedStatus), order.getStatus());
    }

    @Then("quy tắc hoàn trả tồn kho bánh kẹo [BR17] được kích hoạt {string}")
    public void quy_tac_hoan_tra_ton_kho_banh_keo_br17_duoc_kich_hoat(String action) {
        if (action.equals("Hoàn trả kho")) {
            assertTrue(inventoryReleased, "Kho phải được giải phóng số lượng bánh");
        } else {
            assertFalse(inventoryReleased, "Kho không được thay đổi nếu hủy thất bại");
        }
    }

    @Then("hệ thống ghi nhận nhật ký thao tác hủy đơn [BR12]")
    public void he_thong_ghi_nhan_nhat_ky_thao_tac_huy_don_br12() {
        assertTrue(auditLogged, "Phải ghi lại lịch sử Audit Log");
    }

    @Then("thông báo xử lý hủy đơn là {string}")
    public void thong_bao_xu_ly_huy_don_la(String expectedMessage) {
        if (expectedMessage.equals("Không có lỗi")) {
            assertTrue(presentedModel.successful(), "Yêu cầu hợp lệ phải thành công");
            return;
        }

        assertFalse(presentedModel.successful(), "Yêu cầu bị từ chối phải báo lỗi");
        assertEquals(expectedMessage, presentedModel.message());
    }
}