package starter.adapters.demo;

import starter.application.ports.InvoicePort;

import java.math.BigDecimal;

public class ConsoleInvoiceAdapter implements InvoicePort {
    @Override
    public void printPaperInvoice(String orderId, BigDecimal amount) {
        System.out.printf("DEMO paper invoice: %s, %s VNĐ%n", orderId, amount);
    }

    @Override
    public void sendElectronicInvoice(String orderId, BigDecimal amount) {
        System.out.printf("DEMO electronic invoice: %s, %s VNĐ%n", orderId, amount);
    }

    @Override
    public void requestPaymentRetry(String orderId) {
        System.out.printf("DEMO retry requested: %s%n", orderId);
    }
}
