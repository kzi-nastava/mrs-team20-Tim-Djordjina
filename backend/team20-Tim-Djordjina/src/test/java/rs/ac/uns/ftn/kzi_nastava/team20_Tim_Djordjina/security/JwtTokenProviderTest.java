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

}
