package starter.bootstrap;

import starter.adapters.demo.ConsoleAuditLogAdapter;
import starter.adapters.demo.ConsoleInvoiceAdapter;
import starter.adapters.demo.DemoOrderRepository;
import starter.adapters.demo.SimulatedPaymentGatewayAdapter;
import starter.adapters.security.PaymentSecurityPolicyAdapter;
import starter.application.payment.PaymentInteractor;
import starter.presentation.payment.PaymentEventPublisher;
import starter.presentation.payment.PaymentSwingController;
import starter.presentation.payment.PaymentSwingPresenter;
import starter.presentation.payment.PaymentSwingView;

import javax.swing.*;

public final class PaymentDemoApplication {
    private PaymentDemoApplication() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PaymentEventPublisher eventPublisher = new PaymentEventPublisher();
            eventPublisher.addObserver(
                    result -> System.out.println("Payment event: " + result.status()));
            PaymentSwingView view = new PaymentSwingView();
            PaymentSwingPresenter presenter = new PaymentSwingPresenter(view, eventPublisher);
            PaymentInteractor interactor = createDemoInteractor(presenter);
            PaymentSwingController controller = new PaymentSwingController(interactor, presenter);
            view.setController(controller);
            view.setVisible(true);
        });
    }

    private static PaymentInteractor createDemoInteractor(PaymentSwingPresenter presenter) {
        return new PaymentInteractor(
                new DemoOrderRepository(),
                new SimulatedPaymentGatewayAdapter(),
                new ConsoleAuditLogAdapter(),
                new ConsoleInvoiceAdapter(),
                new PaymentSecurityPolicyAdapter(),
                presenter);
    }
}
