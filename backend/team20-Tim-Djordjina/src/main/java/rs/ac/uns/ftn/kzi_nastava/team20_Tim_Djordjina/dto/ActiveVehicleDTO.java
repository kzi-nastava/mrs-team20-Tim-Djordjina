package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A currently-active vehicle for the public home map (US#2.1.1) */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveVehicleDTO {
    private Long vehicleId;
    private double latitude;
    private double longitude;
    private boolean busy;
    private String vehicleType;
    private String model;
}
