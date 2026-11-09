package starter.application.ports;

import starter.domain.PaymentMethod;

import java.math.BigDecimal;

public interface PaymentGatewayPort {
    boolean charge(String orderId, BigDecimal amount, PaymentMethod paymentMethod);
}
