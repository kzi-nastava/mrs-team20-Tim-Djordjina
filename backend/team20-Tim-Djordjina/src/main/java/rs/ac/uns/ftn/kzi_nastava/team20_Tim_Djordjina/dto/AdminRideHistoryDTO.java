package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminRideHistoryDTO {

    private RideHistoryDTO ride;

    private boolean cancelled;      // pending for cancellation (US#2.5)
    private String cancelledBy;
    private String cancelReason;
    private boolean panicTriggered;
    private Integer driverRating;
    private Integer vehicleRating;
    private List<String> inconsistencyReports;  // pending for ride tracking (US#2.6.2)
}
