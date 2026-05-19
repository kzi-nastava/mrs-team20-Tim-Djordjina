package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RegistrationDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Role;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.AuthService;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.EmailService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

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


}
