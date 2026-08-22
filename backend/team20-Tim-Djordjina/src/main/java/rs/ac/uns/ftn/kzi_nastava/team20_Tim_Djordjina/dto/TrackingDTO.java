package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Live tracking for a rider's active ride (US#2.6.2) */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackingDTO {
    private Double vehicleLatitude;
    private Double vehicleLongitude;
    private long etaMinutes;
    private String status;
    private double pickupLatitude;
    private double pickupLongitude;
    private double destinationLatitude;
    private double destinationLongitude;
    private String driverName;
    private String vehicleModel;
    private String licensePlate;
}
