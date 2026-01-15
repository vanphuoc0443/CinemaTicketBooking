package model;

/**
 * Enum định nghĩa trạng thái của ghế
 * Lifecycle: AVAILABLE -> RESERVED -> BOOKED
 *            AVAILABLE <- RESERVED (có thể hủy đặt tạm)
 *            AVAILABLE <- BOOKED (khi hủy vé)
 */
public enum SeatStatus {
    AVAILABLE("Còn trống", "Ghế có thể đặt"),
    RESERVED("Đang giữ", "Ghế đang được giữ tạm thời"),
    BOOKED("Đã đặt", "Ghế đã được đặt và thanh toán");

    private final String displayName;
    private final String description;

    /**
     * Constructor
     * @param displayName Tên hiển thị
     * @param description Mô tả trạng thái
     */
    SeatStatus(String displayName, String description) {
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
     * Kiểm tra ghế có thể đặt không
     * @return true nếu ghế AVAILABLE
     */
    public boolean isAvailable() {
        return this == AVAILABLE;
    }

    /**
     * Kiểm tra ghế đang được giữ tạm không
     * @return true nếu ghế RESERVED
     */
    public boolean isReserved() {
        return this == RESERVED;
    }

    /**
     * Kiểm tra ghế đã được đặt chưa
     * @return true nếu ghế BOOKED
     */
    public boolean isBooked() {
        return this == BOOKED;
    }

    /**
     * Kiểm tra có thể chuyển sang trạng thái mới không
     * @param newStatus Trạng thái mới
     * @return true nếu chuyển đổi hợp lệ
     */
    public boolean canTransitionTo(SeatStatus newStatus) {
        switch (this) {
            case AVAILABLE:
                // Từ AVAILABLE có thể chuyển sang RESERVED hoặc BOOKED
                return newStatus == RESERVED || newStatus == BOOKED;

            case RESERVED:
                // Từ RESERVED có thể chuyển sang BOOKED hoặc quay lại AVAILABLE
                return newStatus == BOOKED || newStatus == AVAILABLE;

            case BOOKED:
                // Từ BOOKED chỉ có thể hủy về AVAILABLE
                return newStatus == AVAILABLE;

            default:
                return false;
        }
    }

    /**
     * Chuyển từ string sang enum (case-insensitive)
     * @param status Tên trạng thái
     * @return SeatStatus enum
     * @throws IllegalArgumentException nếu không tìm thấy
     */
    public static SeatStatus fromString(String status) {
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái ghế không được null");
        }

        for (SeatStatus seatStatus : SeatStatus.values()) {
            if (seatStatus.name().equalsIgnoreCase(status)) {
                return seatStatus;
            }
        }

        throw new IllegalArgumentException("Trạng thái ghế không hợp lệ: " + status);
    }

    @Override
    public String toString() {
        return displayName;
    }
}