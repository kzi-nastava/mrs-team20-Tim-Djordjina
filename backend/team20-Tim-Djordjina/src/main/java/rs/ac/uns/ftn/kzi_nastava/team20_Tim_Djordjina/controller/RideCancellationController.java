package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.ApiResponse;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.CancelRideRequestDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.RideCancellationService;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideCancellationController {

    private final RideCancellationService rideCancellationService;

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse> cancel(Authentication authentication,
                                              @PathVariable Long id,
                                              @RequestBody(required = false) CancelRideRequestDTO body) {
        String reason = body != null ? body.getReason() : null;
        rideCancellationService.cancelRide(id, authentication.getName(), reason);
        return ResponseEntity.ok(ApiResponse.success("Ride cancelled"));
    }
}
