package starter.application.cancelorder;

import starter.application.ports.AuditLogRepositoryPort;
import starter.application.ports.InventoryRepositoryPort;
import starter.application.ports.OrderRepositoryPort;
import starter.domain.Order;
import starter.domain.OrderStatus;

public class CancelOrderUseCase implements CancelOrderInputBoundary {
    private final OrderRepositoryPort orderRepository;
    private final InventoryRepositoryPort inventoryRepository;
    private final AuditLogRepositoryPort auditLogRepository;
    private final CancelOrderOutputBoundary outputBoundary;

    public CancelOrderUseCase(
            OrderRepositoryPort orderRepository,
            InventoryRepositoryPort inventoryRepository,
            AuditLogRepositoryPort auditLogRepository,
            CancelOrderOutputBoundary outputBoundary) {
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
        this.auditLogRepository = auditLogRepository;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(CancelOrderRequest request) {
        Order order = null;
        OrderStatus previousStatus = null;
        boolean stockReleased = false;
        boolean auditLogged = false;
        CancelOrderResponse response;
        try {
            order = orderRepository.findById(request.orderId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy đơn hàng: " + request.orderId()));
            previousStatus = order.getStatus();
            order.cancel();
            inventoryRepository.releaseStock(order);
            stockReleased = true;
            orderRepository.save(order);
            auditLogRepository.logAction(
                    "CANCEL_ORDER",
                    "Đã hủy đơn hàng: " + request.orderId() + " với lý do: " + request.reason());
            auditLogged = true;
            response = new CancelOrderResponse(
                    true,
                    request.orderId(),
                    previousStatus,
                    OrderStatus.CANCELLED,
                    stockReleased,
                    auditLogged,
                    "Đã hủy đơn hàng thành công.");
        } catch (RuntimeException failure) {
            try {
                auditLogRepository.logAction(
                        "CANCEL_ORDER",
                        "Không thể hủy đơn hàng: " + request.orderId()
                                + " với lý do: " + request.reason()
                                + ". Nguyên nhân: " + failure.getMessage());
                auditLogged = true;
            } catch (RuntimeException auditFailure) {
                failure.addSuppressed(auditFailure);
            }
            response = new CancelOrderResponse(
                    false,
                    request.orderId(),
                    previousStatus,
                    order == null ? null : order.getStatus(),
                    stockReleased,
                    auditLogged,
                    describeFailure(failure));
        }
        outputBoundary.present(response);
    }

    private String describeFailure(RuntimeException failure) {
        String message = failure.getMessage() == null ? failure.toString() : failure.getMessage();
        for (Throwable suppressed : failure.getSuppressed()) {
            String suppressedMessage = suppressed.getMessage() == null
                    ? suppressed.toString()
                    : suppressed.getMessage();
            message += " | " + suppressedMessage;
        }
        return message;
    }
}
