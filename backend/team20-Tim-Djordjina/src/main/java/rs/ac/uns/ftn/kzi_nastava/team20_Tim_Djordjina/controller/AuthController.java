package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.ApiResponse;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.LoginDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.LoginResponse;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RegistrationDTO;
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
}
