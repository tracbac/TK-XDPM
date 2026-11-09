package starter.application.cancelorder;

import starter.domain.OrderStatus;

public record CancelOrderResponse(
        boolean successful,
        String orderId,
        OrderStatus previousStatus,
        OrderStatus orderStatus,
        boolean stockReleased,
        boolean auditLogged,
        String message) {}
