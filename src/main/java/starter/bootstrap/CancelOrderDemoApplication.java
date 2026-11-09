package starter.bootstrap;

import starter.application.cancelorder.CancelOrderUseCase;
import starter.adapters.demo.ConsoleAuditLogAdapter;
import starter.adapters.demo.ConsoleInventoryAdapter;
import starter.adapters.demo.DemoOrderRepository;
import starter.presentation.cancelorder.CancelOrderEventPublisher;
import starter.presentation.cancelorder.CancelOrderSwingController;
import starter.presentation.cancelorder.CancelOrderSwingPresenter;
import starter.presentation.cancelorder.CancelOrderSwingView;

import javax.swing.*;

public final class CancelOrderDemoApplication {
    private CancelOrderDemoApplication() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CancelOrderEventPublisher events = new CancelOrderEventPublisher();
            events.addObserver(viewModel -> System.out.printf(
                    "Cancellation event: %s (%s)%n",
                    viewModel.orderId(),
                    viewModel.successful() ? "success" : "rejected"));
            CancelOrderSwingView view = new CancelOrderSwingView();
            CancelOrderSwingPresenter presenter = new CancelOrderSwingPresenter(view, events);
            CancelOrderUseCase useCase =
                    new CancelOrderUseCase(
                            new DemoOrderRepository(),
                            new ConsoleInventoryAdapter(),
                            new ConsoleAuditLogAdapter(),
                            presenter);
            view.setController(new CancelOrderSwingController(useCase, presenter));
            view.setVisible(true);
        });
    }
}
