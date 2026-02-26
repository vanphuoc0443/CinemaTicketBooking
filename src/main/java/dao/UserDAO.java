package dao;

import model.User;
import model.Customer;
import util.DatabaseConnection;
import util.PasswordUtil;

import java.sql.*;
import java.util.UUID;

/**
 * DAO để quản lý User (authentication)
 * Extend CustomerDAO để tái sử dụng các method cơ bản
 */
public class UserDAO {

    private final CustomerDAO customerDAO;

    public UserDAO() {
        this.customerDAO = new CustomerDAO();
    }

    /**
     * Tạo user mới với password
     * Tự động tạo customer trước, sau đó thêm password vào bảng riêng
     */
    public User createUser(String name, String email, String phone,
                           java.util.Date dateOfBirth, String password) throws SQLException {

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Tạo customer trước
            Customer customer = new Customer();
            customer.setName(name);
            customer.setEmail(email);
            customer.setPhone(phone);
            customer.setDateOfBirth(dateOfBirth);

            boolean customerCreated = customerDAO.save(customer);
            if (!customerCreated) {
                conn.rollback();
                return null;
            }

            // 2. Tạo password hash
            String salt = PasswordUtil.generateSalt();
            String passwordHash = PasswordUtil.hashPassword(password, salt);

            // 3. Lưu vào bảng user_credentials (nếu có) hoặc update customers table
            // Giả sử chúng ta update trực tiếp vào customers table
            // Nếu bạn muốn bảng riêng, tạo bảng user_credentials(customer_id, password_hash, salt)

            String sql = "UPDATE customers SET password_hash = ?, salt = ? WHERE customer_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, passwordHash);
                stmt.setString(2, salt);
                stmt.setInt(3, customer.getCustomerId());
                stmt.executeUpdate();
            }

            conn.commit();

            // 4. Tạo User object
            User user = new User(customer);
            user.setPasswordHash(passwordHash);
            user.setSalt(salt);

            return user;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Đăng nhập - verify password và tạo session
     */
    public User login(String email, String password) throws SQLException {
        String sql = "SELECT c.*, c.password_hash, c.salt " +
                "FROM customers c " +
                "WHERE c.email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.toLowerCase().trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    String salt = rs.getString("salt");

                    // Nếu chưa có password (user cũ), return null
                    if (storedHash == null || salt == null) {
                        return null;
                    }

                    // Verify password
                    if (!PasswordUtil.verifyPassword(password, salt, storedHash)) {
                        return null; // Sai password
                    }

                    // Tạo User object
                    User user = extractUserFromResultSet(rs);

                    // Generate session token
                    String sessionToken = generateSessionToken();
                    user.setSessionToken(sessionToken);

                    // Update last login
                    updateLastLogin(user.getCustomerId(), sessionToken);

                    return user;
                }
            }
        }

        return null;
    }

    /**
     * Verify session token
     */
    public User getUserBySessionToken(String sessionToken) throws SQLException {
        String sql = "SELECT c.*, c.password_hash, c.salt " +
                "FROM customers c " +
                "WHERE c.session_token = ? AND c.session_token IS NOT NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sessionToken);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }

        return null;
    }

    /**
     * Logout - xóa session token
     */
    public boolean logout(String sessionToken) throws SQLException {
        String sql = "UPDATE customers SET session_token = NULL WHERE session_token = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sessionToken);
            int result = stmt.executeUpdate();
            conn.commit();

            return result > 0;
        }
    }

    /**
     * Đổi mật khẩu
     */
    public boolean changePassword(int customerId, String oldPassword, String newPassword)
            throws SQLException {

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Verify old password
            String sql = "SELECT password_hash, salt FROM customers WHERE customer_id = ?";
            String storedHash, salt;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, customerId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                    storedHash = rs.getString("password_hash");
                    salt = rs.getString("salt");
                }
            }

            if (!PasswordUtil.verifyPassword(oldPassword, salt, storedHash)) {
                return false; // Old password incorrect
            }

            // 2. Update to new password
            String newSalt = PasswordUtil.generateSalt();
            String newHash = PasswordUtil.hashPassword(newPassword, newSalt);

            sql = "UPDATE customers SET password_hash = ?, salt = ?, session_token = NULL WHERE customer_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newHash);
                stmt.setString(2, newSalt);
                stmt.setInt(3, customerId);
                stmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Reset password (admin function hoặc forgot password)
     */
    public String resetPassword(String email) throws SQLException {
        Customer customer = customerDAO.findByEmail(email);
        if (customer == null) {
            return null;
        }

        // Generate random password
        String newPassword = generateRandomPassword();
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(newPassword, salt);

        String sql = "UPDATE customers SET password_hash = ?, salt = ?, session_token = NULL WHERE customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hash);
            stmt.setString(2, salt);
            stmt.setInt(3, customer.getCustomerId());

            int result = stmt.executeUpdate();
            conn.commit();

            return result > 0 ? newPassword : null;
        }
    }

    // Helper methods

    private void updateLastLogin(int customerId, String sessionToken) throws SQLException {
        String sql = "UPDATE customers SET last_login = CURRENT_TIMESTAMP, session_token = ? WHERE customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sessionToken);
            stmt.setInt(2, customerId);
            stmt.executeUpdate();
            conn.commit();
        }
    }

    private String generateSessionToken() {
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();

        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setCustomerId(rs.getInt("customer_id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setDateOfBirth(rs.getDate("date_of_birth"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));

        // Password fields (có thể null nếu user cũ)
        user.setPasswordHash(rs.getString("password_hash"));
        user.setSalt(rs.getString("salt"));

        // Last login
        try {
            user.setLastLogin(rs.getTimestamp("last_login"));
        } catch (SQLException e) {
            // Column might not exist
        }

        return user;
    }
}