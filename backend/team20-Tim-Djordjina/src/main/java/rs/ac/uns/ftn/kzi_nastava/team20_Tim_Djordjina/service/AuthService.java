package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.LoginDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.LoginResponse;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RegistrationDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.EmailAlreadyExistsException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.PasswordMismatchException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.UserBlockedException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.UserNotActivatedException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Driver;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Role;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.DriverRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.security.JwtTokenProvider;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.EmailService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/*
* Authentication service handling user registration
* */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;


    // ===============================
    // LOGIN IMPLEMENTATION (US#2.2.1)
    // ===============================

    /**
     * Login user with email and password
     * Rules:
     * - User must exist in database
     * - Passwords must match
     * - Account must be activated
     * - Account must not be blocked
     */
    @Transactional(readOnly = true)
    public LoginResponse loginUser(LoginDTO loginDTO){
        log.info("Login attempt for user: {}", loginDTO.getEmail());

        // Find user by email
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {}", loginDTO.getEmail());
                    return new IllegalArgumentException("Invalid email or password.");
                });

        // Verify password
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPasswordHash())){
            log.warn("Login failed - wrong password for user: {}", loginDTO.getEmail());
            throw new IllegalArgumentException("Invalid email or password.");
        }

        // Check if account is activated
        if(!user.isActivated()){
            log.warn("Login failed - account not activated: {}", loginDTO.getEmail());
            throw new UserNotActivatedException(
                    "Your account is not activated. Please check your email for the activation link."
            );
        }

        // Check if account is blocked
        if (user.isBlocked()){
            log.warn("Login failed - account blocked: {}", loginDTO.getEmail());
            String message = "Your account has been blocked";
            if (user.getBlockNote() != null && !user.getBlockNote().isEmpty()){
                message += " Reason: " + user.getBlockNote();
            }
            throw new UserBlockedException(message);
        }

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().toString());

        // If user is a diver, handle driver specific login logic
        if (user.getRole() == Role.DRIVER){
            handleDriverLogin(user.getId());
        }

        log.info("User logged in successfully: {}", loginDTO.getEmail());

        // Build and return login response
        LoginResponse response = new LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().toString()
        );

        // Add driver specific info if applicable
        if (user.getRole() == Role.DRIVER){
            Optional<Driver> driverOpt = driverRepository.findByUserId(user.getId());
            if(driverOpt.isPresent()){
                Driver driver = driverOpt.get();
                response.setDriver(true);
                response.setWorkingHours(driver.getWorkingMinutesLast24Hours());
            }
        }

        return response;
    }

    /**
     *
     * Handle driver specific login logic (US#2.2.1 - Driver availability on login)
     * Requirements:
     * - Driver becomes available on login
     * - Driver must not have active ride
     * - Driver must not exceed 8 hours work limit
     */
    @Transactional
    public void handleDriverLogin(Long userId) {
        log.info("Handling driver login for user ID: {}", userId);

        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found."));

        // Reset working hours if 24 hours have passed
        driver.resetWorkingHoursIfNeeded();

        // Mark driver as logged in
        driver.setLoggedIn(true);

        // Set active and available (unless they have active ride or exceeded hours)
        if(!driver.isHasActiveRide() && !driver.hasExceededWorkingHours()){
            driver.setActive(true);
            driver.setAvailable(true);
        }

        driverRepository.save(driver);
        log.info("Driver logged in and set to available: {}", userId);
    }


    /**
     * Register a new user
     */
    @Transactional
    public User registerUser(RegistrationDTO registrationDTO){
        log.info("Attempting to register user with email: {}", registrationDTO.getEmail());

        // Validate password confirmation
        if(!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        // Check if email already exists
        if(userRepository.existsByEmail(registrationDTO.getEmail())){
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        // Hash the password using Bcrypt
        String hashedPassword = passwordEncoder.encode(registrationDTO.getPassword());

        // Create new user
        User user = new User(
                registrationDTO.getFirstName(),
                registrationDTO.getLastName(),
                registrationDTO.getEmail(),
                hashedPassword,
                registrationDTO.getPhoneNumber(),
                registrationDTO.getAddress(),
                Role.USER
        );

        // Set default profile picture if not provided
        if (user.getProfilePicture() == null || user.getProfilePicture().isEmpty()){
            user.setProfilePicture("/images/default-avatar.png");
        }

        // Generate activation token valid for 24h
        String activationToken = UUID.randomUUID().toString();
        user.setActivationToken(activationToken);
        user.setTokenExpirationDate(LocalDateTime.now().plusHours(24));

        // Save user to database
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // Send activation email asynchronously
        emailService.sendActivationEmail(
                savedUser.getEmail(),
                savedUser.getFirstName(),
                activationToken
        );

        return savedUser;
    }

    /**
     * Activate user account with token
     * - Validates activation token
     * - Checks token expiration (24 hours)
     * - Activates user account
     */
    @Transactional
    public void activateAccount(String token){
        log.info("Attempting to activate with token: {}", token);

        User user = userRepository.findByActivationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Some message"));

        // Check if already activated
        if (user.isActivated()){
            throw new IllegalStateException("Account is already activated.");
        }

        // Check token expiration (24 hours)
        if (user.isActivationTokenExpired()){
            throw new IllegalStateException("Activation token has expired. Please request a new activation email.");
        }

        // Activate the account
        user.setActivated(true);
        user.setActivationToken(null);
        user.setTokenExpirationDate(null);

        userRepository.save(user);
        log.info("Account activated successfully for user: {}", user.getEmail());
    }

    /**
     * Resend activation email
     * - Generate new 24 hour token
     * - Sends new activation email
     *
     */
    @Transactional
    public void resendActivationEmail(String email){
        log.info("Attempting to resend activation email to: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // Check if already activated
        if (user.isActivated()){
            throw new IllegalStateException("Account is already activated.");
        }

        // Generate activation token valid for 24h
        String activationToken = UUID.randomUUID().toString();
        user.setActivationToken(activationToken);
        user.setTokenExpirationDate(LocalDateTime.now().plusHours(24));

        userRepository.save(user);

        // Send new activation email
        emailService.sendActivationEmail(
                user.getEmail(),
                user.getFirstName(),
                activationToken
        );

        log.info("Activation email resent successfully to: {}", email);
    }

    /**
     * Check if user can login
     * User cannot login if:
     * - Account is not activated
     * - Account is blocked by admin
     */
    public void validateUserCanLogin(User user){
        if(!user.isActivated()){
            throw new UserNotActivatedException(
                    "Your account is not activated. Please check your email for the activation link."
            );
        }

        if (user.isBlocked()){
            String message = "Your account has been blocked.";
            if (user.getBlockNote() != null && !user.getBlockNote().isEmpty()){
                message += " Reason: " + user.getBlockNote();
            }
            throw new UserNotActivatedException(message);
        }
    }
}
