package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.ApiResponse;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.ChangePasswordDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.ProfileResponseDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.UpdateProfileDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.ProfileService;


/**
 * Current user profile endpoints.
 * All require authentication;
 * The email comes from the JWT via Authentication.getName().
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final ProfileService profileService;

    // GET /api/users/me -> returns the logged-in user's basic info
    @GetMapping("/me")
    public ResponseEntity<ProfileResponseDTO> getCurrentUser(Authentication authentication){
        return ResponseEntity.ok(profileService.getMyProfile(authentication.getName()));
    }

    // PUT /api/users/me
    @PutMapping("/me")
    public ResponseEntity<ApiResponse> updateProfile(Authentication authentication,
                                                     @Valid @RequestBody UpdateProfileDTO request) {
        boolean appliedDirectly = profileService.updateProfile(authentication.getName(), request);
        String message = appliedDirectly
                ? "Profile updated successfully."
                : "Your changes were submitted and awaiting administrator approval.";
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    // POST /api/users/me/change-password
    @PostMapping("/me/change-password")
    public ResponseEntity<ApiResponse> changePassword(Authentication authentication,
                                                      @Valid @RequestBody ChangePasswordDTO request) {
        profileService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully."));
    }
}
