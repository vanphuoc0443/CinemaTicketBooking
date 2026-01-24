package model;

public enum PaymentStatus {
    PENDING("Đang chờ"),
    COMPLETED("Hoàn thành"),
    FAILED("Thất bại"),
    REFUNDED("Đã hoàn tiền");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isFailed() {
        return this == FAILED;
    }

    public boolean isRefunded() {
        return this == REFUNDED;
    }

    public static PaymentStatus fromString(String status) {
        if (status == null) {
            throw new IllegalArgumentException("Payment status không được null");
        }

        for (PaymentStatus ps : PaymentStatus.values()) {
            if (ps.name().equalsIgnoreCase(status)) {
                return ps;
            }
        }

        throw new IllegalArgumentException("Payment status không hợp lệ: " + status);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
