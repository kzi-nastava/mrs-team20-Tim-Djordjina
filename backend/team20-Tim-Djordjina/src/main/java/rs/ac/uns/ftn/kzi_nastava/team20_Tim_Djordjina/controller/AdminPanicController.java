package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.PanicDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.PanicService;

import java.util.List;

/**
 * Admin panic view (US#2.6.3)
 * */
@RestController
@RequestMapping("/api/admin/panics")
@RequiredArgsConstructor
public class AdminPanicController {

    private final PanicService panicService;

    @GetMapping
    public ResponseEntity<List<PanicDTO>> getAll() {
        return ResponseEntity.ok(panicService.getAllPanics());
    }
}
