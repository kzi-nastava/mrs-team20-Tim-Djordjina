package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RegistrationDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.EmailAlreadyExistsException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.PasswordMismatchException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Role;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

/*
* Authentication service handling user registration
* */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    }
}
