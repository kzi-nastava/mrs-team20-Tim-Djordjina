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
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.UserBlockedException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.UserNotActivatedException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Driver;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Role;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.DriverRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.security.JwtTokenProvider;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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


    @Test
    @DisplayName("Should return valid Jwt token")
    void loginUser_ShouldReturnValidJwtToken(){
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("john@example.com", "USER"))
                .thenReturn("eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIn0.abc123");

        // Act
        LoginResponse response = authService.loginUser(validLoginDTO);

        // Assert
        assertTrue(response.getToken().contains("."));
        assertEquals(3, response.getToken().split("\\.").length);
        assertEquals("Bearer", response.getTokenType());
    }


    @Test
    @DisplayName("Should throw exception with non-existent email")
    void loginUser_WithNonExistentEmail_ShouldThrowException(){
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("nonexistent@example.com");
        loginDTO.setPassword("password123");

        // Act and Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authService.loginUser(loginDTO));

        assertEquals("Invalid email or password.", exception.getMessage());
        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception with wrong password")
    void loginUser_WithWrongPassword_ShouldThrowException(){
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "$2a$10$hashedPassword")).thenReturn(false);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("john@example.com");
        loginDTO.setPassword("wrongPassword");

        // Act and Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authService.loginUser(loginDTO));

        assertEquals("Invalid email or password.", exception.getMessage());
        verify(passwordEncoder).matches("wrongPassword", "$2a$10$hashedPassword");
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyString());

    }

    @Test
    @DisplayName("Should reject login for non-activated account")
    void loginUser_WithNonActivatedAccount_ShouldThrowUserNotActivatedException(){
        // Arrange
        testUser.setActivated(false);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);

        // Act and Assert
        UserNotActivatedException exception = assertThrows(
                UserNotActivatedException.class, () -> authService.loginUser(validLoginDTO)
        );

        assertTrue(exception.getMessage().contains("not activated"));
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Should reject login for blocked account")
    void loginUser_WithBlockedAccount_ShouldThrowUserBlockedException(){
        testUser.setBlocked(true);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);

        // Act and Assert
        UserBlockedException exception = assertThrows(
                UserBlockedException.class, () -> authService.loginUser(validLoginDTO)
        );

        assertTrue(exception.getMessage().contains("blocked"));
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyString());
    }


    @Test
    @DisplayName("Should include block reason in exception message for blocked account")
    void loginUser_WithBlockedAccount_ShouldIncludeBlockReason(){
        // Arrange
        testUser.setBlocked(true);
        testUser.setBlockNote("Suspicious activity detected");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);

        // Act and Assert
        UserBlockedException exception = assertThrows(
                UserBlockedException.class, () -> authService.loginUser(validLoginDTO)
        );

        assertTrue(exception.getMessage().contains("Suspicious activity detected"));
    }

    @Test
    @DisplayName("Should return LoginResponse with isDriver=true for driver user")
    void loginUser_WithDriverUser_ShouldReturnIsDriverTrue(){
        // Arrange
        testUser.setRole(Role.DRIVER);

        Driver driver = new Driver();
        driver.setId(1L);
        driver.setUser(testUser);
        driver.setLoggedIn(false);
        driver.setActive(false);
        driver.setAvailable(false);
        driver.setWorkingMinutesLast24Hours(0);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("john@example.com", Role.DRIVER.toString())).thenReturn("driver-token");
        when(driverRepository.findByUserId(1L)).thenReturn(Optional.of(driver));    // 1L - user id
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);


        // Act
        LoginResponse response = authService.loginUser(validLoginDTO);

        //Assert
        assertTrue(response.isDriver());
        assertEquals(0, response.getWorkingHours());
        assertEquals("DRIVER", response.getRole());

    }

    @Test
    @DisplayName("Should handle driver login and set availability")
    void handleDriverLogin_ShouldMarkDriverAsAvailable(){
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setActivated(true);
        user.setBlocked(false);

        Driver driver = new Driver();
        driver.setUser(user);
        driver.setLoggedIn(false);
        driver.setActive(false);
        driver.setAvailable(false);
        driver.setWorkingMinutesLast24Hours(0);

        when(driverRepository.findByUserId(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        // Act
        authService.handleDriverLogin(1L);

        // Assert
        verify(driverRepository).findByUserId(1L);
        verify(driverRepository).save(argThat(d ->
            d.isLoggedIn() && d.isActive() && d.isAvailable()
        ));
    }

    @Test
    @DisplayName("Should reset driver working hours if 24h passed")
    void handleDriverLogin_ShouldResetDriverWorkingHoursIf24HoursPassed(){
        // Arrange
        User user = new User();
        user.setId(2L);     // user's ID
        user.setActivated(true);
        user.setBlocked(false);

        Driver driver = new Driver();
        driver.setId(1L);   // driver's ID
        driver.setUser(user);
        driver.setWorkingMinutesLast24Hours(480); // 8 hours
        driver.setLastWorkingHoursReset(LocalDateTime.now().minusHours(25));    // 25 hours age

        when(driverRepository.findByUserId(2L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        // Act
        authService.handleDriverLogin(2L);

        // Assert
        verify(driverRepository).findByUserId(2L);
        verify(driverRepository).save(any(Driver.class));
    }

    // TODO
    @Test
    @DisplayName("Should not allow login if driver exceeded working hours")
    void handleDriverLogin_WithExceededHours_ShouldNotSetAvailable(){
        // Arrange
        User user = new User();
        user.setId(2L);     // user's ID

        Driver driver = new Driver();
        driver.setId(1L);   // driver's ID
        driver.setUser(user);
        driver.setLoggedIn(false);
        driver.setActive(true);
        driver.setAvailable(true);
        driver.setWorkingMinutesLast24Hours(500);   // more than 8 hours (>480)
        driver.setHasActiveRide(false);

        when(driverRepository.findByUserId(2L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        // Act
        authService.handleDriverLogin(2L);

        // Assert
        verify(driverRepository).findByUserId(2L);
        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    @DisplayName("Should logout driver successfully")
    void handleDriverLogout_ShouldMarkDriverAsLoggedOut(){
        Driver driver = new Driver();
        driver.setId(1L);
        driver.setUser(testUser);
        driver.setLoggedIn(true);
        driver.setActive(true);
        driver.setAvailable(true);

        when(driverRepository.findByUserId(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        // Act
        authService.handleDriverLogout(1L);

        // Assert
        verify(driverRepository).save(argThat(d ->
                !d.isLoggedIn() && !d.isAvailable()
        ));
    }

    @Test
    @DisplayName("Should prevent logout if driver has active ride")
    void handleDriverLogout_WithActiveRide_ShouldThrowException() {
        // Arrange
        Driver driver = new Driver();
        driver.setId(1L);
        driver.setUser(testUser);
        driver.setHasActiveRide(true);
        driver.setCurrentRideId(123L); // has active ride

        when(driverRepository.findByUserId(1L)).thenReturn(Optional.of(driver));

        // Act and Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authService.handleDriverLogout(1L)
        );

        assertEquals("Cannot logout while you have an active ride.", exception.getMessage());
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    @DisplayName("Should toggle driver availability status")
    void toggleDriverAvailability_ShouldToggleActiveStatus() {
        // Arrange
        Driver driver = new Driver();
        driver.setId(1L);
        driver.setUser(testUser);
        driver.setLoggedIn(true);
        driver.setActive(true);

        when(driverRepository.findByUserId(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        // Act
        authService.toggleDriverAvailability(1L);

        // Assert
        verify(driverRepository).save(argThat(d -> !d.isActive()));
    }

    @Test
    @DisplayName("Should prevent toggle if driver not logged in")
    void toggleDriverAvailability_NotLoggedIn_ShouldThrowException() {
        // Arrange
        Driver driver = new Driver();
        driver.setId(1L);
        driver.setUser(testUser);
        driver.setLoggedIn(false);

        when(driverRepository.findByUserId(1L)).thenReturn(Optional.of(driver));

        // Act and Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authService.toggleDriverAvailability(1L)
        );

        assertEquals("Driver must be logged in to toggle availability.", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle case-insensitive email")
    void loginUser_WithDifferentCaseEmail_ShouldWork(){
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("john@example.com", "USER")).thenReturn("token");

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("john@example.com");
        loginDTO.setPassword("password123");

        // Act
        LoginResponse response = authService.loginUser(loginDTO);

        // Assert
        assertNotNull(response);
        assertEquals("john@example.com", response.getEmail());
    }

    @Test
    @DisplayName("Should not expose detailed error messages for security")
    void loginUser_ShouldNotExposeDetailedErrors() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        // Act and Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.loginUser(validLoginDTO)
        );

        assertEquals("Invalid email or password.", exception.getMessage());
        assertFalse(exception.getMessage().contains("not found"));
        assertFalse(exception.getMessage().contains("does not exist"));
    }
}
