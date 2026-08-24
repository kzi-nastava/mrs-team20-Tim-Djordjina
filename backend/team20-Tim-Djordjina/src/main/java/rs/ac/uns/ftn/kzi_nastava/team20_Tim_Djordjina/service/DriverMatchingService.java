package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.DriverRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.RideRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.VehicleRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

/** Chooses a driver for a ride request */
@Service
@RequiredArgsConstructor
@Slf4j
public class DriverMatchingService {

    public static final double AVG_SPEED_KMH = 40.0;
    public static final double NEAR_FINISH_MINUTES = 10.0;

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final DistanceCalculator distanceCalculator;
    private final RideRepository rideRepository;

    /** A driver together with their vehicle */
    private record Candidate(Driver driver, Vehicle vehicle) {}

    /**
     * Two tier match. @return the chosen driver, or empty if none available
     * */
    public Optional<Driver> findDriverForRide(VehicleType vehicleType,
                                              boolean babyTransport,
                                              boolean petTransport,
                                              double pickupLat,
                                              double pickupLng){
        Optional<Driver> free = findNearestFreeDriver(
                vehicleType, babyTransport, petTransport, pickupLat, pickupLng);
        if (free.isPresent()) {
            log.info("Matched free driver {}", free.get().getId());
            return free;
        }

        Optional<Driver> busy = findNearestBusyFinishingDriver(
                vehicleType, babyTransport, petTransport, pickupLat, pickupLng);
        busy.ifPresentOrElse(
                d -> log.info("Matched busy driver {} (~{} min from finishing)", d.getId(), NEAR_FINISH_MINUTES),
                () -> log.info("No eligible driver (free or busy) for the request"));
        return busy;
    }

    // ---------- Tier 1 - free ----------

    private Optional<Driver> findNearestFreeDriver(VehicleType type, boolean baby, boolean pet,
                          double pickupLat, double pickupLng) {
        return driverRepository.findAll().stream()
                .filter(Driver::canAcceptRides)
                .map(this::withVehicle)
                .filter(Objects::nonNull)
                .filter(c -> vehicleMatches(c.vehicle(), type, baby, pet))
                .min(Comparator.comparingDouble(c -> distanceToPickup(c.vehicle(), pickupLat, pickupLng)))
                .map(Candidate::driver);
    }

    // ---------- Tier 2 - busy , ~10min from finishing ----------

    private Optional<Driver> findNearestBusyFinishingDriver(VehicleType type, boolean baby, boolean pet,
                                                            double pickupLat, double pickupLng) {
        return driverRepository.findAll().stream()
                .filter(this::isBusyButActive)
                .filter(d -> !hasScheduledFutureRide(d))
                .filter(this::isNearFinishing)
                .map(this::withVehicle)
                .filter(Objects::nonNull)
                .filter(c -> vehicleMatches(c.vehicle(), type, baby, pet))
                .min(Comparator.comparingDouble(c -> distanceToPickup(c.vehicle(), pickupLat, pickupLng)))
                .map(Candidate::driver);
    }

    private boolean isBusyButActive(Driver d) {
        return d.isLoggedIn()
                && d.isActive()
                && d.isHasActiveRide()
                && d.getWorkingMinutesLast24Hours() < 480
                && d.getUser() != null
                && d.getUser().canLogin();
    }

    private boolean hasScheduledFutureRide(Driver d) {
        return rideRepository.findByDriverIdOrderByCreatedAtDesc(d.getId()).stream()
                .anyMatch(r -> r.getScheduledFor() != null
                        && r.getScheduledFor().isAfter(LocalDateTime.now())
                        && r.getStatus() != RideStatus.FINISHED
                        && r.getStatus() != RideStatus.CANCELLED
                        && r.getStatus() != RideStatus.REJECTED);
    }

    private boolean isNearFinishing(Driver d) {
        if (d.getCurrentRideId() == null) return false;
        Ride ride = rideRepository.findById(d.getCurrentRideId()).orElse(null);
        if (ride == null
                || ride.getStatus() != RideStatus.IN_PROGRESS
                || ride.getStartedAt() == null) {
            return false;
        }
        double totalMinutes = estimateDurationMinutes(ride.getDistanceKM());
        double elapsedMinutes = Duration.between(ride.getStartedAt(),LocalDateTime.now()).toSeconds() / 60.0;
        double remaining = totalMinutes - elapsedMinutes;
        return remaining >= 0 && remaining < NEAR_FINISH_MINUTES;
    }

    private double estimateDurationMinutes(double distanceKm) {
        return distanceKm / AVG_SPEED_KMH * 60.0;
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
