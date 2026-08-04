package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(length = 1000, nullable = false)
    private String message;

    // The ride this notification is about (nullable for failures with no ride)
    private Long relatedRideId;

    @Column(nullable = false)
    private boolean read = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

}
