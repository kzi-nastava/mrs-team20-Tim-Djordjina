package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.ApiResponse;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.NotificationDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.NotificationService;

import java.util.List;

/** Notification endpoints */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> list(Authentication authentication) {
        return ResponseEntity.ok(notificationService.listForUser(authentication.getName()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> unreadCount(Authentication authentication) {
        return ResponseEntity.ok(notificationService.unreadCount(authentication.getName()));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse> markRead(Authentication authentication,
                                                @PathVariable Long id) {
        notificationService.markRead(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read."));
    }

}
