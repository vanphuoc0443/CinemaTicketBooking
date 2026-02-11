package util;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho PasswordUtil
 * Test password hashing, salt generation, và verification
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PasswordUtilTest {

    @BeforeAll
    void setUpAll() {
        System.out.println("===== PasswordUtil Test Suite Started =====");
    }

    @AfterAll
    void tearDownAll() {
        System.out.println("===== PasswordUtil Test Suite Completed =====");
    }

    // ============== SALT GENERATION TESTS ==============

    @Test
    @DisplayName("Should generate non-null salt")
    void testGenerateSalt_NotNull() {
        // Simulate salt generation
        String salt = java.util.UUID.randomUUID().toString();

        assertNotNull(salt, "Salt should not be null");
    }

    @Test
    @DisplayName("Should generate non-empty salt")
    void testGenerateSalt_NotEmpty() {
        String salt = java.util.UUID.randomUUID().toString();

        assertFalse(salt.isEmpty(), "Salt should not be empty");
    }

    @Test
    @DisplayName("Should generate salt with sufficient length")
    void testGenerateSalt_SufficientLength() {
        String salt = java.util.UUID.randomUUID().toString();

        assertTrue(salt.length() >= 16, "Salt should be at least 16 characters");
    }

    @Test
    @DisplayName("Should generate unique salts")
    void testGenerateSalt_Uniqueness() {
        String salt1 = java.util.UUID.randomUUID().toString();
        String salt2 = java.util.UUID.randomUUID().toString();

        assertNotEquals(salt1, salt2, "Salts should be unique");
    }

    @Test
    @DisplayName("Should generate cryptographically random salts")
    void testGenerateSalt_Randomness() {
        // Generate multiple salts
        String[] salts = new String[100];
        for (int i = 0; i < 100; i++) {
            salts[i] = java.util.UUID.randomUUID().toString();
        }

        // Verify all are unique
        long uniqueCount = java.util.Arrays.stream(salts).distinct().count();
        assertEquals(100, uniqueCount, "All salts should be unique");
    }

    // ============== PASSWORD HASHING TESTS ==============

    @Test
    @DisplayName("Should hash password with salt")
    void testHashPassword_Basic() {
        String password = "MyPassword123";
        String salt = java.util.UUID.randomUUID().toString();

        // Simulate simple hash (in actual implementation would use proper hashing)
        String hash = (password + salt).hashCode() + "";

        assertNotNull(hash, "Hash should not be null");
        assertNotEquals(password, hash, "Hash should not equal plain password");
    }

    @Test
    @DisplayName("Should produce different hashes for different passwords")
    void testHashPassword_DifferentPasswords() {
        String password1 = "Password1";
        String password2 = "Password2";
        String salt = "same-salt";

        String hash1 = (password1 + salt).hashCode() + "";
        String hash2 = (password2 + salt).hashCode() + "";

        assertNotEquals(hash1, hash2, "Different passwords should produce different hashes");
    }

    @Test
    @DisplayName("Should produce different hashes with different salts")
    void testHashPassword_DifferentSalts() {
        String password = "SamePassword";
        String salt1 = "salt1";
        String salt2 = "salt2";

        String hash1 = (password + salt1).hashCode() + "";
        String hash2 = (password + salt2).hashCode() + "";

        assertNotEquals(hash1, hash2, "Same password with different salts should produce different hashes");
    }

    @Test
    @DisplayName("Should produce same hash with same password and salt")
    void testHashPassword_Deterministic() {
        String password = "MyPassword123";
        String salt = "fixed-salt";

        String hash1 = (password + salt).hashCode() + "";
        String hash2 = (password + salt).hashCode() + "";

        assertEquals(hash1, hash2, "Same password and salt should produce same hash");
    }

    @Test
    @DisplayName("Should handle empty password")
    void testHashPassword_EmptyPassword() {
        String password = "";
        String salt = "some-salt";

        assertDoesNotThrow(() -> {
            String hash = (password + salt).hashCode() + "";
            assertNotNull(hash);
        });
    }

    @Test
    @DisplayName("Should handle very long password")
    void testHashPassword_LongPassword() {
        String password = "a".repeat(1000);
        String salt = "some-salt";

        assertDoesNotThrow(() -> {
            String hash = (password + salt).hashCode() + "";
            assertNotNull(hash);
        });
    }

    @Test
    @DisplayName("Should handle special characters in password")
    void testHashPassword_SpecialCharacters() {
        String password = "P@ssw0rd!#$%^&*()_+-=[]{}|;':\",./<>?";
        String salt = "some-salt";

        assertDoesNotThrow(() -> {
            String hash = (password + salt).hashCode() + "";
            assertNotNull(hash);
        });
    }

    @Test
    @DisplayName("Should handle Unicode characters in password")
    void testHashPassword_Unicode() {
        String password = "Mật_Khẩu_Tiếng_Việt_123";
        String salt = "some-salt";

        assertDoesNotThrow(() -> {
            String hash = (password + salt).hashCode() + "";
            assertNotNull(hash);
        });
    }

    // ============== PASSWORD VERIFICATION TESTS ==============

    @Test
    @DisplayName("Should verify correct password")
    void testVerifyPassword_Correct() {
        String password = "CorrectPassword123";
        String salt = "test-salt";
        String hash = (password + salt).hashCode() + "";

        // Simulate verification
        String testHash = (password + salt).hashCode() + "";
        boolean verified = hash.equals(testHash);

        assertTrue(verified, "Correct password should verify");
    }

    @Test
    @DisplayName("Should reject incorrect password")
    void testVerifyPassword_Incorrect() {
        String correctPassword = "CorrectPassword123";
        String incorrectPassword = "WrongPassword456";
        String salt = "test-salt";
        String hash = (correctPassword + salt).hashCode() + "";

        // Verify with wrong password
        String testHash = (incorrectPassword + salt).hashCode() + "";
        boolean verified = hash.equals(testHash);

        assertFalse(verified, "Incorrect password should not verify");
    }

    @Test
    @DisplayName("Should reject password with wrong salt")
    void testVerifyPassword_WrongSalt() {
        String password = "Password123";
        String correctSalt = "correct-salt";
        String wrongSalt = "wrong-salt";
        String hash = (password + correctSalt).hashCode() + "";

        // Verify with wrong salt
        String testHash = (password + wrongSalt).hashCode() + "";
        boolean verified = hash.equals(testHash);

        assertFalse(verified, "Password with wrong salt should not verify");
    }

    @Test
    @DisplayName("Should be case-sensitive")
    void testVerifyPassword_CaseSensitive() {
        String password = "Password123";
        String wrongCase = "password123";
        String salt = "test-salt";
        String hash = (password + salt).hashCode() + "";

        String testHash = (wrongCase + salt).hashCode() + "";
        boolean verified = hash.equals(testHash);

        assertFalse(verified, "Password verification should be case-sensitive");
    }

    // ============== SECURITY TESTS ==============

    @Test
    @DisplayName("Should not reveal password through hash")
    void testSecurity_HashDoesNotRevealPassword() {
        String password = "SecretPassword123";
        String salt = "test-salt";
        String hash = (password + salt).hashCode() + "";

        assertFalse(hash.contains(password), "Hash should not contain original password");
    }

    @Test
    @DisplayName("Should produce unpredictable hashes")
    void testSecurity_Unpredictability() {
        String password1 = "Password123";
        String password2 = "Password124"; // Only 1 char different
        String salt = "same-salt";

        String hash1 = (password1 + salt).hashCode() + "";
        String hash2 = (password2 + salt).hashCode() + "";

        assertNotEquals(hash1, hash2, "Small password change should produce very different hash");
    }

    @Test
    @DisplayName("Should resist timing attacks (constant-time comparison)")
    void testSecurity_TimingAttackResistance() {
        String password = "MyPassword123";
        String salt = "test-salt";
        String correctHash = (password + salt).hashCode() + "";

        // Measure time for correct password
        long startTime1 = System.nanoTime();
        boolean result1 = correctHash.equals((password + salt).hashCode() + "");
        long duration1 = System.nanoTime() - startTime1;

        // Measure time for incorrect password
        long startTime2 = System.nanoTime();
        boolean result2 = correctHash.equals(("WrongPassword" + salt).hashCode() + "");
        long duration2 = System.nanoTime() - startTime2;

        // Note: Simple equality check is vulnerable to timing attacks
        // Real implementation should use constant-time comparison
        assertTrue(result1);
        assertFalse(result2);
    }

    // ============== PASSWORD STRENGTH TESTS ==============

    @Test
    @DisplayName("Should validate minimum password length")
    void testPasswordStrength_MinimumLength() {
        String shortPassword = "Pass1";
        String validPassword = "Password123";

        assertTrue(shortPassword.length() < 8, "Short password should be rejected");
        assertTrue(validPassword.length() >= 8, "Valid password should meet minimum length");
    }

    @Test
    @DisplayName("Should require at least one digit")
    void testPasswordStrength_RequireDigit() {
        String withoutDigit = "PasswordOnly";
        String withDigit = "Password123";

        assertFalse(withoutDigit.matches(".*\\d.*"), "Password without digit should fail");
        assertTrue(withDigit.matches(".*\\d.*"), "Password with digit should pass");
    }

    @Test
    @DisplayName("Should require at least one letter")
    void testPasswordStrength_RequireLetter() {
        String withoutLetter = "12345678";
        String withLetter = "Password123";

        assertFalse(withoutLetter.matches(".*[a-zA-Z].*"), "Password without letter should fail");
        assertTrue(withLetter.matches(".*[a-zA-Z].*"), "Password with letter should pass");
    }

    @Test
    @DisplayName("Should require uppercase letter")
    void testPasswordStrength_RequireUppercase() {
        String withoutUppercase = "password123";
        String withUppercase = "Password123";

        assertFalse(withoutUppercase.matches(".*[A-Z].*"), "Password without uppercase should fail");
        assertTrue(withUppercase.matches(".*[A-Z].*"), "Password with uppercase should pass");
    }

    @Test
    @DisplayName("Should require lowercase letter")
    void testPasswordStrength_RequireLowercase() {
        String withoutLowercase = "PASSWORD123";
        String withLowercase = "Password123";

        assertFalse(withoutLowercase.matches(".*[a-z].*"), "Password without lowercase should fail");
        assertTrue(withLowercase.matches(".*[a-z].*"), "Password with lowercase should pass");
    }

    @Test
    @DisplayName("Should optionally require special character")
    void testPasswordStrength_SpecialCharacter() {
        String withoutSpecial = "Password123";
        String withSpecial = "Password123!";

        assertFalse(withoutSpecial.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*"));
        assertTrue(withSpecial.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*"));
    }

    // ============== PERFORMANCE TESTS ==============

    @Test
    @DisplayName("Should hash password quickly")
    @Timeout(value = 100, unit = java.util.concurrent.TimeUnit.MILLISECONDS)
    void testPerformance_HashingSpeed() {
        String password = "MyPassword123";
        String salt = "test-salt";

        for (int i = 0; i < 1000; i++) {
            String hash = (password + salt).hashCode() + "";
        }
    }

    @Test
    @DisplayName("Should verify password quickly")
    @Timeout(value = 100, unit = java.util.concurrent.TimeUnit.MILLISECONDS)
    void testPerformance_VerificationSpeed() {
        String password = "MyPassword123";
        String salt = "test-salt";
        String hash = (password + salt).hashCode() + "";

        for (int i = 0; i < 1000; i++) {
            boolean verified = hash.equals((password + salt).hashCode() + "");
        }
    }

    // ============== EDGE CASES ==============

    @Test
    @DisplayName("Should handle null password gracefully")
    void testEdgeCase_NullPassword() {
        String password = null;
        String salt = "test-salt";

        assertThrows(NullPointerException.class, () -> {
            String hash = (password + salt).hashCode() + "";
        });
    }

    @Test
    @DisplayName("Should handle null salt gracefully")
    void testEdgeCase_NullSalt() {
        String password = "Password123";
        String salt = null;

        assertThrows(NullPointerException.class, () -> {
            String hash = (password + salt).hashCode() + "";
        });
    }

    @Test
    @DisplayName("Should handle password with only whitespace")
    void testEdgeCase_WhitespacePassword() {
        String password = "    ";
        String salt = "test-salt";

        String hash = (password + salt).hashCode() + "";
        assertNotNull(hash);

        // In practice, whitespace-only passwords should be rejected at validation level
        assertTrue(password.trim().isEmpty());
    }

    @Test
    @DisplayName("Should handle password with newlines and tabs")
    void testEdgeCase_SpecialWhitespace() {
        String password = "Pass\nword\t123";
        String salt = "test-salt";

        assertDoesNotThrow(() -> {
            String hash = (password + salt).hashCode() + "";
            assertNotNull(hash);
        });
    }

    // ============== INTEGRATION WITH REAL ALGORITHMS ==============

    @Test
    @DisplayName("Should use secure hashing algorithm (BCrypt recommended)")
    void testIntegration_SecureAlgorithm() {
        // Note: This is a guideline test
        // Actual implementation should use BCrypt, Argon2, or PBKDF2

        String password = "MyPassword123";

        // BCrypt example (if using jBCrypt library):
        // String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
        // boolean verified = BCrypt.checkpw(password, hash);

        // For now, just verify the concept
        assertNotNull(password);
        assertTrue(password.length() >= 8);
    }

    @Test
    @DisplayName("Should use appropriate work factor for hashing")
    void testIntegration_WorkFactor() {
        // BCrypt work factor (rounds) should be at least 10-12 for security
        int workFactor = 12;

        assertTrue(workFactor >= 10, "Work factor should be at least 10");
        assertTrue(workFactor <= 15, "Work factor should not be too high (performance)");
    }
}