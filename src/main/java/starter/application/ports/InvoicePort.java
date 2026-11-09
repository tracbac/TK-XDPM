package starter.application.ports;

import java.math.BigDecimal;

public interface InvoicePort {
    void printPaperInvoice(String orderId, BigDecimal amount);

    void sendElectronicInvoice(String orderId, BigDecimal amount);

    void requestPaymentRetry(String orderId);
}
