package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideResponseDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.RideExecutionService;

/** Ride execution endpoints (US#2.6.1, US#2.7) */
@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideExecutionController {


    private final RideExecutionService rideExecutionService;

    @GetMapping("/current")
    public ResponseEntity<RideResponseDTO> currentRide(Authentication authentication) {
        return rideExecutionService.getCurrentRide(authentication.getName())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<RideResponseDTO> start(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(rideExecutionService.startRide(id, authentication.getName()));
    }

    @PostMapping("/{id}/finish")
    public ResponseEntity<RideResponseDTO> finish(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(rideExecutionService.finishRide(id, authentication.getName()));
    }
}
