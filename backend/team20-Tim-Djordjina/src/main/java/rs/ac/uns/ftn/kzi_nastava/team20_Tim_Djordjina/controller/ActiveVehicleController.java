package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.ActiveVehicleDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.ActiveVehicleService;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class ActiveVehicleController {

    private final ActiveVehicleService activeVehicleService;

    @GetMapping("/active")
    public ResponseEntity<List<ActiveVehicleDTO>> active() {
        return ResponseEntity.ok(activeVehicleService.getActiveVehicles());
    }
}
