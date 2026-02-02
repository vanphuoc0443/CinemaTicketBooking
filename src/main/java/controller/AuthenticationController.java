package controller;

import dao.CustomerDAO;
import model.Customer;
import exception.ValidationException;
import exception.DatabaseException;

import java.sql.SQLException;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Controller xử lý authentication và registration
 */
public class AuthenticationController {
    private final CustomerDAO customerDAO;

    // Email regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // Phone regex pattern (Vietnamese phone numbers)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$"
    );

    public AuthenticationController() {
        this.customerDAO = new CustomerDAO();
    }

    /**
     * Đăng ký khách hàng mới
     *
     * @param name Họ tên
     * @param email Email
     * @param phone Số điện thoại
     * @param dateOfBirth Ngày sinh
     * @return Customer object nếu đăng ký thành công
     * @throws ValidationException nếu dữ liệu không hợp lệ
     * @throws DatabaseException nếu có lỗi database
     */
    public Customer registerCustomer(String name, String email, String phone, Date dateOfBirth)
            throws ValidationException, DatabaseException {

        // Validate input
        validateRegistrationInput(name, email, phone, dateOfBirth);

        try {
            // Check if email already exists
            if (customerDAO.emailExists(email)) {
                throw new ValidationException("Email này đã được sử dụng");
            }

            // Check if phone already exists
            Customer existingCustomer = customerDAO.findByPhone(phone);
            if (existingCustomer != null) {
                throw new ValidationException("Số điện thoại này đã được sử dụng");
            }

            // Create new customer
            Customer customer = new Customer();
            customer.setName(name.trim());
            customer.setEmail(email.trim().toLowerCase());
            customer.setPhone(phone.trim());
            customer.setDateOfBirth(dateOfBirth);

            // Save to database
            boolean success = customerDAO.save(customer);

            if (!success) {
                throw new DatabaseException("Không thể tạo tài khoản. Vui lòng thử lại.");
            }

            return customer;

        } catch (SQLException e) {
            throw new DatabaseException("Lỗi database khi đăng ký: " + e.getMessage(), e);
        }
    }

    /**
     * Đăng nhập
     *
     * @param email Email
     * @return Customer object nếu tìm thấy
     * @throws ValidationException nếu email không hợp lệ
     * @throws DatabaseException nếu có lỗi database
     */
    public Customer login(String email) throws ValidationException, DatabaseException {
        // Validate email
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email không được để trống");
        }

        if (!isValidEmail(email)) {
            throw new ValidationException("Email không hợp lệ");
        }

        try {
            Customer customer = customerDAO.findByEmail(email.trim().toLowerCase());

            if (customer == null) {
                throw new ValidationException("Không tìm thấy tài khoản với email này");
            }

            return customer;

        } catch (SQLException e) {
            throw new DatabaseException("Lỗi database khi đăng nhập: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật thông tin khách hàng
     *
     * @param customer Customer object cần cập nhật
     * @return true nếu cập nhật thành công
     * @throws ValidationException nếu dữ liệu không hợp lệ
     * @throws DatabaseException nếu có lỗi database
     */
    public boolean updateCustomer(Customer customer) throws ValidationException, DatabaseException {
        // Validate input
        if (customer == null) {
            throw new ValidationException("Thông tin khách hàng không được null");
        }

        validateName(customer.getName());
        validatePhone(customer.getPhone());

        try {
            // Check if phone is already used by another customer
            Customer existingCustomer = customerDAO.findByPhone(customer.getPhone());
            if (existingCustomer != null && existingCustomer.getCustomerId() != customer.getCustomerId()) {
                throw new ValidationException("Số điện thoại này đã được sử dụng");
            }

            return customerDAO.update(customer);

        } catch (SQLException e) {
            throw new DatabaseException("Lỗi database khi cập nhật: " + e.getMessage(), e);
        }
    }

    /**
     * Validate toàn bộ input đăng ký
     */
    private void validateRegistrationInput(String name, String email, String phone, Date dateOfBirth)
            throws ValidationException {

        validateName(name);
        validateEmail(email);
        validatePhone(phone);
        validateDateOfBirth(dateOfBirth);
    }

    /**
     * Validate tên
     */
    private void validateName(String name) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Họ tên không được để trống");
        }

        if (name.trim().length() < 2) {
            throw new ValidationException("Họ tên phải có ít nhất 2 ký tự");
        }

        if (name.trim().length() > 100) {
            throw new ValidationException("Họ tên không được quá 100 ký tự");
        }
    }

    /**
     * Validate email
     */
    private void validateEmail(String email) throws ValidationException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email không được để trống");
        }

        if (!isValidEmail(email)) {
            throw new ValidationException("Email không hợp lệ");
        }
    }

    /**
     * Validate số điện thoại
     */
    private void validatePhone(String phone) throws ValidationException {
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("Số điện thoại không được để trống");
        }

        if (!isValidPhone(phone)) {
            throw new ValidationException("Số điện thoại không hợp lệ");
        }
    }

    /**
     * Validate ngày sinh
     */
    private void validateDateOfBirth(Date dateOfBirth) throws ValidationException {
        if (dateOfBirth == null) {
            throw new ValidationException("Ngày sinh không được để trống");
        }

        // Check if age is at least 13 years old
        long ageInMillis = System.currentTimeMillis() - dateOfBirth.getTime();
        long yearsInMillis = 13L * 365L * 24L * 60L * 60L * 1000L;

        if (ageInMillis < yearsInMillis) {
            throw new ValidationException("Bạn phải từ 13 tuổi trở lên để đăng ký");
        }
    }

    /**
     * Kiểm tra email hợp lệ
     */
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Kiểm tra số điện thoại hợp lệ
     */
    private boolean isValidPhone(String phone) {
        String cleanPhone = phone.replaceAll("[\\s.-]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    /**
     * Tìm khách hàng theo ID
     */
    public Customer findCustomerById(int customerId) throws DatabaseException {
        try {
            return customerDAO.findById(customerId);
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi tìm khách hàng: " + e.getMessage(), e);
        }
    }
}