package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto;

import lombok.Data;

import java.util.List;

/** Response for a created ride */
@Data
public class RideResponseDTO {

    private Long id;
    private String status;
    private double distanceKm;
    private double fare;

    private String pickupAddress;
    private String destinationAddress;
    private List<RideStopDTO> stops;

    private String vehicleType;
    private boolean babyTransport;
    private boolean petTransport;

    // null until driver is assigned
    private DriverInfo driver;

    private String message;

    @Data
    public static class DriverInfo {
        private Long driverId;
        private String firstName;
        private String lastName;
        private String vehicleModel;
        private String licensePlate;
    }
}
