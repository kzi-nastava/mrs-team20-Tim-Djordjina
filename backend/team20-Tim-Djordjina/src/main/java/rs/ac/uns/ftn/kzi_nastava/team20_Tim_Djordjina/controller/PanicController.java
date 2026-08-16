package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.PanicDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.PanicRequestDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.PanicService;

/**
 * Trigger a panic alert during a ride (US#2.6.3)
 * */
@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class PanicController {

    private final PanicService panicService;

    @PostMapping("/{id}/panic")
    public ResponseEntity<PanicDTO> panic(Authentication authentication,
                                          @PathVariable Long id,
                                          @RequestBody(required = false) PanicRequestDTO dto) {
        String note = dto != null ? dto.getNote() : null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(panicService.triggerPanic(id, authentication.getName(), note));
    }
}
