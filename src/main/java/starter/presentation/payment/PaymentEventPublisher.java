package starter.presentation.payment;

import java.util.concurrent.CopyOnWriteArrayList;

public class PaymentEventPublisher {
    private final CopyOnWriteArrayList<PaymentObserver> observers = new CopyOnWriteArrayList<>();

    public void addObserver(PaymentObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(PaymentObserver observer) {
        observers.remove(observer);
    }

    public void publish(PaymentViewModel viewModel) {
        observers.forEach(observer -> observer.onPaymentUpdated(viewModel));
    }
}
