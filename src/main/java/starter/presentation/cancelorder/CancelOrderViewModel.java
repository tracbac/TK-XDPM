package starter.presentation.cancelorder;

import starter.domain.OrderStatus;

public record CancelOrderViewModel(
        boolean successful,
        String orderId,
        OrderStatus previousStatus,
        OrderStatus orderStatus,
        boolean stockReleased,
        boolean auditLogged,
        String message) {}
