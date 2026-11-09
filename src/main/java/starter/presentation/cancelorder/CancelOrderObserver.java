package starter.presentation.cancelorder;

@FunctionalInterface
public interface CancelOrderObserver {
    void onCancellationUpdated(CancelOrderViewModel viewModel);
}
