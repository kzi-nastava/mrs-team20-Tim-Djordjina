package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverRatingSummaryDTO {
    private Long driverId;
    private Double averageDriverRating;
    private Double averageVehicleRating;
    private long ratingCount;
}
