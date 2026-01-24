package model;

public enum PaymentMethod {
    CASH("Tiền mặt"),
    CREDIT_CARD("Thẻ tín dụng"),
    DEBIT_CARD("Thẻ ghi nợ"),
    E_WALLET("Ví điện tử"),
    BANK_TRANSFER("Chuyển khoản");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PaymentMethod fromString(String method) {
        if (method == null) {
            throw new IllegalArgumentException("Payment method không được null");
        }

        for (PaymentMethod pm : PaymentMethod.values()) {
            if (pm.name().equalsIgnoreCase(method)) {
                return pm;
            }
        }

        throw new IllegalArgumentException("Payment method không hợp lệ: " + method);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
