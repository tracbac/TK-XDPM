package starter.adapters.demo;

import starter.application.ports.PaymentGatewayPort;
import starter.domain.PaymentMethod;

import java.math.BigDecimal;

public class SimulatedPaymentGatewayAdapter implements PaymentGatewayPort {
    @Override
    public boolean charge(String orderId, BigDecimal amount, PaymentMethod paymentMethod) {
        System.out.printf(
                "DEMO gateway called for %s (%s, %s VNĐ)%n",
                orderId,
                paymentMethod.getDisplayName(),
                amount);
        return !orderId.equals("PAY-FAIL");
    }
}
