package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** A ride request */
@Entity
@Table(name="rides")
@Getter
@Setter
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rider_id", nullable = false)
    private User rider;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    // Pickup
    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;

    // Destination
    private String destinationAddress;
    private Double destinationLatitude;
    private Double destinationLongitude;

    // Ordered intermediate stops
    @OneToMany(mappedBy = "ride", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stopOrder ASC")
    private List<RideStop> stops = new ArrayList<>();

    // Options that affect driver/vehicle choice
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;
    private boolean babyTransport;
    private boolean petTransport;

    private double distanceKM;
    private double fare;

    @Enumerated(EnumType.STRING)
    private RideStatus status = RideStatus.REQUESTED;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime scheduledFor;

    public void addStop(RideStop stop) {
        stop.setRide(this);
        this.stops.add(stop);
    }

}
