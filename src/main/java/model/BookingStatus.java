package model;

/**
 * Enum định nghĩa trạng thái của đơn đặt vé
 * Lifecycle: PENDING -> CONFIRMED
 *            PENDING -> CANCELLED
 *            CONFIRMED -> CANCELLED (trong thời gian cho phép)
 */
public enum BookingStatus {
    PENDING("Chờ xác nhận", "Đơn đặt vé đang chờ thanh toán"),
    CONFIRMED("Đã xác nhận", "Đơn đặt vé đã được xác nhận và thanh toán"),
    CANCELLED("Đã hủy", "Đơn đặt vé đã bị hủy");

    private final String displayName;
    private final String description;

    /**
     * Constructor
     * @param displayName Tên hiển thị
     * @param description Mô tả trạng thái
     */
    BookingStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Lấy tên hiển thị
     * @return Tên trạng thái
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Lấy mô tả
     * @return Mô tả chi tiết trạng thái
     */
    public String getDescription() {
        return description;
    }

    /**
     * Kiểm tra đơn đang chờ xác nhận không
     * @return true nếu PENDING
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * Kiểm tra đơn đã được xác nhận chưa
     * @return true nếu CONFIRMED
     */
    public boolean isConfirmed() {
        return this == CONFIRMED;
    }

    /**
     * Kiểm tra đơn đã bị hủy chưa
     * @return true nếu CANCELLED
     */
    public boolean isCancelled() {
        return this == CANCELLED;
    }

    /**
     * Kiểm tra có thể hủy đơn không
     * @return true nếu có thể hủy (PENDING hoặc CONFIRMED)
     */
    public boolean canBeCancelled() {
        return this == PENDING || this == CONFIRMED;
    }

    /**
     * Kiểm tra có thể xác nhận đơn không
     * @return true nếu đang PENDING
     */
    public boolean canBeConfirmed() {
        return this == PENDING;
    }

    /**
     * Kiểm tra có thể chuyển sang trạng thái mới không
     * @param newStatus Trạng thái mới
     * @return true nếu chuyển đổi hợp lệ
     */
    public boolean canTransitionTo(BookingStatus newStatus) {
        switch (this) {
            case PENDING:
                // Từ PENDING có thể chuyển sang CONFIRMED hoặc CANCELLED
                return newStatus == CONFIRMED || newStatus == CANCELLED;

            case CONFIRMED:
                // Từ CONFIRMED chỉ có thể hủy
                return newStatus == CANCELLED;

            case CANCELLED:
                // Từ CANCELLED không thể chuyển sang trạng thái nào khác
                return false;

            default:
                return false;
        }
    }

    /**
     * Lấy màu sắc hiển thị cho UI (hex color)
     * @return Mã màu hex
     */
    public String getDisplayColor() {
        switch (this) {
            case PENDING:
                return "#FFA500"; // Orange
            case CONFIRMED:
                return "#4CAF50"; // Green
            case CANCELLED:
                return "#F44336"; // Red
            default:
                return "#000000"; // Black
        }
    }

    /**
     * Chuyển từ string sang enum (case-insensitive)
     * @param status Tên trạng thái
     * @return BookingStatus enum
     * @throws IllegalArgumentException nếu không tìm thấy
     */
    public static BookingStatus fromString(String status) {
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái booking không được null");
        }

        for (BookingStatus bookingStatus : BookingStatus.values()) {
            if (bookingStatus.name().equalsIgnoreCase(status)) {
                return bookingStatus;
            }
        }

        throw new IllegalArgumentException("Trạng thái booking không hợp lệ: " + status);
    }

    /**
     * Override toString để trả về tên có ý nghĩa
     */
    @Override
    public String toString() {
        return displayName;
    }
}