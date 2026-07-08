package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.AuthService;

/**
 * Authentication controller handling registration
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    /**
     *
     * Login user with email and password
     * POST /api/auth/login
     *
     * Returns JWT token if credentials are valid and account is activated
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginDTO loginDTO){
        LoginResponse loginResponse = authService.loginUser(loginDTO);
        return ResponseEntity
                .ok(ApiResponse.success("Login successfully", loginResponse));
    }

    /**
     * Logout driver and set unavailable
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestParam Long userId){
        authService.handleDriverLogout(userId);

        return ResponseEntity.ok(ApiResponse.success("Logout successful."));
    }

    /**
     * Toggle driver availability status
     * POST /api/auth/driver/toggle-availability
     *
     * Allows driver to manually set active/inactive status
     */
    @PostMapping("/driver/toggle-availability")
    public ResponseEntity<ApiResponse> toggleAvailability(@RequestParam Long userId){
        authService.toggleDriverAvailability(userId);

        return ResponseEntity.ok(ApiResponse.success("Availability status toggled."));
    }

    /*
    * Register a new user
    * POST /api/auth/register
    * */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegistrationDTO registrationDTO){
        User user = authService.registerUser(registrationDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Registration successful!",
                        user.getEmail()
                ));
    }

    /**
     * Activate user account
     * GET /api/auth/activate?token=xxx
     *
     * Activates account using token from email
     * Token expired after 24 hours
     */
    @GetMapping("/activate")
    public ResponseEntity<ApiResponse> activateAccount(@RequestParam String token){
        authService.activateAccount(token);

        return ResponseEntity.ok(ApiResponse.success("Account activated successfully! You can now login."));
    }

    /**
     * Resend activation email
     * POST /api/auth/resend-activation?email=xxx
     *
     * Generates new 24 hour token and resends activation email
     */
    @PostMapping("/resend-activation")
    public ResponseEntity<ApiResponse> resendActivationEmail(@RequestParam String email) {
        authService.resendActivationEmail(email);

        return ResponseEntity.ok(ApiResponse.success("Activation email has been resent. Please check your inbox. The link will expire in 24 hours."));
    }


    /**
     * POST /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody PasswordResetRequestDTO request){
        authService.requestPasswordReset(request.getEmail());

        return ResponseEntity.ok(ApiResponse.success("If an account with that email exists, a password reset link has been sent."));
    }

    /**
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody PasswordResetDTO request){
        authService.resetPassword(request);

        return ResponseEntity.ok(ApiResponse.success("Password reset successfully. Please log in with your new password."));
    }
}
