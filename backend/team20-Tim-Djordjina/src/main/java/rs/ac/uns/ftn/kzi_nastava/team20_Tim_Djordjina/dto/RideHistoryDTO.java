package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A past ride for the history view (US#2.9)
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideHistoryDTO {

    private Long id;
    private String status;

    private String pickupAddress;
    private String destinationAddress;
    private List<RideStopDTO> stops;

    private double distanceKm;
    private double fare;
    private String vehicleType;

    private PartyInfo driver;
    private PartyInfo rider;

    private LocalDateTime scheduledFor;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartyInfo {
        private Long id;
        private String firstName;
        private String lastName;
    }
}
