package starter.application.cancelorder;

public record CancelOrderRequest(String orderId, String reason) {}
