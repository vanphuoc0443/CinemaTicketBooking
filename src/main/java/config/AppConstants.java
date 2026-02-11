package config;

/**
 * Class chứa tất cả các constants của application
 * Giúp tránh magic numbers và dễ dàng maintain
 */
public final class AppConstants {

    // Private constructor để prevent instantiation
    private AppConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ============== BOOKING CONSTANTS ==============

    /**
     * Số ghế tối đa có thể đặt trong một booking
     */
    public static final int MAX_SEATS_PER_BOOKING = 10;

    /**
     * Số giờ tối thiểu trước suất chiếu mới được phép hủy vé
     */
    public static final int CANCELLATION_HOURS_LIMIT = 2;

    /**
     * Thời gian lock ghế (phút) khi khách hàng đang chọn ghế
     */
    public static final int SEAT_LOCK_DURATION_MINUTES = 10;

    // ============== RETRY CONSTANTS ==============

    /**
     * Số lần thử lại tối đa khi gặp lỗi database
     */
    public static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * Thời gian delay giữa các lần retry (milliseconds)
     */
    public static final long RETRY_DELAY_MS = 100;

    /**
     * Exponential backoff multiplier
     */
    public static final double RETRY_BACKOFF_MULTIPLIER = 2.0;

    // ============== DATABASE CONSTANTS ==============

    /**
     * Connection pool size tối đa
     */
    public static final int MAX_POOL_SIZE = 20;

    /**
     * Connection pool size tối thiểu
     */
    public static final int MIN_POOL_SIZE = 5;

    /**
     * Connection timeout (milliseconds)
     */
    public static final int CONNECTION_TIMEOUT_MS = 10000;

    /**
     * Leak detection threshold (milliseconds)
     */
    public static final int LEAK_DETECTION_THRESHOLD_MS = 60000;

    // ============== VALIDATION CONSTANTS ==============

    /**
     * Password minimum length
     */
    public static final int PASSWORD_MIN_LENGTH = 8;

    /**
     * Password maximum length
     */
    public static final int PASSWORD_MAX_LENGTH = 100;

    /**
     * Username minimum length
     */
    public static final int USERNAME_MIN_LENGTH = 3;

    /**
     * Username maximum length
     */
    public static final int USERNAME_MAX_LENGTH = 50;

    /**
     * Email maximum length
     */
    public static final int EMAIL_MAX_LENGTH = 255;

    /**
     * Phone number pattern (Vietnam)
     */
    public static final String PHONE_PATTERN = "^(\\+84|0)[0-9]{9,10}$";

    /**
     * Email pattern
     */
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";

    // ============== PAYMENT CONSTANTS ==============

    /**
     * Payment timeout (seconds)
     */
    public static final int PAYMENT_TIMEOUT_SECONDS = 300;

    /**
     * Minimum payment amount
     */
    public static final double MIN_PAYMENT_AMOUNT = 1000.0;

    /**
     * Maximum payment amount
     */
    public static final double MAX_PAYMENT_AMOUNT = 10000000.0;

    // ============== BUSINESS RULE CONSTANTS ==============

    /**
     * Số ngày trước có thể đặt vé trước
     */
    public static final int MAX_ADVANCE_BOOKING_DAYS = 30;

    /**
     * Discount percentage cho VIP customers
     */
    public static final double VIP_DISCOUNT_PERCENTAGE = 10.0;

    /**
     * Point reward rate (points per 1000 VND spent)
     */
    public static final int POINTS_PER_1000_VND = 1;

    // ============== ERROR MESSAGES ==============

    public static final String ERR_INVALID_CUSTOMER_ID = "ID khách hàng không hợp lệ";
    public static final String ERR_INVALID_SHOWTIME_ID = "ID suất chiếu không hợp lệ";
    public static final String ERR_NO_SEATS_SELECTED = "Phải chọn ít nhất một ghế";
    public static final String ERR_TOO_MANY_SEATS = "Không thể đặt quá " + MAX_SEATS_PER_BOOKING + " ghế cùng lúc";
    public static final String ERR_DUPLICATE_SEATS = "Danh sách ghế có ID trùng lặp";
    public static final String ERR_SEAT_NOT_AVAILABLE = "Ghế đã được đặt";
    public static final String ERR_BOOKING_NOT_FOUND = "Không tìm thấy đơn đặt vé";
    public static final String ERR_CANCELLATION_TOO_LATE = "Không thể hủy vé trong vòng " + CANCELLATION_HOURS_LIMIT + " giờ trước suất chiếu";
    public static final String ERR_ALREADY_CANCELLED = "Đơn đặt vé đã được hủy trước đó";

    // ============== SUCCESS MESSAGES ==============

    public static final String MSG_BOOKING_CREATED = "Đặt vé thành công";
    public static final String MSG_BOOKING_CONFIRMED = "Xác nhận đặt vé thành công";
    public static final String MSG_BOOKING_CANCELLED = "Hủy vé thành công";
    public static final String MSG_PAYMENT_SUCCESS = "Thanh toán thành công";

    // ============== DATE/TIME FORMATS ==============

    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
}