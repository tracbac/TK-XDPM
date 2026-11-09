package starter.application.payment;

import starter.domain.PaymentMethod;

import java.math.BigDecimal;

public record PaymentRequest(String orderId, BigDecimal amount, PaymentMethod paymentMethod) {}
