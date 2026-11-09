package starter.presentation.payment;

@FunctionalInterface
public interface PaymentObserver {
    void onPaymentUpdated(PaymentViewModel viewModel);
}
