package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavouriteRouteDTO {

    private Long id;
    private String label;

    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;

    private String destinationAddress;
    private Double destinationLatitude;
    private Double destinationLongitude;

    private List<RideStopDTO> stops;

}
