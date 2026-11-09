package starter.presentation.payment;

import starter.domain.PaymentMethod;
import starter.domain.PaymentTransactionStatus;

import java.math.BigDecimal;

public record PaymentViewModel(
        PaymentTransactionStatus status,
        String orderId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        String message,
        String invoiceAction,
        String securityRule) {}
