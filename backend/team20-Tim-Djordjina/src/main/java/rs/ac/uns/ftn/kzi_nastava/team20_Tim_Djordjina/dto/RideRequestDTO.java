package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.VehicleType;

import java.util.ArrayList;
import java.util.List;

/** Body for POST /api/rides  */
@Data
public class RideRequestDTO {

    @NotBlank(message = "Pickup address is required")
    private String pickupAddress;
    @NotNull(message = "Pickup latitude is required")
    private Double pickupLatitude;
    @NotNull(message = "Pickup longitude is required")
    private Double pickupLongitude;

    @NotBlank(message = "Destination address is required")
    private String destinationAddress;
    @NotNull(message = "Destination latitude is required")
    private Double destinationLatitude;
    @NotNull(message = "Destination longitude is required")
    private Double destinationLongitude;

    // Optional ordered intermediate stops
    @Valid
    private List<RideStopDTO> stops = new ArrayList<>();

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    private boolean babyTransport;
    private boolean petTransport;
}
