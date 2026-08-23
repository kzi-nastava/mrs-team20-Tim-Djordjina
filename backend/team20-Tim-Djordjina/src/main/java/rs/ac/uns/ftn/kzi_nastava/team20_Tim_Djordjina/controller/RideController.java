package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideRequestDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideResponseDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.RideService;

/** Ride endpoints;
 *  Required authentication. */
@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

    @PostMapping
    public ResponseEntity<RideResponseDTO> requestRide(Authentication authentication,
                                                       @Valid @RequestBody RideRequestDTO request){
        RideResponseDTO response = rideService.requestRide(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/current/rider")
    public ResponseEntity<RideResponseDTO> riderCurrentRide(Authentication authentication) {
        RideResponseDTO ride = rideService.getRiderCurrentRide(authentication.getName());
        return ride == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(ride);
    }
}
