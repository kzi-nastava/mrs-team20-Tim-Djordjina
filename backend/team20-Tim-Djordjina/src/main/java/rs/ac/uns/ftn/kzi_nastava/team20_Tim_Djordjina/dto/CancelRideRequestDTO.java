package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto;

import lombok.Data;

/** Cancellation body: reason (required for a driver, optional for a rider)*/
@Data
public class CancelRideRequestDTO {
    private String reason;
}
