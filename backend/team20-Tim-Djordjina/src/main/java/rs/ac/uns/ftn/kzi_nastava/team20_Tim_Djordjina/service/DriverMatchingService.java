package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Driver;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Vehicle;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.VehicleType;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.DriverRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.VehicleRepository;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

/** Chooses a driver for a ride request */
@Service
@RequiredArgsConstructor
@Slf4j
public class DriverMatchingService {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final DistanceCalculator distanceCalculator;

    /** A driver together with their vehicle */
    private record Candidate(Driver driver, Vehicle vehicle) {}

    /** @return the nearest eligible free driver whose vehicle matches the
     * request, or empty if none is available */
    public Optional<Driver> findNearestAvailableDriver(VehicleType vehicleType,
                                                       boolean babyTransport,
                               boolean petTransport,
                               double pickupLat,
                               double pickupLng){
        Optional<Driver> chosen = driverRepository.findAll().stream()
                .filter(Driver::canAcceptRides)
                .map(this::withVehicle)
                .filter(Objects::nonNull)
                .filter(c -> vehicleMatches(c.vehicle(), vehicleType, babyTransport, petTransport))
                .min(Comparator.comparingDouble(c -> distanceToPickup(c.vehicle(), pickupLat, pickupLng)))
                .map(Candidate::driver);

        chosen.ifPresentOrElse(
                d -> log.info("Matched driver {} for ride request", d.getId()),
                () -> log.info("No eligible driver found for the ride request")
        );
        return chosen;
    }

    private Candidate withVehicle(Driver driver) {
        return vehicleRepository.findByDriverId(driver.getId())
                .map(v -> new Candidate(driver, v))
                .orElse(null);
    }

    private boolean vehicleMatches(Vehicle v, VehicleType type, boolean baby, boolean pet) {
        if (v.getVehicleType() != type) return false;
        if (baby && !v.isBabyTransport()) return false;
        if (pet && !v.isPetTransport()) return false;
        return true;
    }

    /** Distance from the vehicle's current position to the pickup, in km */
    private double distanceToPickup(Vehicle v, Double pickupLat, Double pickupLng) {
        if (v.getCurrentLatitude() == null || v.getCurrentLongitude() == null) {
            // Unknown location -> rank last, but still assignable if it's the only option
            return Double.MAX_VALUE;
        }
        return distanceCalculator.distanceKm(
                v.getCurrentLatitude(), v.getCurrentLongitude(), pickupLat, pickupLng);
    }
}
