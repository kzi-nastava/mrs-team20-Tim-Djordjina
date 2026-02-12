package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.ApiResponse;
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
}
