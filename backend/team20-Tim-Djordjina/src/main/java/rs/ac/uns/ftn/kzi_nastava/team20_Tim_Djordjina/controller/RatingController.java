package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RatingDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.RatingService;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/{id}/rating")
    public ResponseEntity<RatingDTO> rate(Authentication authentication,
                                          @PathVariable Long id,
                                          @RequestBody RatingDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ratingService.rateRide(id, authentication.getName(), dto));
    }
}
