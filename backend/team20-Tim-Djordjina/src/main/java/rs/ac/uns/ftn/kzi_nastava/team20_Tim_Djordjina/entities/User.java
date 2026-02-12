/*
package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.enums.UserRole;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String address;
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @JsonProperty(value = "isActivated")
    private boolean activated = false;
    @JsonProperty(value = "isBlocked")
    private boolean blocked = false;
    private String blockNote;

    @Column(unique = true)
    private String profileImageUrl;

    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    private Long supportChatId;
}*/
