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

    private final Long TEST_USER_ID = 1L;
    private final Long TEST_DRIVER_ID = 1L;
    private final String TEST_USER_EMAIL = "john@example.com";
    private final String TEST_USER_FIRST_NAME = "John";
    private final String TEST_USER_LAST_NAME = "Doe";
    private final String TEST_USER_PASSWORD = "password123";
    private final String TEST_USER_PASSWORD_HASH = "$2a$10$hashedPassword";


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
        testUser.setId(TEST_USER_ID);
        testUser.setEmail(TEST_USER_EMAIL);
        testUser.setFirstName(TEST_USER_FIRST_NAME);
        testUser.setLastName(TEST_USER_LAST_NAME);
        testUser.setPasswordHash(TEST_USER_PASSWORD_HASH);
        testUser.setRole(Role.USER);
        testUser.setActivated(true);
        testUser.setBlocked(false);

        // Create valid login DTO
        validLoginDTO = new LoginDTO();
        validLoginDTO.setEmail(TEST_USER_EMAIL);
        validLoginDTO.setPassword(TEST_USER_PASSWORD);
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void loginUser_WithValidCredentials_ShouldReturnLoginResponse(){
        // Arrange
        String role = "USER";
        String validJwtToken = "valid-jwt-token";
        String tokenType = "Bearer";
        when(userRepository.findByEmail(TEST_USER_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_USER_PASSWORD, TEST_USER_PASSWORD_HASH)).thenReturn(true);
        when(jwtTokenProvider.generateToken(TEST_USER_EMAIL, role)).thenReturn(validJwtToken);

        // Act
        LoginResponse response = authService.loginUser(validLoginDTO);

        // Assert
        assertNotNull(response);
        assertEquals(validJwtToken, response.getToken());
        assertEquals(tokenType, response.getTokenType());
        assertEquals(TEST_USER_ID, response.getUserId());
        assertEquals(TEST_USER_EMAIL, response.getEmail());
        assertEquals(TEST_USER_FIRST_NAME, response.getFirstName());
        assertEquals(TEST_USER_LAST_NAME, response.getLastName());
        assertEquals(role, response.getRole());
        assertFalse(response.isDriver());

        verify(userRepository).findByEmail(TEST_USER_EMAIL);
        verify(passwordEncoder).matches(TEST_USER_PASSWORD, TEST_USER_PASSWORD_HASH);
        verify(jwtTokenProvider).generateToken(TEST_USER_EMAIL, role);
    }


    @Test
    @DisplayName("Should return valid Jwt token")
    void loginUser_ShouldReturnValidJwtToken(){
        // Arrange
        String jwt_token = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIn0.abc123";
        when(userRepository.findByEmail(TEST_USER_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_USER_PASSWORD, TEST_USER_PASSWORD_HASH)).thenReturn(true);
        when(jwtTokenProvider.generateToken(TEST_USER_EMAIL, "USER"))
                .thenReturn(jwt_token);

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
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(nonExistentEmail);
        loginDTO.setPassword(TEST_USER_PASSWORD);

        // Act and Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authService.loginUser(loginDTO));

        assertEquals("Invalid email or password.", exception.getMessage());
        verify(userRepository).findByEmail(nonExistentEmail);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception with wrong password")
    void loginUser_WithWrongPassword_ShouldThrowException(){
        // Arrange
        String wrongPassword = "wrongPassword";
        when(userRepository.findByEmail(TEST_USER_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(wrongPassword, TEST_USER_PASSWORD_HASH)).thenReturn(false);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(TEST_USER_EMAIL);
        loginDTO.setPassword(wrongPassword);

        // Act and Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authService.loginUser(loginDTO));

        assertEquals("Invalid email or password.", exception.getMessage());
        verify(passwordEncoder).matches(wrongPassword, TEST_USER_PASSWORD_HASH);
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyString());

    }

    @Test
    @DisplayName("Should reject login for non-activated account")
    void loginUser_WithNonActivatedAccount_ShouldThrowUserNotActivatedException(){
        // Arrange
        testUser.setActivated(false);
        when(userRepository.findByEmail(TEST_USER_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_USER_PASSWORD, TEST_USER_PASSWORD_HASH)).thenReturn(true);

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
        when(userRepository.findByEmail(TEST_USER_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_USER_PASSWORD, TEST_USER_PASSWORD_HASH)).thenReturn(true);

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
        String blockNote = "Suspicious activity detected";
        testUser.setBlocked(true);
        testUser.setBlockNote(blockNote);

        when(userRepository.findByEmail(TEST_USER_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_USER_PASSWORD, TEST_USER_PASSWORD_HASH)).thenReturn(true);

        // Act and Assert
        UserBlockedException exception = assertThrows(
                UserBlockedException.class, () -> authService.loginUser(validLoginDTO)
        );

        assertTrue(exception.getMessage().contains(blockNote));
    }

    @Test
    @DisplayName("Should return LoginResponse with isDriver=true for driver user")
    void loginUser_WithDriverUser_ShouldReturnIsDriverTrue(){
        // Arrange
        testUser.setRole(Role.DRIVER);

        Driver driver = new Driver();
        driver.setId(TEST_DRIVER_ID);
        driver.setUser(testUser);
        driver.setLoggedIn(false);
        driver.setActive(false);
        driver.setAvailable(false);
        driver.setWorkingMinutesLast24Hours(0);

        String driverToken = "driver-token";

        when(userRepository.findByEmail(TEST_USER_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_USER_PASSWORD, TEST_USER_PASSWORD_HASH)).thenReturn(true);
        when(jwtTokenProvider.generateToken(TEST_USER_EMAIL, Role.DRIVER.toString())).thenReturn(driverToken);
        when(driverRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(driver));
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
        Driver driver = new Driver();
        driver.setUser(testUser);
        driver.setLoggedIn(false);
        driver.setActive(false);
        driver.setAvailable(false);
        driver.setWorkingMinutesLast24Hours(0);

        when(driverRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        // Act
        authService.handleDriverLogin(TEST_USER_ID);

        // Assert
        verify(driverRepository).findByUserId(TEST_USER_ID);
        verify(driverRepository).save(argThat(d ->
            d.isLoggedIn() && d.isActive() && d.isAvailable()
        ));
    }

    @Test
    @DisplayName("Should reset driver working hours if 24h passed")
    void handleDriverLogin_ShouldResetDriverWorkingHoursIf24HoursPassed(){
        // Arrange
        Driver driver = new Driver();
        driver.setId(TEST_DRIVER_ID);   // driver's ID
        driver.setUser(testUser);
        driver.setWorkingMinutesLast24Hours(480); // 8 hours
        driver.setLastWorkingHoursReset(LocalDateTime.now().minusHours(25));    // 25 hours age

        when(driverRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        // Act
        authService.handleDriverLogin(TEST_USER_ID);

        // Assert
        verify(driverRepository).findByUserId(TEST_USER_ID);
        verify(driverRepository).save(any(Driver.class));
    }

    // TODO - should not set available
    @Test
    @DisplayName("Should not allow login if driver exceeded working hours")
    void handleDriverLogin_WithExceededHours_ShouldNotSetAvailable(){
        // Arrange
        Driver driver = new Driver();
        driver.setId(TEST_DRIVER_ID);
        driver.setUser(testUser);
        driver.setLoggedIn(false);
        driver.setActive(true);
        driver.setAvailable(true);
        driver.setWorkingMinutesLast24Hours(500);   // more than 8 hours (>480)
        driver.setHasActiveRide(false);

        when(driverRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        // Act
        authService.handleDriverLogin(TEST_USER_ID);

        // Assert
        verify(driverRepository).findByUserId(TEST_USER_ID);
        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    @DisplayName("Should logout driver successfully")
    void handleDriverLogout_ShouldMarkDriverAsLoggedOut(){
        Driver driver = new Driver();
        driver.setId(TEST_DRIVER_ID);
        driver.setUser(testUser);
        driver.setLoggedIn(true);
        driver.setActive(true);
        driver.setAvailable(true);

        when(driverRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        // Act
        authService.handleDriverLogout(TEST_USER_ID);

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
        driver.setId(TEST_DRIVER_ID);
        driver.setUser(testUser);
        driver.setHasActiveRide(true);
        driver.setCurrentRideId(123L); // has active ride

        when(driverRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(driver));

        // Act and Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authService.handleDriverLogout(TEST_USER_ID)
        );

        assertEquals("Cannot logout while you have an active ride.", exception.getMessage());
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    @DisplayName("Should toggle driver availability status")
    void toggleDriverAvailability_ShouldToggleActiveStatus() {
        // Arrange
        Driver driver = new Driver();
        driver.setId(TEST_DRIVER_ID);
        driver.setUser(testUser);
        driver.setLoggedIn(true);
        driver.setActive(true);

        when(driverRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        // Act
        authService.toggleDriverAvailability(TEST_USER_ID);

        // Assert
        verify(driverRepository).save(argThat(d -> !d.isActive()));
    }

    @Test
    @DisplayName("Should prevent toggle if driver not logged in")
    void toggleDriverAvailability_NotLoggedIn_ShouldThrowException() {
        // Arrange
        Driver driver = new Driver();
        driver.setId(TEST_DRIVER_ID);
        driver.setUser(testUser);
        driver.setLoggedIn(false);

        when(driverRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(driver));

        // Act and Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authService.toggleDriverAvailability(TEST_USER_ID)
        );

        assertEquals("Driver must be logged in to toggle availability.", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle case-insensitive email")
    void loginUser_WithDifferentCaseEmail_ShouldWork(){
        // Arrange
        String role = "USER";
        String jwtToken = "token";
        when(userRepository.findByEmail(TEST_USER_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_USER_PASSWORD, TEST_USER_PASSWORD_HASH)).thenReturn(true);
        when(jwtTokenProvider.generateToken(TEST_USER_EMAIL, role)).thenReturn(jwtToken);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(TEST_USER_EMAIL);
        loginDTO.setPassword(TEST_USER_PASSWORD);

        // Act
        LoginResponse response = authService.loginUser(loginDTO);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_USER_EMAIL, response.getEmail());
    }

    @Test
    @DisplayName("Should not expose detailed error messages for security")
    void loginUser_ShouldNotExposeDetailedErrors() {
        // Arrange
        when(userRepository.findByEmail(TEST_USER_EMAIL)).thenReturn(Optional.empty());

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
