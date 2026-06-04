package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.LoginDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.LoginResponse;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Role;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.DriverRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.security.JwtTokenProvider;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Login Tests")
public class AuthServiceLoginTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginDTO validLoginDTO;

    @BeforeEach
    void setup(){
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("john@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPasswordHash("$2a$10$hashedPassword");
        testUser.setRole(Role.USER);
        testUser.setActivated(true);
        testUser.setBlocked(false);

        // Create valid login DTO
        validLoginDTO = new LoginDTO();
        validLoginDTO.setEmail("john@example.com");
        validLoginDTO.setPassword("password123");
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void loginUser_WithValidCredentials_ShouldReturnLoginResponse(){
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("john@example.com", "USER")).thenReturn("valid-jwt-token");

        // Act
        LoginResponse response = authService.loginUser(validLoginDTO);

        // Assert
        assertNotNull(response);
        assertEquals("valid-jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(1L, response.getUserId());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("USER", response.getRole());
        assertFalse(response.isDriver());

        verify(userRepository).findByEmail("john@example.com");
        verify(passwordEncoder).matches("password123", "$2a$10$hashedPassword");
        verify(jwtTokenProvider).generateToken("john@example.com", "USER");
    }

}
