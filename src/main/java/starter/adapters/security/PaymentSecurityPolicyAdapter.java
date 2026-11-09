package starter.adapters.security;

import starter.domain.PaymentMethod;
import starter.application.payment.PaymentSecurityPolicy;

public class PaymentSecurityPolicyAdapter implements PaymentSecurityPolicy {
    @Override
    public String resolveRule(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Phương thức thanh toán không được để trống.");
        }

        return switch (paymentMethod) {
            case CASH_ON_DELIVERY -> "Xác nhận biên lai tại chỗ";
            case MOMO -> "Mã hóa token giao dịch";
            case BANK_TRANSFER -> "Xác thực 3D Secure";
            case QR_TRANSFER -> "Kiểm tra lỗi cổng thanh toán";
        };
    }
}
