package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideRequestDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideResponseDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideStopDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.NoAvailableDriverException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.UserBlockedException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.DriverRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.RideRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.VehicleRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

        // Distance over pickup -> ordered stops -> destination
        double distanceKm = round2(distanceCalculator.totalDistanceKm(buildRoute(dto)));

        double fare = round2(fareCalculationService.calculateFare(dto.getVehicleType(), distanceKm));

        // Match the nearest eligible free driver
        Driver driver = driverMatchingService.findNearestAvailableDriver(
                dto.getVehicleType(),
                dto.isBabyTransport(),
                dto.isPetTransport(),
                dto.getPickupLatitude(),
                dto.getPickupLongitude()
        ).orElseThrow(() -> new NoAvailableDriverException(
                "No active drivers are available right now. Please try again later."));

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

        Ride saved = rideRepository.save(ride);

        // Mark the driver busy
        driver.setHasActiveRide(true);
        driver.setCurrentRideId(saved.getId());
        driver.setAvailable(false);
        driverRepository.save(driver);

        log.info("Ride {} created for rider {} assigned to driver {}",
                saved.getId(), riderEmail, driver.getId());


        return toResponse(saved, "Ride request. A driver has been assigned.");
    }

    /** Ordered coordinate list: pickup -> stops (by order) -> destination */
    private List<double[]> buildRoute(RideRequestDTO dto) {
        List<double[]> points = new ArrayList<>();
        points.add(new double[] {dto.getPickupLatitude(), dto.getPickupLongitude()});
        for (RideStopDTO s : sortStops(dto.getStops())){
            points.add(new double[] {s.getLatitude(), s.getLongitude()});
        }
        points.add(new double[] {dto.getDestinationLatitude(), dto.getDestinationLatitude()});
        return points;
    }

    private List<RideStopDTO> sortStops(List<RideStopDTO> stops) {
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
