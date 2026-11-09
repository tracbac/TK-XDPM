package starter.presentation.payment;

import starter.application.payment.PaymentOutputBoundary;
import starter.application.payment.PaymentResponse;

public class PaymentSwingPresenter implements PaymentOutputBoundary {
    private final PaymentView view;
    private final PaymentEventPublisher eventPublisher;

    public PaymentSwingPresenter(PaymentView view, PaymentEventPublisher eventPublisher) {
        this.view = view;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void present(PaymentResponse response) {
        PaymentViewModel viewModel = new PaymentViewModel(
                response.status(),
                response.orderId(),
                response.amount(),
                response.paymentMethod(),
                response.message(),
                response.invoiceAction(),
                response.securityRule());
        view.display(viewModel);
        eventPublisher.publish(viewModel);
    }
}
