package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.ApiResponse;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.InconsistencyRequestDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.TrackingDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.RideTrackingService;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideTrackingController {

    private final RideTrackingService rideTrackingService;

    @GetMapping("/{id}/tracking")
    public ResponseEntity<TrackingDTO> tracking(Authentication authentication,
                                                @PathVariable Long id) {
        return ResponseEntity.ok(rideTrackingService.getTracking(id, authentication.getName()));
    }

    @PostMapping("/{id}/inconsistency")
    public ResponseEntity<ApiResponse> report(Authentication authentication,
                                              @PathVariable Long id,
                                              @RequestBody(required = false)InconsistencyRequestDTO body) {
        rideTrackingService.reportInconsistency(id, authentication.getName(),
                body != null ? body.getText() : null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Report submitted"));
    }
}
