package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false, unique = true)
    private Driver driver;

    // Vehicle model (example: "Toyota Camry 2020")
    @Column(nullable = false, length = 100)
    private String model;

    // Vehicle type: STANDARD, LUXURY, VAN
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType vehicleType;

    // License plate number
    @Column(nullable = false, unique = true, length = 20)
    private String licensePlate;

    // Number of passenger seats
    @Column(nullable = false)
    private Integer seats;

    // Baby transport allowed
    @Column(nullable = false)
    private boolean babyTransport = false;

    // Pet transport allowed
    @Column(nullable = false)
    private boolean petTransport = false;

    // Current location (latitude, longitude) for tracking
    @Column
    private Double currentLatitude;

    @Column
    private Double currentLongitude;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Custom constructor with parameters
    public Vehicle(String model, VehicleType vehicleType, String licensePlate,
                   Integer seats, boolean babyTransport, boolean petTransport){
        this.model = model;
        this.vehicleType = vehicleType;
        this.licensePlate = licensePlate;
        this.seats = seats;
        this.babyTransport = babyTransport;
        this.petTransport = petTransport;
    }
}
