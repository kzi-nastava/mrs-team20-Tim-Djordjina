package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.VehicleType;

/* Vehicle details supplied when an admin registers a driver
* */
@Data
public class VehicleDTO {

    @NotBlank(message = "Vehicle model is required")
    private String model;

    @NotNull(message = "Vehicle type is required (STANDARD, LUXURY, or VAN)")
    private VehicleType vehicleType;

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @Min(value = 1, message = "Vehicle must have at least 1 seat")
    private int seats;

    private boolean babyTransport;

    private boolean petTransport;

}
