package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.ApiResponse;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.DriverActiveDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.DriverStateDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.DriverStateService;

@RestController
@RequestMapping("/api/drivers/me")
@RequiredArgsConstructor
public class DriverStateController {

    private final DriverStateService driverStateService;

    @GetMapping("/state")
    public ResponseEntity<DriverStateDTO> state(Authentication authentication) {
        return ResponseEntity.ok(driverStateService.getState(authentication.getName()));
    }

    @PostMapping("/active")
    public ResponseEntity<DriverStateDTO> setActive(Authentication authentication,
                                                    @RequestBody DriverActiveDTO dto) {
        return ResponseEntity.ok(driverStateService.setActive(authentication.getName(), dto.isActive()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(Authentication authentication) {
        driverStateService.logout(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Logged out."));
    }
}
