package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.DriverRatingSummaryDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.RatingService;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverRatingController {

    private final RatingService ratingService;

    @GetMapping("/{driverId}/rating")
    public ResponseEntity<DriverRatingSummaryDTO> summary(@PathVariable Long driverId) {
        return ResponseEntity.ok(ratingService.getDriverSummary(driverId));
    }
}
