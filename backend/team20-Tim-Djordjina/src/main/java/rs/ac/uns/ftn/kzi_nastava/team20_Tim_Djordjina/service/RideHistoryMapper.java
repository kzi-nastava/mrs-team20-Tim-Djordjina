package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideHistoryDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideStopDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Ride;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.RideStop;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps a Ride to a RideHistoryDTO.
 * Shared by the ride-history view (US#2.9) and the admin ride-state view (US#2.13).
 * */
@Component
public class RideHistoryMapper {

    public RideHistoryDTO toDto(Ride ride) {
        List<RideStopDTO> stops = new ArrayList<>();
        for (RideStop s : ride.getStops()) {
            RideStopDTO sd = new RideStopDTO();
            sd.setAddress(s.getAddress());
            sd.setLatitude(s.getLatitude());
            sd.setLongitude(s.getLongitude());
            sd.setStopOrder(s.getStopOrder());
            stops.add(sd);
        }

        RideHistoryDTO dto = new RideHistoryDTO();
        dto.setId(ride.getId());
        dto.setStatus(ride.getStatus().name());
        dto.setPickupAddress(ride.getPickupAddress());
        dto.setDestinationAddress(ride.getDestinationAddress());
        dto.setStops(stops);
        dto.setDistanceKm(ride.getDistanceKM());
        dto.setFare(ride.getFare());
        dto.setVehicleType(ride.getVehicleType() != null ? ride.getVehicleType().name() : null);
        dto.setScheduledFor(ride.getScheduledFor());
        dto.setStartedAt(ride.getStartedAt());
        dto.setFinishedAt(ride.getFinishedAt());
        dto.setCreatedAt(ride.getCreatedAt());

        if (ride.getDriver() != null && ride.getDriver().getUser() != null) {
            User d = ride.getDriver().getUser();
            dto.setDriver(new RideHistoryDTO.PartyInfo(
                    ride.getDriver().getId(), d.getFirstName(), d.getLastName()));
        }
        if (ride.getRider() != null) {
            User r = ride.getRider();
            dto.setRider(new RideHistoryDTO.PartyInfo(
                    r.getId(), r.getFirstName(), r.getLastName()));
        }
        return dto;

    }
}
