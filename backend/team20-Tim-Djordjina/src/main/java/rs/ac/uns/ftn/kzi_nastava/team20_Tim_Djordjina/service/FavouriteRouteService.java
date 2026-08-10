package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.FavouriteRouteDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideStopDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.FavouriteRoute;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.FavouriteRouteStop;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.FavouriteRouteRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Favourite routes (US#2.4.3): save, list and delete a user's saved routes
 * */
@Service
@RequiredArgsConstructor
@Slf4j
public class FavouriteRouteService {

    private final UserRepository userRepository;
    private final FavouriteRouteRepository favouriteRouteRepository;

    @Transactional
    public FavouriteRouteDTO save(String email, FavouriteRouteDTO dto) {
        validate(dto);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        FavouriteRoute route = new FavouriteRoute();
        route.setUser(user);
        route.setLabel(dto.getLabel());
        route.setPickupAddress(dto.getPickupAddress());
        route.setPickupLatitude(dto.getPickupLatitude());
        route.setPickupLongitude(dto.getPickupLongitude());
        route.setDestinationAddress(dto.getDestinationAddress());
        route.setDestinationLatitude(dto.getDestinationLatitude());
        route.setDestinationLongitude(dto.getDestinationLongitude());

        for (RideStopDTO s : sortedStops(dto.getStops())) {
            FavouriteRouteStop stop = new FavouriteRouteStop();
            stop.setAddress(s.getAddress());
            stop.setLatitude(s.getLatitude());
            stop.setLongitude(s.getLongitude());
            stop.setStopOrder(s.getStopOrder());
            route.addStop(stop);
        }

        FavouriteRoute saved = favouriteRouteRepository.save(route);
        log.info("Saved favourite route {} for user {}", saved.getId(), user.getId());
        return toDto(saved);
    }

    @Transactional
    public List<FavouriteRouteDTO> list(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return favouriteRouteRepository.findByUserIdOrderByIdDesc(user.getId())
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public void delete(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        FavouriteRoute route = favouriteRouteRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Favourite route not found"));
        favouriteRouteRepository.delete(route);
    }

    // ---------- Helpers ----------

    private void validate(FavouriteRouteDTO dto) {
        if (dto.getPickupAddress() == null
                || dto.getPickupLatitude() == null
                || dto.getPickupLongitude() == null
                || dto.getDestinationAddress() == null
                || dto.getDestinationLatitude() == null
                || dto.getDestinationLongitude() == null) {
            throw new IllegalArgumentException("Pickup and destination are required.");
        }
    }

    private List<RideStopDTO> sortedStops(List<RideStopDTO> stops) {
        List<RideStopDTO> result = new ArrayList<>();
        if (stops != null) {
            result.addAll(stops);
            result.sort(Comparator.comparingInt(RideStopDTO::getStopOrder));
        }
        return result;
    }

    private FavouriteRouteDTO toDto(FavouriteRoute route) {
        List<RideStopDTO> stops = new ArrayList<>();
        for (FavouriteRouteStop s : route.getStops()) {
            RideStopDTO sd = new RideStopDTO();
            sd.setAddress(s.getAddress());
            sd.setLatitude(s.getLatitude());
            sd.setLongitude(s.getLongitude());
            sd.setStopOrder(s.getStopOrder());
            stops.add(sd);
        }

        return new FavouriteRouteDTO(
                route.getId(),
                route.getLabel(),
                route.getPickupAddress(),
                route.getPickupLatitude(),
                route.getPickupLongitude(),
                route.getDestinationAddress(),
                route.getDestinationLatitude(),
                route.getDestinationLongitude(),
                stops
        );
    }
}


