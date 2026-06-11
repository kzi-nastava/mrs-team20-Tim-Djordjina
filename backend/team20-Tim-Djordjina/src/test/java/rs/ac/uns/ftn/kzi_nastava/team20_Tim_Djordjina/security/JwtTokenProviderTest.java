package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT Token Provider Tests")
public class JwtTokenProviderTest {
    private JwtTokenProvider jwtTokenProvider;
    private static final String TEST_SECRET = "test-secret-key-minimum-256-bits-long-for-hs256-algorithm";
    private static final long TEST_EXPIRATION = 86400000;

    @BeforeEach
    void setup(){
        jwtTokenProvider = new JwtTokenProvider();

        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", TEST_EXPIRATION);
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void generateToken_ShouldCreateValidToken() {
        // Act
        String token = jwtTokenProvider.generateToken("john@example.com", "USER");

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("Should generate token with correct structure")
    void generateToken_ShouldHaveCorrectStructure() {
        // Act
        String token = jwtTokenProvider.generateToken("john@example.com", "DRIVER");

        // Assert
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);

        // Each part needs to be non-empty
        for (String part : parts){
            assertFalse(part.isEmpty());
        }
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void generateToken_ShouldGenerateDifferentTokensForDifferentUsers() {
        // Act
        String token1 = jwtTokenProvider.generateToken("john@example.com", "USER");
        String token2 = jwtTokenProvider.generateToken("jane@example.com", "USER");

        // Assert
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Should generate different tokens for different roles")
    void generateToken_ShouldGenerateDifferentTokensForDifferentRoles() {
        // Act
        String userToken = jwtTokenProvider.generateToken("john@example.com", "USER");
        String driverToken = jwtTokenProvider.generateToken("john@example.com", "DRIVER");
        String adminToken = jwtTokenProvider.generateToken("john@example.com", "ADMIN");

        // Assert
        assertNotEquals(userToken, driverToken);
        assertNotEquals(userToken, adminToken);
        assertNotEquals(driverToken, adminToken);
    }

    @Test
    @DisplayName("Should extract email from token")
    void getEmailFromToken_ShouldExtractCorrectEmail(){
        // Arrange
        String email = "john@example.com";
        String token = jwtTokenProvider.generateToken(email, "USER");

        // Act
        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        // Assert
        assertEquals(email, extractedEmail);
    }

    @Test
    @DisplayName("Should extract different emails from different tokens")
    void getEmailFromToken_ShouldExtractDifferentEmails() {
        // Arrange
        String email1 = "john@example.com";
        String email2 = "jane@example.com";
        String token1 = jwtTokenProvider.generateToken(email1, "USER");
        String token2 = jwtTokenProvider.generateToken(email2, "USER");

        // Act
        String extracted1 = jwtTokenProvider.getEmailFromToken(token1);
        String extracted2 = jwtTokenProvider.getEmailFromToken(token2);

        // Assert
        assertEquals(email1, extracted1);
        assertEquals(email2, extracted2);
        assertNotEquals(extracted1, extracted2);
    }

    @Test
    @DisplayName("Should handle invalid token gracefully")
    void getEmailFromToken_WithInvalidToken_ShouldReturnNull() {
        // Act
        String email = jwtTokenProvider.getEmailFromToken("invalid.token.here");

        // Assert
        assertNull(email);
    }

    @Test
    @DisplayName("Should extract role from token")
    void getRoleFromToken_ShouldExtractCorrectRole () {
        // Arrange
        String token = jwtTokenProvider.generateToken("john@example.com", "USER");

        // Act
        String extractedRole = jwtTokenProvider.getRoleFromToken(token);

        // Assert
        assertEquals("USER", extractedRole);
    }

    @Test
    @DisplayName("Should extract different roles from different tokens")
    void getRoleFromToken_ShouldExtractDifferentRoles() {
        // Arrange
        String userToken = jwtTokenProvider.generateToken("john@example.com", "USER");
        String driverToken = jwtTokenProvider.generateToken("jane@example.com", "DRIVER");
        String adminToken = jwtTokenProvider.generateToken("admin@example.com", "ADMIN");

        // Act
        String userRole = jwtTokenProvider.getRoleFromToken(userToken);
        String driverRole = jwtTokenProvider.getRoleFromToken(driverToken);
        String adminRole = jwtTokenProvider.getRoleFromToken(adminToken);

        // Assert
        assertEquals("USER", userRole);
        assertEquals("DRIVER", driverRole);
        assertEquals("ADMIN", adminRole);

    }

    @Test
    @DisplayName("Should handle invalid token for role extraction")
    void getRoleFromToken_WithInvalidToken_ShouldReturnNull(){
        // Act
        String role = jwtTokenProvider.getRoleFromToken("invalid.token.here");

        // Assert
        assertNull(role);
    }

    @Test
    @DisplayName("Should validate valid token")
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String token = jwtTokenProvider.generateToken("john@example.com", "USER");

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject invalidate token signature")
    void validateToken_WithInvalidSignature_ShouldReturnFalse() {
        // Arrange
        String token = "invalid.token.here";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject malformed token")
    void validateToken_WithMalformedToken_ShouldReturnFalse() {
        // Arrange
        String token = "not.a.valid.jwt";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject token with only one part")
    void validateToken_WithIncompleteParts_ShouldReturnFalse() {
        // Arrange
        String token = "onlyonepart";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should validate that new token is not expired")
    void isTokenExpired_WithFreshToken_ShouldReturnFalse() {
        // Arrange
        String token = jwtTokenProvider.generateToken("john@example.com", "USER");

        // Act
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        // Assert
        assertFalse(isExpired);
    }

    @Test
    @DisplayName("Should detect expired token")
    void isTokenExpired_WithExpiredToken_ShouldReturnTrue() {
        // Arrange
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 1L);
        String token = jwtTokenProvider.generateToken("john@example.com", "USER");

        // Wait a bit to ensure token expires
        try{
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        // Assert
        assertTrue(isExpired);

    }

    @Test
    @DisplayName("Should handle invalid token in expiration check")
    void isTokenExpired_WithInvalidToken_ShouldReturnTrue() {
        // Act
        boolean isExpired = jwtTokenProvider.isTokenExpired("invalid.token.here");

        // Assert
        assertTrue(isExpired);

    }

    @Test
    @DisplayName("Should handle complete token lifecycle")
    void tokenLifecycle_ShouldHandleGenerationValidationAndExtraction() {
        // Arrange
        String email = "john@example.com";
        String role = "DRIVER";

        // Act - Generate
        String token = jwtTokenProvider.generateToken(email, role);

        // Assert - Token generated
        assertNotNull(token);

        // Act - Validate
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert - Token is valid
        assertTrue(isValid);

        // Act - Extract email
        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        // Assert - Email matches
        assertEquals(email, extractedEmail);

        // Act - Extract role
        String extractedRole = jwtTokenProvider.getRoleFromToken(token);

        // Assert - Role matches
        assertEquals(role, extractedRole);

        // Act - Check expiration
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        // Assert - Not expired
        assertFalse(isExpired);

    }

    @Test
    @DisplayName("Should generate tokens with correct claims")
    void generateToken_ShouldIncludeAllRequiredClaims() {
        // Arrange
        String email = "john@example.com";
        String role = "ADMIN";

        // Act
        String token = jwtTokenProvider.generateToken(email, role);

        // Assert
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(email, jwtTokenProvider.getEmailFromToken(token));
        assertEquals(role, jwtTokenProvider.getRoleFromToken(token));
        assertFalse(jwtTokenProvider.isTokenExpired(token));
    }

}
