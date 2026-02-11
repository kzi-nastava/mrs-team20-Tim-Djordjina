package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.naming.StringRefAddr;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 50)
    private Role role = Role.USER;

    // Profile picture URL or path (default picture if not set)
    @Column(length = 500)
    private String profilePicture;

    @Column(nullable = false)
    private boolean isActivated = false;

    @Column(length = 255)
    private String activationToken;

    @Column
    private LocalDateTime tokenExpirationDate;

    @Column(length = 255)
    private String resetPasswordToken;

    @Column
    private LocalDateTime resetPasswordTokenExpirationDate;

    // Blocking functionality
    @Column(nullable = false)
    private boolean isBlocked = false;

    // Note left by administrator explaining why user is blocked
    @Column(length = 1000)
    private String blockNote;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Custom constructor for registration
    public User(String firstName, String lastName, String email, String passwordHash,
                String phoneNumber, String address, Role role){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
        this.isActivated = false;
        this.isBlocked = false;
    }

    // Check if user can login (activated and not blocked)
    public boolean canLogin(){
        return isActivated && !isBlocked;
    }
}
