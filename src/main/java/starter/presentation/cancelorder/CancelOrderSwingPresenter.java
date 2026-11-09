package starter.presentation.cancelorder;

import starter.application.cancelorder.CancelOrderOutputBoundary;
import starter.application.cancelorder.CancelOrderResponse;

public class CancelOrderSwingPresenter implements CancelOrderOutputBoundary {
    private final CancelOrderView view;
    private final CancelOrderEventPublisher eventPublisher;

    public CancelOrderSwingPresenter(
            CancelOrderView view, CancelOrderEventPublisher eventPublisher) {
        this.view = view;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void present(CancelOrderResponse response) {
        CancelOrderViewModel viewModel = new CancelOrderViewModel(
                response.successful(),
                response.orderId(),
                response.previousStatus(),
                response.orderStatus(),
                response.stockReleased(),
                response.auditLogged(),
                response.message());
        view.display(viewModel);
        eventPublisher.publish(viewModel);
    }
}
