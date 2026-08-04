package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.NotificationDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Notification;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.NotificationType;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.NotificationRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;

import java.util.List;

/** Persisted notifications: create on ride events, list, and mark read */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /** Creates a notification within the caller's transaction.
     *  Used for success events.*/
    @Transactional
    public Notification create(User recipient, NotificationType type,
                               String message, Long relatedRideId) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setType(type);
        n.setMessage(message);
        n.setRelatedRideId(relatedRideId);
        Notification saved = notificationRepository.save(n);
        log.info("Notification {} created for user {}", type, recipient.getId());
        return saved;
    }

    /** Creates a notification in a NEW transaction, so it survives even when
     * the caller's transaction rolls back.
     * Used for the "request failed" case.*/
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createInNewTransaction(User recipient, NotificationType type,
                           String message, Long relatedRideId) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setType(type);
        n.setMessage(message);
        n.setRelatedRideId(relatedRideId);
        notificationRepository.save(n);
        log.info("Notification {} (new tx) created for user {}", type, recipient.getId());
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> listForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toDto).toList();

    }

    @Transactional(readOnly = true)
    public long unreadCount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return notificationRepository.countByRecipientIdAndReadFalse(user.getId());
    }

    @Transactional
    public void markRead(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        // A user can only mark their own notifications; others get "not found"
        Notification n = notificationRepository.findByIdAndRecipientId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        n.setRead(true);
        notificationRepository.save(n);
    }

    private NotificationDTO toDto(Notification n) {
        return new NotificationDTO(
                n.getId(),
                n.getType().name(),
                n.getMessage(),
                n.getRelatedRideId(),
                n.isRead(),
                n.getCreatedAt()
        );
    }

}
