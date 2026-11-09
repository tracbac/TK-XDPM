package starter.domain;

public enum PaymentMethod {
    CASH_ON_DELIVERY("Tiền mặt (COD)"),
    MOMO("Ví MoMo"),
    BANK_TRANSFER("Thẻ Ngân hàng"),
    QR_TRANSFER("Chuyển khoản QR");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PaymentMethod fromDisplayName(String displayName) {
        for (PaymentMethod method : values()) {
            if (method.displayName.equals(displayName)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Phương thức thanh toán không được hỗ trợ: " + displayName);
    }
}
