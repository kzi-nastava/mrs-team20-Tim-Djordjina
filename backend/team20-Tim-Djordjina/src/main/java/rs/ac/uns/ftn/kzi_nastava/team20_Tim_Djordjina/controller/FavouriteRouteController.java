package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.ApiResponse;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.FavouriteRouteDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.FavouriteRouteService;

import java.util.List;

/**
 * Favourite route endpoints (US#2.4.3)
 * */
@RestController
@RequestMapping("/api/favourite-routes")
@RequiredArgsConstructor
public class FavouriteRouteController {

    private final FavouriteRouteService favouriteRouteService;

    @PostMapping
    public ResponseEntity<FavouriteRouteDTO> create(Authentication authentication,
                                                    @RequestBody FavouriteRouteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(favouriteRouteService.save(authentication.getName(), dto));
    }

    @GetMapping
    public ResponseEntity<List<FavouriteRouteDTO>> list(Authentication authentication) {
        return ResponseEntity.ok(favouriteRouteService.list(authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(Authentication authentication,
                                              @PathVariable Long id) {
        favouriteRouteService.delete(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Favourite route removed."));
    }
}
