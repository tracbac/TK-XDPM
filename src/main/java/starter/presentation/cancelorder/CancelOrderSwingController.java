package starter.presentation.cancelorder;

import starter.application.cancelorder.CancelOrderInputBoundary;
import starter.application.cancelorder.CancelOrderOutputBoundary;
import starter.application.cancelorder.CancelOrderRequest;
import starter.application.cancelorder.CancelOrderResponse;

public class CancelOrderSwingController {
    private final CancelOrderInputBoundary cancelOrder;
    private final CancelOrderOutputBoundary outputBoundary;

    public CancelOrderSwingController(
            CancelOrderInputBoundary cancelOrder,
            CancelOrderOutputBoundary outputBoundary) {
        this.cancelOrder = cancelOrder;
        this.outputBoundary = outputBoundary;
    }

    public void cancelOrder(String orderId, String reason) {
        String normalizedOrderId = orderId == null ? "" : orderId.trim();
        String normalizedReason = reason == null ? "" : reason.trim();
        if (normalizedOrderId.isEmpty() || normalizedReason.isEmpty()) {
            outputBoundary.present(new CancelOrderResponse(
                    false,
                    normalizedOrderId,
                    null,
                    null,
                    false,
                    false,
                    "Vui lòng nhập mã đơn hàng và lý do hủy."));
            return;
        }
        cancelOrder.execute(new CancelOrderRequest(normalizedOrderId, normalizedReason));
    }
}
