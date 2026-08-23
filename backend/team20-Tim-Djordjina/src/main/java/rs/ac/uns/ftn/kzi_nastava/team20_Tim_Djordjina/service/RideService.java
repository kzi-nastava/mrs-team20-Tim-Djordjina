package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideRequestDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideResponseDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideStopDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.NoAvailableDriverException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.RideStateException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.UserBlockedException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.DriverRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.RideRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.VehicleRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Creates a ride request
 * reject blocked riders (with note), compute distance over the ordered route,
 * compute the fare, match a driver, mark them busy, and persist */
@Service
@RequiredArgsConstructor
@Slf4j
public class RideService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final RideRepository rideRepository;
    private final DistanceCalculator distanceCalculator;
    private final FareCalculationService fareCalculationService;
    private final DriverMatchingService driverMatchingService;
    private final NotificationService notificationService;
    private final LinkedPassengerService linkedPassengerService;

    @Transactional
    public RideResponseDTO requestRide(String riderEmail, RideRequestDTO dto) {
        User rider = userRepository.findByEmail(riderEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // A block passenger cannot order a ride; show the admin's note.
        if (rider.isBlocked()) {
            String note = (rider.getBlockNote() != null && !rider.getBlockNote().isEmpty())
                    ? rider.getBlockNote()
                    : "Your account is blocked and cannot order rides.";
            throw new UserBlockedException(note);
        }

        if (dto.getScheduledFor() != null) {
            return scheduleRide(rider, dto);
        }

        if (rideRepository.existsByRiderIdAndStatusIn(
                rider.getId(), List.of(RideStatus.ASSIGNED, RideStatus.IN_PROGRESS))) {
            throw new RideStateException(
                    "You already have an active ride. Finish it before ordering another.");
        }

        // Distance over pickup -> ordered stops -> destination
        double distanceKm = round2(distanceCalculator.totalDistanceKm(buildRoute(dto)));

        double fare = round2(fareCalculationService.calculateFare(dto.getVehicleType(), distanceKm));

        // Match the nearest eligible free driver
        Driver driver = driverMatchingService.findDriverForRide(
                dto.getVehicleType(),
                dto.isBabyTransport(),
                dto.isPetTransport(),
                dto.getPickupLatitude(),
                dto.getPickupLongitude()
        ).orElseThrow(() -> {
            notificationService.createInNewTransaction(
                    rider,
                    NotificationType.RIDE_FAILED,
                    "No active drivers are available right now. Please try again later.",
                    null);
            return new NoAvailableDriverException(
                    "No active drivers are available right now. Please try again later.");
        });

        // Build and persist the ride
        Ride ride = new Ride();
        ride.setRider(rider);
        ride.setDriver(driver);
        ride.setPickupAddress(dto.getPickupAddress());
        ride.setPickupLatitude(dto.getPickupLatitude());
        ride.setPickupLongitude(dto.getPickupLongitude());
        ride.setDestinationAddress(dto.getDestinationAddress());
        ride.setDestinationLatitude(dto.getDestinationLatitude());
        ride.setDestinationLongitude(dto.getDestinationLongitude());
        ride.setVehicleType(dto.getVehicleType());
        ride.setBabyTransport(dto.isBabyTransport());
        ride.setPetTransport(dto.isPetTransport());
        ride.setDistanceKM(distanceKm);
        ride.setFare(fare);
        ride.setStatus(RideStatus.ASSIGNED);

        for (RideStopDTO s : sortedStops(dto.getStops())) {
            RideStop stop = new RideStop();
            stop.setAddress(s.getAddress());
            stop.setLatitude(s.getLatitude());
            stop.setLongitude(s.getLongitude());
            stop.setStopOrder(s.getStopOrder());
            ride.addStop(stop);
        }

        Ride saved = rideRepository.save(ride);

        // Mark the driver busy
        driver.setHasActiveRide(true);
        driver.setCurrentRideId(saved.getId());
        driver.setAvailable(false);
        driverRepository.save(driver);

        notificationService.create(driver.getUser(), NotificationType.NEW_RIDE,
                "You have a new ride from " + saved.getPickupAddress()
                        + " to " + saved.getDestinationAddress() + ".",
                saved.getId());

        notificationService.create(rider, NotificationType.RIDE_ACCEPTED,
                "Your ride has been accepted. Driver: "
                        + driver.getUser().getFirstName() + " " + driver.getUser().getLastName() + ".",
                saved.getId());

        linkedPassengerService.linkPassengers(saved, dto.getLinkedPassengerEmails());
        linkedPassengerService.notifyAccepted(saved);

        log.info("Ride {} created for rider {} assigned to driver {}",
                saved.getId(), riderEmail, driver.getId());


        return toResponse(saved, "Ride request. A driver has been assigned.");
    }

    @Transactional(readOnly = true)
    public RideResponseDTO getRiderCurrentRide(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Ride ride = rideRepository
                .findFirstByRiderIdAndStatusInOrderByCreatedAtDesc(
                        user.getId(),
                        List.of(RideStatus.ASSIGNED, RideStatus.IN_PROGRESS)
                ).orElse(null);
        return ride == null ? null : toResponse(ride, null);
    }

    private RideResponseDTO scheduleRide(User rider, RideRequestDTO dto) {
        LocalDateTime when = dto.getScheduledFor();
        LocalDateTime now = LocalDateTime.now();

        if (!when.isAfter(now)) {
            throw new IllegalArgumentException("Scheduled time must be in the future.");
        }
        if (when.isAfter(now.plusHours(5))) {
            throw new IllegalArgumentException("A ride can be scheduled at most 5 hours in advance.");
        }

        double distanceKm = round2(distanceCalculator.totalDistanceKm(buildRoute(dto)));
        double fare = round2(fareCalculationService.calculateFare(dto.getVehicleType(), distanceKm));

        Ride ride = new Ride();
        ride.setRider(rider);
        // no driver yet
        ride.setPickupAddress(dto.getPickupAddress());
        ride.setPickupLatitude(dto.getPickupLatitude());
        ride.setPickupLongitude(dto.getPickupLongitude());
        ride.setDestinationAddress(dto.getDestinationAddress());
        ride.setDestinationLatitude(dto.getDestinationLatitude());
        ride.setDestinationLongitude(dto.getDestinationLongitude());
        ride.setVehicleType(dto.getVehicleType());
        ride.setBabyTransport(dto.isBabyTransport());
        ride.setPetTransport(dto.isPetTransport());
        ride.setDistanceKM(distanceKm);
        ride.setFare(fare);
        ride.setStatus(RideStatus.SCHEDULED);
        ride.setScheduledFor(when);

        for(RideStopDTO s : sortedStops(dto.getStops())) {
            RideStop stop = new RideStop();
            stop.setAddress(s.getAddress());
            stop.setLatitude(s.getLatitude());
            stop.setLongitude(s.getLongitude());
            stop.setStopOrder(s.getStopOrder());
            ride.addStop(stop);
        }

        Ride saved = rideRepository.save(ride);

        notificationService.create(rider, NotificationType.RIDE_ACCEPTED,
                "Your ride is scheduled for " + when + ". A driver will be assigned closer to the time.",
                saved.getId());

        linkedPassengerService.linkPassengers(saved, dto.getLinkedPassengerEmails());

        return toResponse(saved, "Ride scheduled for " + when + ".");
    }


    /** Ordered coordinate list: pickup -> stops (by order) -> destination */
    private List<double[]> buildRoute(RideRequestDTO dto) {
        List<double[]> points = new ArrayList<>();
        points.add(new double[] {dto.getPickupLatitude(), dto.getPickupLongitude()});
        for (RideStopDTO s : sortedStops(dto.getStops())){
            points.add(new double[] {s.getLatitude(), s.getLongitude()});
        }
        points.add(new double[] {dto.getDestinationLatitude(), dto.getDestinationLongitude()});
        return points;
    }

    private List<RideStopDTO> sortedStops(List<RideStopDTO> stops) {
        if (stops == null) return List.of();
        List<RideStopDTO> copy = new ArrayList<>(stops);
        copy.sort(Comparator.comparingInt(RideStopDTO::getStopOrder));
        return copy;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private RideResponseDTO toResponse(Ride ride, String message) {
        RideResponseDTO dto = new RideResponseDTO();
        dto.setId(ride.getId());
        dto.setStatus(ride.getStatus().name());
        dto.setDistanceKm(ride.getDistanceKM());
        dto.setFare(ride.getFare());
        dto.setPickupAddress(ride.getPickupAddress());
        dto.setDestinationAddress(ride.getDestinationAddress());
        dto.setVehicleType(ride.getVehicleType().name());
        dto.setBabyTransport(ride.isBabyTransport());
        dto.setPetTransport(ride.isPetTransport());
        dto.setMessage(message);

        List<RideStopDTO> stopDTOs = new ArrayList<>();
        for (RideStop s : ride.getStops()) {
            RideStopDTO sd = new RideStopDTO();
            sd.setAddress(s.getAddress());
            sd.setLatitude(s.getLatitude());
            sd.setLongitude(s.getLongitude());
            sd.setStopOrder(s.getStopOrder());
            stopDTOs.add(sd);
        }
        dto.setStops(stopDTOs);

        if (ride.getDriver() != null) {
            Driver driver = ride.getDriver();
            RideResponseDTO.DriverInfo di = new RideResponseDTO.DriverInfo();
            di.setDriverId(driver.getId());
            if (driver.getUser() != null) {
                di.setFirstName(driver.getUser().getFirstName());
                di.setLastName(driver.getUser().getLastName());
            }
            vehicleRepository.findByDriverId(driver.getId()).ifPresent(v -> {
                di.setVehicleModel(v.getModel());
                di.setLicensePlate(v.getLicensePlate());
            });
            dto.setDriver(di);
        }

        return dto;
    }
}
