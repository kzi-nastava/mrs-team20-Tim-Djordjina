package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverStateDTO {
    private boolean loggedIn;
    private boolean active;
    private boolean available;
    private boolean hasActiveRide;
    private Integer workingMinutesLast24Hours;
}
