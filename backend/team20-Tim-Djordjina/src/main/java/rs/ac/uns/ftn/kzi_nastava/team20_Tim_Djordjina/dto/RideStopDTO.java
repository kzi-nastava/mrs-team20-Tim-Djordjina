package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** An ordered intermediate stop, used in ride request and response */
@Data
public class RideStopDTO {

    @NotBlank(message = "Stop address is required")
    private String address;

    @NotNull(message = "Stop latitude is required")
    private String latitude;

    @NotNull(message = "Stop longitude is required")
    private String longitude;

    // Position between pickup and destination; order matters
    private int stopOrder;
}
