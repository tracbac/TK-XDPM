package starter.application.payment;

import starter.domain.PaymentMethod;
import starter.domain.PaymentTransactionStatus;

import java.math.BigDecimal;

public record PaymentResponse(
        PaymentTransactionStatus status,
        String orderId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        String message,
        String invoiceAction,
        String securityRule) {}
