package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name="profile_change_requests")
@Data
public class ProfileChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user with the role DRIVER requesting the change
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String profilePicture;

    @Enumerated(EnumType.STRING)
    private ProfileChangeStatus status = ProfileChangeStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime resolvedAt;
}
