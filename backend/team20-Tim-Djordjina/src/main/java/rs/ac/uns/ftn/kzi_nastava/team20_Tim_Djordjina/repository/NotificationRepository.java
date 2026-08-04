package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    long countByRecipientIdAndReadFalse(Long recipientId);

    Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);
}
