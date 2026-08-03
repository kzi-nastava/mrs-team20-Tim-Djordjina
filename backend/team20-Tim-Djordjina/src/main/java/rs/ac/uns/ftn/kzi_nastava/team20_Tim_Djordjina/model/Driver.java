package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "drivers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // One-to-One relationship with User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    // Vehicle information
    @OneToOne(mappedBy = "driver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Vehicle vehicle;

    // Driver availability status
    @Column(nullable = false)
    private boolean isActive = false;    // Manual active/inactive toggle

    @Column(nullable = false)
    private boolean isAvailable = false; // Available for ride assignments

    // Track login status - Driver becomes available on login
    @Column(nullable = false)
    private boolean isLoggedIn = false;

    // Working hours tracking - cannot work more that 8h in 24h
    @Column
    private LocalDateTime lastWorkingHoursReset;

    @Column(nullable = false)
    private Integer workingMinutesLast24Hours = 0;  // working hours stored in minutes

    // Current ride information
    @Column
    private Long currentRideId;

    @Column(nullable = false)
    private boolean hasActiveRide = false;

    // Profile change requests - changes must be approved by admin
    @Column(nullable = false)
    private boolean hasPendingProfileChanges = false;

    @Column(length = 1000)
    private String pendingChangesDescription;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /*
    * Check if driver can accept new rides
    * Driver can't accept rides if:
    * - not logged in
    * - not active
    * - has active ride
    * - exceeded 8 working hours in last 24 hours
    * - user is blocked
    * */
    public boolean canAcceptRides(){
        return isLoggedIn
                && isActive
                && isAvailable
                && !hasActiveRide
                && workingMinutesLast24Hours < 480      // 8 hours = 480 minutes
                && user != null
                && user.canLogin();

    }

    /**
     * Check if driver has exceeded working hours
     */
    public boolean hasExceededWorkingHours() {
        return workingMinutesLast24Hours >= 480;
    }

    /**
     * Reset working hours if 24 hours have passed
     */
    public void resetWorkingHoursIfNeeded(){
        if (lastWorkingHoursReset == null ||
            LocalDateTime.now().isAfter(lastWorkingHoursReset.plusHours(24))){
            this.workingMinutesLast24Hours = 0;
            this.lastWorkingHoursReset = LocalDateTime.now();
        }
    }
}
