package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideHistoryDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.RideHistoryService;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideHistoryController {

    private final RideHistoryService rideHistoryService;

    @GetMapping("/history")
    public ResponseEntity<List<RideHistoryDTO>> history(Authentication authentication) {
        return ResponseEntity.ok(rideHistoryService.getHistory(authentication.getName()));
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<RideHistoryDTO> detail(Authentication authentication,
                                                 @PathVariable Long id) {
        return ResponseEntity.ok(rideHistoryService.getDetail(id, authentication.getName()));
    }
}
