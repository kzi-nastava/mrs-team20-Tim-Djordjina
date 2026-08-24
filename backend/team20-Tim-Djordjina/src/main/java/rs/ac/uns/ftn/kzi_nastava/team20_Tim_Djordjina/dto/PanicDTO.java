package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Panic record for the admin view (US#2.6.3)
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PanicDTO {
    private Long id;
    private Long rideId;
    private String rideStatus;
    private String pickupAddress;
    private String destinationAddress;
    private String triggeredByName;
    private String triggeredByEmail;
    private String triggeredByRole;
    private String note;
    private LocalDateTime createdAt;
}
