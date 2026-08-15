package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideHistoryDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.AdminRideService;

import java.util.List;

/**
 * Admin ride state view
 * */
@RestController
@RequestMapping("/api/admin/rides")
@RequiredArgsConstructor
public class AdminRideController {

    private final AdminRideService adminRideService;

    @GetMapping
    public ResponseEntity<List<RideHistoryDTO>> getAll(@RequestParam(required = false) String status){
        return ResponseEntity.ok(adminRideService.getAllRides(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RideHistoryDTO> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(adminRideService.getRide(id));
    }
}
