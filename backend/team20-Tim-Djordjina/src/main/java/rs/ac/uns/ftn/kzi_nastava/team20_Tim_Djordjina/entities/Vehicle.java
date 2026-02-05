package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.enums.VehicleModel;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.enums.VehicleType;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;
    @Enumerated(EnumType.STRING)
    private VehicleModel  vehicleModel;

    @Column(nullable = false, unique = true)
    private String licensePlate;

    @Column(nullable = false)
    private Integer numberOfSeats;

    private Boolean babyTransport;
    private Boolean petTransport;
}
