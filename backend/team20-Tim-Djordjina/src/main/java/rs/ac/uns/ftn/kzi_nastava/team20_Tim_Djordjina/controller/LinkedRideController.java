package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideResponseDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.LinkedPassengerService;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class LinkedRideController {

    private final LinkedPassengerService linkedPassengerService;

    @GetMapping("/linked")
    public ResponseEntity<List<RideResponseDTO>> linkedRides(Authentication authentication) {
        return ResponseEntity.ok(linkedPassengerService.getLinkedRides(authentication.getName()));
    }
}
