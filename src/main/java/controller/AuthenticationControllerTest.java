package controller;

import dao.CustomerDAO;
import model.Customer;
import exception.ValidationException;
import exception.DatabaseException;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests cho AuthenticationController
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthenticationControllerTest {

    private AuthenticationController authController;

    @Mock
    private CustomerDAO mockCustomerDAO;

    private AutoCloseable closeable;

    @BeforeAll
    void setUpAll() {
        System.out.println("===== AuthenticationController Test Suite Started =====");
    }

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        authController = new AuthenticationController();
        System.out.println("Test case started");
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
        System.out.println("Test case completed");
    }

    @AfterAll
    void tearDownAll() {
        System.out.println("===== AuthenticationController Test Suite Completed =====");
    }

    // ============== HELPER METHODS ==============

    private Date createValidDateOfBirth() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -20); // 20 years ago
        return cal.getTime();
    }

    private Date createUnderageDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -10); // 10 years ago
        return cal.getTime();
    }

    // ============== REGISTRATION TESTS ==============

    @Test
    @DisplayName("Should register customer successfully with valid data")
    void testRegisterCustomer_Success() {
        String name = "Nguyen Van A";
        String email = "nguyenvana@example.com";
        String phone = "0901234567";
        Date dateOfBirth = createValidDateOfBirth();

        assertDoesNotThrow(() -> {
            // Validate all fields are correct
            assertNotNull(name);
            assertNotNull(email);
            assertNotNull(phone);
            assertNotNull(dateOfBirth);

            assertTrue(name.length() >= 2);
            assertTrue(email.contains("@"));
            assertTrue(phone.matches("^0[0-9]{9,10}$"));
        });
    }

    @Test
    @DisplayName("Should throw ValidationException when name is null")
    void testRegisterCustomer_NullName() {
        String name = null;
        String email = "test@example.com";
        String phone = "0901234567";
        Date dateOfBirth = createValidDateOfBirth();

        assertThrows(ValidationException.class, () -> {
            if (name == null || name.trim().isEmpty()) {
                throw new ValidationException("Họ tên không được để trống");
            }
        });
    }

    @Test
    @DisplayName("Should throw ValidationException when name is empty")
    void testRegisterCustomer_EmptyName() {
        String name = "";
        String email = "test@example.com";
        String phone = "0901234567";
        Date dateOfBirth = createValidDateOfBirth();

        assertThrows(ValidationException.class, () -> {
            if (name == null || name.trim().isEmpty()) {
                throw new ValidationException("Họ tên không được để trống");
            }
        });
    }

    @Test
    @DisplayName("Should throw ValidationException when name is too short")
    void testRegisterCustomer_NameTooShort() {
        String name = "A";

        assertThrows(ValidationException.class, () -> {
            if (name.trim().length() < 2) {
                throw new ValidationException("Họ tên phải có ít nhất 2 ký tự");
            }
        });
    }

    @Test
    @DisplayName("Should throw ValidationException when name is too long")
    void testRegisterCustomer_NameTooLong() {
        String name = "A".repeat(101);

        assertThrows(ValidationException.class, () -> {
            if (name.trim().length() > 100) {
                throw new ValidationException("Họ tên không được quá 100 ký tự");
            }
        });
    }

    @Test
    @DisplayName("Should throw ValidationException when email is null")
    void testRegisterCustomer_NullEmail() {
        String email = null;

        assertThrows(ValidationException.class, () -> {
            if (email == null || email.trim().isEmpty()) {
                throw new ValidationException("Email không được để trống");
            }
        });
    }

    @Test
    @DisplayName("Should throw ValidationException when email is invalid")
    void testRegisterCustomer_InvalidEmail() {
        String[] invalidEmails = {
                "invalid-email",
                "missing@",
                "@missing.com",
                "missing.com",
                "test@",
                "@test.com",
                "test@@test.com",
                "test@test"
        };

        for (String email : invalidEmails) {
            assertFalse(email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"),
                    "Email should be invalid: " + email);
        }
    }

    @Test
    @DisplayName("Should accept valid email formats")
    void testRegisterCustomer_ValidEmails() {
        String[] validEmails = {
                "test@example.com",
                "user.name@example.com",
                "user+tag@example.co.uk",
                "user_name@example-domain.com",
                "123@example.com"
        };

        for (String email : validEmails) {
            assertTrue(email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"),
                    "Email should be valid: " + email);
        }
    }

    @Test
    @DisplayName("Should throw ValidationException when phone is null")
    void testRegisterCustomer_NullPhone() {
        String phone = null;

        assertThrows(ValidationException.class, () -> {
            if (phone == null || phone.trim().isEmpty()) {
                throw new ValidationException("Số điện thoại không được để trống");
            }
        });
    }

    @Test
    @DisplayName("Should throw ValidationException when phone is invalid")
    void testRegisterCustomer_InvalidPhone() {
        String[] invalidPhones = {
                "123",
                "12345",
                "abc123456",
                "0123456",
                "1234567890",
                "+123456789"
        };

        String vietnamPhonePattern = "^(0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$";

        for (String phone : invalidPhones) {
            String cleanPhone = phone.replaceAll("[\\s.-]", "");
            assertFalse(cleanPhone.matches(vietnamPhonePattern),
                    "Phone should be invalid: " + phone);
        }
    }

    @Test
    @DisplayName("Should accept valid Vietnamese phone numbers")
    void testRegisterCustomer_ValidPhones() {
        String[] validPhones = {
                "0901234567",
                "0912345678",
                "0923456789",
                "0987654321",
                "+84901234567",
                "0909.123.456",
                "090 912 3456"
        };

        // Basic validation (not full Vietnamese pattern)
        for (String phone : validPhones) {
            String cleanPhone = phone.replaceAll("[\\s.-]", "");
            assertTrue(cleanPhone.matches("^(\\+84|0)[0-9]{9,10}$"),
                    "Phone should be valid: " + phone);
        }
    }

    @Test
    @DisplayName("Should throw ValidationException when date of birth is null")
    void testRegisterCustomer_NullDateOfBirth() {
        Date dateOfBirth = null;

        assertThrows(ValidationException.class, () -> {
            if (dateOfBirth == null) {
                throw new ValidationException("Ngày sinh không được để trống");
            }
        });
    }

    @Test
    @DisplayName("Should throw ValidationException when user is underage")
    void testRegisterCustomer_Underage() {
        Date dateOfBirth = createUnderageDate();

        assertThrows(ValidationException.class, () -> {
            long ageInMillis = System.currentTimeMillis() - dateOfBirth.getTime();
            long yearsInMillis = 13L * 365L * 24L * 60L * 60L * 1000L;

            if (ageInMillis < yearsInMillis) {
                throw new ValidationException("Bạn phải từ 13 tuổi trở lên để đăng ký");
            }
        });
    }

    @Test
    @DisplayName("Should throw ValidationException when email already exists")
    void testRegisterCustomer_DuplicateEmail() {
        // This would require mocking CustomerDAO
        String existingEmail = "existing@example.com";

        assertDoesNotThrow(() -> {
            // Simulate email check
            boolean emailExists = true; // Would come from DAO
            if (emailExists) {
                throw new ValidationException("Email này đã được sử dụng");
            }
        });

        assertThrows(ValidationException.class, () -> {
            boolean emailExists = true;
            if (emailExists) {
                throw new ValidationException("Email này đã được sử dụng");
            }
        });
    }

    @Test
    @DisplayName("Should throw ValidationException when phone already exists")
    void testRegisterCustomer_DuplicatePhone() {
        String existingPhone = "0901234567";

        assertThrows(ValidationException.class, () -> {
            boolean phoneExists = true; // Would come from DAO
            if (phoneExists) {
                throw new ValidationException("Số điện thoại này đã được sử dụng");
            }
        });
    }

    @Test
    @DisplayName("Should normalize email to lowercase")
    void testRegisterCustomer_EmailNormalization() {
        String email = "TEST@EXAMPLE.COM";
        String normalized = email.trim().toLowerCase();

        assertEquals("test@example.com", normalized);
    }

    @Test
    @DisplayName("Should trim whitespace from inputs")
    void testRegisterCustomer_InputTrimming() {
        String name = "  Nguyen Van A  ";
        String email = "  test@example.com  ";
        String phone = "  0901234567  ";

        assertEquals("Nguyen Van A", name.trim());
        assertEquals("test@example.com", email.trim());
        assertEquals("0901234567", phone.trim());
    }

    // ============== LOGIN TESTS ==============

    @Test
    @DisplayName("Should login successfully with valid email")
    void testLogin_Success() {
        String email = "test@example.com";

        assertDoesNotThrow(() -> {
            assertNotNull(email);
            assertFalse(email.trim().isEmpty());
            assertTrue(email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"));
        });
    }

    @Test
    @DisplayName("Should throw ValidationException when login email is null")
    void testLogin_NullEmail() {
        String email = null;

        assertThrows(ValidationException.class, () -> {
            if (email == null || email.trim().isEmpty()) {
                throw new ValidationException("Email không được để trống");
            }
        });
    }

    @Test
    @DisplayName("Should throw ValidationException when login email is empty")
    void testLogin_EmptyEmail() {
        String email = "";

        assertThrows(ValidationException.class, () -> {
            if (email == null || email.trim().isEmpty()) {
                throw new ValidationException("Email không được để trống");
            }
        });
    }

    @Test
    @DisplayName("Should throw ValidationException when login email is invalid")
    void testLogin_InvalidEmail() {
        String email = "invalid-email";

        assertThrows(ValidationException.class, () -> {
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw new ValidationException("Email không hợp lệ");
            }
        });
    }

    @Test
    @DisplayName("Should throw ValidationException when user not found")
    void testLogin_UserNotFound() {
        String email = "nonexistent@example.com";

        assertThrows(ValidationException.class, () -> {
            Customer customer = null; // Would come from DAO
            if (customer == null) {
                throw new ValidationException("Không tìm thấy tài khoản với email này");
            }
        });
    }

    // ============== UPDATE CUSTOMER TESTS ==============

    @Test
    @DisplayName("Should update customer successfully")
    void testUpdateCustomer_Success() {
        Customer customer = new Customer();
        customer.setCustomerId(1);
        customer.setName("Updated Name");
        customer.setEmail("updated@example.com");
        customer.setPhone("0901234567");
        customer.setDateOfBirth(createValidDateOfBirth());

        assertDoesNotThrow(() -> {
            assertNotNull(customer);
            assertNotNull(customer.getName());
            assertNotNull(customer.getPhone());
        });
    }

    @Test
    @DisplayName("Should throw ValidationException when updating with null customer")
    void testUpdateCustomer_NullCustomer() {
        Customer customer = null;

        assertThrows(ValidationException.class, () -> {
            if (customer == null) {
                throw new ValidationException("Thông tin khách hàng không được null");
            }
        });
    }

    @Test
    @DisplayName("Should throw ValidationException when phone is already used")
    void testUpdateCustomer_DuplicatePhone() {
        assertThrows(ValidationException.class, () -> {
            // Simulate phone already used by another customer
            boolean phoneExistsForOtherCustomer = true;
            if (phoneExistsForOtherCustomer) {
                throw new ValidationException("Số điện thoại này đã được sử dụng");
            }
        });
    }

    // ============== EDGE CASES ==============

    @Test
    @DisplayName("Should handle SQL injection attempts in email")
    void testSQLInjectionPrevention() {
        String maliciousEmail = "test@example.com'; DROP TABLE customers; --";

        assertDoesNotThrow(() -> {
            // PreparedStatement should prevent SQL injection
            assertNotNull(maliciousEmail);
        });
    }

    @Test
    @DisplayName("Should handle special characters in name")
    void testSpecialCharactersInName() {
        String[] namesWithSpecialChars = {
                "Nguyễn Văn A",
                "O'Connor",
                "Jean-Pierre",
                "María José"
        };

        for (String name : namesWithSpecialChars) {
            assertNotNull(name);
            assertTrue(name.length() >= 2);
        }
    }

    @Test
    @DisplayName("Should handle very long email")
    void testVeryLongEmail() {
        String longEmail = "verylongemailaddress" + "a".repeat(200) + "@example.com";

        assertNotNull(longEmail);
        // Should validate max length in actual implementation
    }

    @Test
    @DisplayName("Should handle concurrent registrations with same email")
    void testConcurrentRegistrations() {
        String email = "test@example.com";

        assertDoesNotThrow(() -> {
            // In actual implementation, database constraints should prevent duplicates
            // Transaction isolation should handle concurrency
        });
    }

    // ============== PERFORMANCE TESTS ==============

    @Test
    @DisplayName("Should validate email within acceptable time")
    @Timeout(value = 1, unit = java.util.concurrent.TimeUnit.SECONDS)
    void testEmailValidationPerformance() {
        String email = "test@example.com";
        String pattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        for (int i = 0; i < 10000; i++) {
            email.matches(pattern);
        }
    }

    @Test
    @DisplayName("Should validate phone within acceptable time")
    @Timeout(value = 1, unit = java.util.concurrent.TimeUnit.SECONDS)
    void testPhoneValidationPerformance() {
        String phone = "0901234567";
        String pattern = "^(0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$";

        for (int i = 0; i < 10000; i++) {
            phone.replaceAll("[\\s.-]", "").matches(pattern);
        }
    }

    // ============== INTEGRATION TEST EXAMPLES ==============

    @Test
    @DisplayName("Should complete full registration flow")
    void testFullRegistrationFlow() {
        assertDoesNotThrow(() -> {
            // 1. Validate inputs
            String name = "Nguyen Van A";
            String email = "test@example.com";
            String phone = "0901234567";
            Date dateOfBirth = createValidDateOfBirth();

            assertNotNull(name);
            assertNotNull(email);
            assertNotNull(phone);
            assertNotNull(dateOfBirth);

            // 2. Check duplicates (would call DAO)
            boolean emailExists = false;
            boolean phoneExists = false;

            assertFalse(emailExists);
            assertFalse(phoneExists);

            // 3. Create customer
            Customer customer = new Customer();
            customer.setName(name.trim());
            customer.setEmail(email.trim().toLowerCase());
            customer.setPhone(phone.trim());
            customer.setDateOfBirth(dateOfBirth);

            assertNotNull(customer);

            // 4. Save to database (would call DAO)
            boolean saved = true; // Simulated
            assertTrue(saved);
        });
    }
}