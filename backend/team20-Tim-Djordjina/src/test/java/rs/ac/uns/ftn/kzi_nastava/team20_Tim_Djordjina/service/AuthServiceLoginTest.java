package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.LoginDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Role;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.DriverRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;

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



}
