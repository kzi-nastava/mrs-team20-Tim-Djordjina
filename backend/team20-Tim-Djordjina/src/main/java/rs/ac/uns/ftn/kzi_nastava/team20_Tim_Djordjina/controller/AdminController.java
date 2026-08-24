package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.ApiResponse;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.DriverRegistrationDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.DriverService;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DriverService driverService;


    /**
     * POST /api/admin/drivers
     **/
    @PostMapping("/drivers")
    public ResponseEntity<ApiResponse> registerDriver(@Valid @RequestBody DriverRegistrationDTO request) {
        driverService.registerDriverByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Driver registered. An email has been sent for them to set their password."));
    }
}
