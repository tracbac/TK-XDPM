package starter.application.payment;

import starter.domain.PaymentMethod;

public interface PaymentSecurityPolicy {
    String resolveRule(PaymentMethod paymentMethod);
}
