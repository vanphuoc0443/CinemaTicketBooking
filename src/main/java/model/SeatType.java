package model;

public enum SeatType {
    STANDARD(50000, "Ghế Thường"),
    VIP(100000, "Ghế VIP"),
    COUPLE(150000, "Ghế Đôi");

    private final double price;
    private final String displayName;

    /**
     * Constructor
     * @param price Giá ghế (VNĐ)
     * @param displayName Tên hiển thị
     */
    SeatType(double price, String displayName) {
        this.price = price;
        this.displayName = displayName;
    }

    /**
     * Lấy giá ghế
     * @return Giá ghế theo VNĐ
     */
    public double getPrice() {
        return price;
    }

    /**
     * Lấy tên hiển thị
     * @return Tên loại ghế
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Lấy giá đã format
     * @return Giá dạng string có format (ví dụ: "50,000 VNĐ")
     */
    public String getFormattedPrice() {
        return String.format("%,.0f VNĐ", price);
    }

    /**
     * Chuyển từ string sang enum (case-insensitive)
     * @param type Tên loại ghế
     * @return SeatType enum
     * @throws IllegalArgumentException nếu không tìm thấy
     */
    public static SeatType fromString(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Loại ghế không được null");
        }

        for (SeatType seatType : SeatType.values()) {
            if (seatType.name().equalsIgnoreCase(type)) {
                return seatType;
            }
        }

        throw new IllegalArgumentException("Loại ghế không hợp lệ: " + type);
    }

    /**
     * Override toString để trả về tên có ý nghĩa
     */
    @Override
    public String toString() {
        return displayName + " - " + getFormattedPrice();
    }
}
