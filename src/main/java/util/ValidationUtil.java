package util;

import config.AppConstants;
import exception.ValidationException;

import java.util.regex.Pattern;

/**
 * Utility class cho input validation
 * Giúp đảm bảo data integrity và security
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(AppConstants.EMAIL_PATTERN);
    private static final Pattern PHONE_PATTERN = Pattern.compile(AppConstants.PHONE_PATTERN);

    private ValidationUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ============== STRING VALIDATION ==============

    /**
     * Kiểm tra string không null và không rỗng
     */
    public static void requireNonEmpty(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " không được để trống");
        }
    }

    /**
     * Kiểm tra độ dài string
     */
    public static void requireLength(String value, String fieldName, int minLength, int maxLength)
            throws ValidationException {
        requireNonEmpty(value, fieldName);

        int length = value.trim().length();
        if (length < minLength || length > maxLength) {
            throw new ValidationException(
                    String.format("%s phải có độ dài từ %d đến %d ký tự",
                            fieldName, minLength, maxLength)
            );
        }
    }

    /**
     * Sanitize string input - loại bỏ ký tự đặc biệt nguy hiểm
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }

        // Remove potentially dangerous characters
        // Keep alphanumeric, spaces, and common punctuation
        return input.replaceAll("[^a-zA-Z0-9\\sàáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđĐ@.\\-_]", "")
                .trim();
    }

    // ============== EMAIL VALIDATION ==============

    /**
     * Validate email format
     */
    public static void requireValidEmail(String email) throws ValidationException {
        requireNonEmpty(email, "Email");

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Email không hợp lệ");
        }

        if (email.length() > AppConstants.EMAIL_MAX_LENGTH) {
            throw new ValidationException("Email quá dài (tối đa " + AppConstants.EMAIL_MAX_LENGTH + " ký tự)");
        }
    }

    /**
     * Sanitize email - chuyển thành lowercase và trim
     */
    public static String sanitizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.toLowerCase().trim();
    }

    // ============== PHONE VALIDATION ==============

    /**
     * Validate phone number (Vietnam format)
     */
    public static void requireValidPhone(String phone) throws ValidationException {
        requireNonEmpty(phone, "Số điện thoại");

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new ValidationException("Số điện thoại không hợp lệ (định dạng: +84XXXXXXXXX hoặc 0XXXXXXXXX)");
        }
    }

    /**
     * Normalize phone number (remove spaces, dashes)
     */
    public static String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        return phone.replaceAll("[\\s-]", "");
    }

    // ============== PASSWORD VALIDATION ==============

    /**
     * Validate password strength
     */
    public static void requireValidPassword(String password) throws ValidationException {
        requireNonEmpty(password, "Mật khẩu");

        if (password.length() < AppConstants.PASSWORD_MIN_LENGTH) {
            throw new ValidationException(
                    "Mật khẩu phải có ít nhất " + AppConstants.PASSWORD_MIN_LENGTH + " ký tự"
            );
        }

        if (password.length() > AppConstants.PASSWORD_MAX_LENGTH) {
            throw new ValidationException(
                    "Mật khẩu không được vượt quá " + AppConstants.PASSWORD_MAX_LENGTH + " ký tự"
            );
        }

        // Check for at least one digit
        if (!password.matches(".*\\d.*")) {
            throw new ValidationException("Mật khẩu phải chứa ít nhất một chữ số");
        }

        // Check for at least one letter
        if (!password.matches(".*[a-zA-Z].*")) {
            throw new ValidationException("Mật khẩu phải chứa ít nhất một chữ cái");
        }

        // Optional: Check for special character
        // if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
        //     throw new ValidationException("Mật khẩu phải chứa ít nhất một ký tự đặc biệt");
        // }
    }

    // ============== USERNAME VALIDATION ==============

    /**
     * Validate username
     */
    public static void requireValidUsername(String username) throws ValidationException {
        requireLength(username, "Tên đăng nhập",
                AppConstants.USERNAME_MIN_LENGTH,
                AppConstants.USERNAME_MAX_LENGTH);

        // Username chỉ chứa chữ cái, số và underscore
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new ValidationException(
                    "Tên đăng nhập chỉ được chứa chữ cái, số và dấu gạch dưới"
            );
        }
    }

    // ============== NUMBER VALIDATION ==============

    /**
     * Validate positive integer
     */
    public static void requirePositive(int value, String fieldName) throws ValidationException {
        if (value <= 0) {
            throw new ValidationException(fieldName + " phải là số dương");
        }
    }

    /**
     * Validate non-negative integer
     */
    public static void requireNonNegative(int value, String fieldName) throws ValidationException {
        if (value < 0) {
            throw new ValidationException(fieldName + " không được âm");
        }
    }

    /**
     * Validate value in range
     */
    public static void requireInRange(int value, String fieldName, int min, int max)
            throws ValidationException {
        if (value < min || value > max) {
            throw new ValidationException(
                    String.format("%s phải nằm trong khoảng %d đến %d", fieldName, min, max)
            );
        }
    }

    /**
     * Validate positive double
     */
    public static void requirePositive(double value, String fieldName) throws ValidationException {
        if (value <= 0) {
            throw new ValidationException(fieldName + " phải là số dương");
        }
    }

    /**
     * Validate double in range
     */
    public static void requireInRange(double value, String fieldName, double min, double max)
            throws ValidationException {
        if (value < min || value > max) {
            throw new ValidationException(
                    String.format("%s phải nằm trong khoảng %.2f đến %.2f", fieldName, min, max)
            );
        }
    }

    // ============== PAYMENT VALIDATION ==============

    /**
     * Validate payment amount
     */
    public static void requireValidPaymentAmount(double amount) throws ValidationException {
        requireInRange(amount, "Số tiền thanh toán",
                AppConstants.MIN_PAYMENT_AMOUNT,
                AppConstants.MAX_PAYMENT_AMOUNT);
    }

    // ============== CUSTOM VALIDATION ==============

    /**
     * Validate booking input
     */
    public static void validateBookingInput(int customerId, int showtimeId,
                                            java.util.List<Integer> seatIds)
            throws ValidationException {

        requirePositive(customerId, "ID khách hàng");
        requirePositive(showtimeId, "ID suất chiếu");

        if (seatIds == null || seatIds.isEmpty()) {
            throw new ValidationException(AppConstants.ERR_NO_SEATS_SELECTED);
        }

        if (seatIds.size() > AppConstants.MAX_SEATS_PER_BOOKING) {
            throw new ValidationException(AppConstants.ERR_TOO_MANY_SEATS);
        }

        // Check for duplicates
        long distinctCount = seatIds.stream().distinct().count();
        if (distinctCount != seatIds.size()) {
            throw new ValidationException(AppConstants.ERR_DUPLICATE_SEATS);
        }

        // Validate each seat ID
        for (int seatId : seatIds) {
            requirePositive(seatId, "ID ghế");
        }
    }
}