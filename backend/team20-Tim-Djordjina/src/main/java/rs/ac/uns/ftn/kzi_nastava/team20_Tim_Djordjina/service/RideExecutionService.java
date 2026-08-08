package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideResponseDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideStopDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.ForbiddenActionException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.RideStateException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.DriverRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.RideRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Ride execution (US#2.6.1, US#2.7): start and finish a ride,
 * expose the driver's current ride. */
@Service
@RequiredArgsConstructor
@Slf4j
public class RideExecutionService {

    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final LinkedPassengerService linkedPassengerService;

    // ---------- Start ----------

    @Transactional
    public RideResponseDTO startRide(Long rideId, String driverEmail) {
        Ride ride = getRide(rideId);
        verifyAssignedDriver(ride, driverEmail);

        if (ride.getStatus() != RideStatus.ASSIGNED) {
            throw new RideStateException("Ride cannot be started from status " + ride.getStatus());
        }

        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartedAt(LocalDateTime.now());
        rideRepository.save(ride);

        log.info("Ride {} started by {}", rideId, driverEmail);
        return toResponse(ride, "Ride started.");
    }

    // ---------- Finish ----------

    @Transactional
    public RideResponseDTO finishRide(Long rideId, String driverEmail) {
        Ride ride = getRide(rideId);
        verifyAssignedDriver(ride, driverEmail);

        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new RideStateException("Ride cannot be finished from status " + ride.getStatus());
        }

        ride.setStatus(RideStatus.FINISHED);
        ride.setFinishedAt(LocalDateTime.now());
        rideRepository.save(ride);

        // Free the driver
        Driver driver = ride.getDriver();
        if (driver != null) {
            driver.setHasActiveRide(false);
            driver.setCurrentRideId(null);
            driver.setAvailable(true);
            driverRepository.save(driver);
        }

        // Notify the rider
        notificationService.create(ride.getRider(), NotificationType.RIDE_FINISHED,
                "Your ride has finished. Thank you for riding with RideOn.", ride.getId());

        linkedPassengerService.notifyFinished(ride);

        log.info("Ride {} finished by {}", rideId, driverEmail);
        return toResponse(ride, "Ride finished.");
    }

    // ---------- Current ride (for driver screen) ----------

    @Transactional(readOnly = true)
    public Optional<RideResponseDTO> getCurrentRide(String driverEmail) {
        User user = userRepository.findByEmail(driverEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Driver driver = driverRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));

        return rideRepository.findByDriverIdOrderByCreatedAtDesc(driver.getId()).stream()
                .filter(r -> r.getStatus() == RideStatus.ASSIGNED
                        || r.getStatus() == RideStatus.IN_PROGRESS)
                .findFirst()
                .map(r -> toResponse(r, null));
    }

    // ---------- Helpers ----------

    private Ride getRide(Long rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));
    }

    private void verifyAssignedDriver(Ride ride, String driverEmail) {
        if (ride.getDriver() == null
            || ride.getDriver().getUser() == null
            || !ride.getDriver().getUser().getEmail().equals(driverEmail)) {
            throw new ForbiddenActionException("You are not the assigned driver for this ride.");
        }
    }

    private RideResponseDTO toResponse(Ride ride, String message) {
        RideResponseDTO dto = new RideResponseDTO();
        dto.setId(ride.getId());
        dto.setStatus(ride.getStatus().name());
        dto.setDistanceKm(ride.getDistanceKM());
        dto.setFare(ride.getFare());
        dto.setPickupAddress(ride.getPickupAddress());
        dto.setDestinationAddress(ride.getDestinationAddress());
        dto.setVehicleType(ride.getVehicleType() != null ? ride.getVehicleType().name() : null);
        dto.setBabyTransport(ride.isBabyTransport());
        dto.setPetTransport(ride.isPetTransport());
        dto.setMessage(message);

        List<RideStopDTO> stops = new ArrayList<>();
        for (RideStop s : ride.getStops()) {
            RideStopDTO sd = new RideStopDTO();
            sd.setAddress(s.getAddress());
            sd.setLatitude(s.getLatitude());
            sd.setLongitude(s.getLongitude());
            sd.setStopOrder(s.getStopOrder());
            stops.add(sd);
        }
        dto.setStops(stops);

        if (ride.getDriver() != null && ride.getDriver().getUser() != null) {
            RideResponseDTO.DriverInfo di = new RideResponseDTO.DriverInfo();
            di.setDriverId(ride.getDriver().getId());
            di.setFirstName(ride.getDriver().getUser().getFirstName());
            di.setLastName(ride.getDriver().getUser().getLastName());
            dto.setDriver(di);
        }
        return dto;
    }

}
