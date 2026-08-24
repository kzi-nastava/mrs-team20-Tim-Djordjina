package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.AdminRideHistoryDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.AdminUserHistoryService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Admin per user history (US#2.9.3)
 * */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserHistoryController {

    private final AdminUserHistoryService adminUserHistoryService;

    @GetMapping("/users/{userId}/history")
    public ResponseEntity<List<AdminRideHistoryDTO>> getUserHistory(
            @PathVariable Long userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(adminUserHistoryService.getUserHistory(userId, from, to));
    }

    @GetMapping("/rides/{rideId}/full")
    public ResponseEntity<AdminRideHistoryDTO> getRideFull(@PathVariable Long rideId) {
        return ResponseEntity.ok(adminUserHistoryService.getRideFull(rideId));
    }
}
