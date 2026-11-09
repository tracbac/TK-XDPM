package starter.presentation.cancelorder;

import java.util.concurrent.CopyOnWriteArrayList;

public class CancelOrderEventPublisher {
    private final CopyOnWriteArrayList<CancelOrderObserver> observers =
            new CopyOnWriteArrayList<>();

    public void addObserver(CancelOrderObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(CancelOrderObserver observer) {
        observers.remove(observer);
    }

    public void publish(CancelOrderViewModel viewModel) {
        observers.forEach(observer -> observer.onCancellationUpdated(viewModel));
    }
}
