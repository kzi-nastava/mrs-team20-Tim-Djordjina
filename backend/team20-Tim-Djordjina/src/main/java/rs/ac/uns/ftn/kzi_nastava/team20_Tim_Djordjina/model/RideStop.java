package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** An ordered intermediate stop on a ride */
@Entity
@Table(name = "ride_stops")
@Getter
@Setter
public class RideStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;

    private String address;
    private Double latitude;
    private Double longitude;

    // 0-based (or 1-based) position in the route between pickup and destination
    private int stopOrder;
}
