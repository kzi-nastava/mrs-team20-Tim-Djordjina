package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.ApiResponse;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.ProfileChangeRequestDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.ProfileService;

import java.util.List;

/**
 * Admin review of driver profile change requests
 */
@RestController
@RequestMapping("/api/admin/profile-changes")
@RequiredArgsConstructor
public class AdminProfileChangeController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<List<ProfileChangeRequestDTO>> listPending() {
        return ResponseEntity.ok(profileService.listPendingChanges());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse> approve(@PathVariable Long id) {
        profileService.approveChange(id);
        return ResponseEntity.ok(ApiResponse.success("Change request approved."));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse> reject(@PathVariable Long id) {
        profileService.rejectChange(id);
        return ResponseEntity.ok(ApiResponse.success("Change request rejected."));
    }
}
