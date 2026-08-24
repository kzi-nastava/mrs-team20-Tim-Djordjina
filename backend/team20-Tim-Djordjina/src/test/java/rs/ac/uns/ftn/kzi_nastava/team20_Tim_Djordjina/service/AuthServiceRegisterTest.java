package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RegistrationDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.EmailAlreadyExistsException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.PasswordMismatchException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Role;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceRegisterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private RegistrationDTO validRegistrationDTO;

    @BeforeEach
    void setup(){
        validRegistrationDTO = new RegistrationDTO(
                "John",
                "Doe",
                "john.doe@example.com",
                "password123",
                "password123",
                "+381641234567",
                "123 Main Street"
        );
    }

    @Test
    void registerUser_WithValidData_ShouldSuccess() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        doNothing().when(emailService).sendActivationEmail(anyString(), anyString(), anyString());

        // Act
        User result = authService.registerUser(validRegistrationDTO);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals(Role.USER, result.getRole());
        assertFalse(result.isActivated());
        assertNotNull(result.getActivationToken());
        assertNotNull(result.getTokenExpirationDate());

        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(emailService).sendActivationEmail(anyString(), eq("John"), anyString());
    }

    @Test
    void registerUser_WithMismatchedPasswords_ShouldThrowException(){
        // Arrange
        validRegistrationDTO.setConfirmPassword("differentPassword");

        // Act and Assert
        assertThrows(PasswordMismatchException.class, () -> {
            authService.registerUser(validRegistrationDTO);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WithExistingEmail_ShouldThrowException(){
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act and Assert
        assertThrows(EmailAlreadyExistsException.class, () -> {
            authService.registerUser(validRegistrationDTO);
        });

        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_ShouldHashPassword() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = authService.registerUser(validRegistrationDTO);

        // Assert
        assertEquals("hashedPassword", result.getPasswordHash());
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void registerUser_ShouldGenerateActivationTokenWith24HourExpiration(){
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime beforeRegistration = LocalDateTime.now().plusHours(24).minusMinutes(1);
        LocalDateTime afterRegistration = LocalDateTime.now().plusHours(24).plusMinutes(1);

        // Act
        User result = authService.registerUser(validRegistrationDTO);

        // Assert
        assertNotNull(result.getActivationToken());
        assertTrue(result.getTokenExpirationDate().isAfter(beforeRegistration));
        assertTrue(result.getTokenExpirationDate().isBefore(afterRegistration));
    }

    @Test
    void activateAccount_WithValidToken_ShouldActivateUser(){
        // Arrange
        String token = "valid-token";
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setActivated(false);
        user.setActivationToken(token);
        user.setTokenExpirationDate(LocalDateTime.now().plusHours(1));

        when(userRepository.findByActivationToken(token)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        authService.activateAccount(token);

        // Assert
        assertTrue(user.isActivated());
        assertNull(user.getActivationToken());
        assertNull(user.getTokenExpirationDate());
        verify(userRepository).save(user);
    }

    @Test
    void activateAccount_WithInvalidToken_ShouldThrowException(){
        // Arrange
        when(userRepository.findByActivationToken(anyString())).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.activateAccount("invalid-token");
        });
    }

    @Test
    void activateAccount_WhenAlreadyActivated_ShouldThrowException(){
        // Arrange
        String token = "valid-token";
        User user = new User();
        user.setActivated(true);
        user.setActivationToken(token);

        when(userRepository.findByActivationToken(token)).thenReturn(Optional.of(user));

        // Act and Assert
        assertThrows(IllegalStateException.class, () -> {
            authService.activateAccount(token);
        });
    }

    @Test
    void activateAccount_WithExpiredToken_ShouldThrowException(){
        // Arrange
        String token = "expired-token";
        User user = new User();
        user.setActivated(false);
        user.setActivationToken(token);
        user.setTokenExpirationDate(LocalDateTime.now().minusHours(1));

        when(userRepository.findByActivationToken(token)).thenReturn(Optional.of(user));

        // Act and Assert
        assertThrows(IllegalStateException.class, () -> {
            authService.activateAccount(token);
        });
    }

    @Test
    void resendActivationEmail_WithValidEmail_ShouldGenerateNewToken(){
        // Arrange
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setFirstName("John");
        user.setActivated(false);
        user.setActivationToken("old-token");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendActivationEmail(anyString(), anyString(), anyString());

        String oldToken = user.getActivationToken();

        // Act
        authService.resendActivationEmail(email);

        // Assert
        assertNotEquals(oldToken, user.getActivationToken());
        assertNotNull(user.getTokenExpirationDate());
        verify(emailService).sendActivationEmail(eq(email), eq("John"), anyString());
    }

    @Test
    void resendActivationEmail_ForAlreadyActivatedAccount_ShouldThrowException(){
        // Arrange
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setActivated(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act and Assert
        assertThrows(IllegalStateException.class, () -> {
            authService.resendActivationEmail(email);
        });
    }

    @Test
    void resendActivationEmail_WithNonExistentEmail_ShouldThrowException(){
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.resendActivationEmail("nonexistent@example.com");
        });
    }
}
