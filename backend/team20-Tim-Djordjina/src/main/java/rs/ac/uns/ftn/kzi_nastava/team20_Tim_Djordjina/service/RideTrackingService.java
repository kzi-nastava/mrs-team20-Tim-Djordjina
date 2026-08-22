package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.TrackingDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.ForbiddenActionException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.InconsistencyReportRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.RideRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class RideTrackingService {

    private static final double AVG_SPEED_KMH = 40.0;
    private final UserRepository userRepository;
    private final RideRepository rideRepository;
    private final DistanceCalculator distanceCalculator;
    private final InconsistencyReportRepository inconsistencyReportRepository;

    @Transactional(readOnly = true)
    public TrackingDTO getTracking(Long rideId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));

        requireRider(ride, user);

        Double vLat = null, vLng = null;
        String driverName = null, vehicleModel = null, plate = null;
        if (ride.getDriver() != null && ride.getDriver().getVehicle() != null) {
            Vehicle v = ride.getDriver().getVehicle();
            vLat = v.getCurrentLatitude();
            vLng = v.getCurrentLongitude();
            vehicleModel = v.getModel();
            plate = v.getLicensePlate();
            if (ride.getDriver().getUser() != null) {
                driverName = ride.getDriver().getUser().getFirstName() + " "
                        + ride.getDriver().getUser().getLastName();
            }
        }

        long etaMinutes = 0;
        if (vLat != null && vLng != null) {
            double targetLat, targetLng;
            if (ride.getStatus() == RideStatus.IN_PROGRESS) {
                targetLat = ride.getDestinationLatitude();
                targetLng = ride.getDestinationLongitude();
            } else {
                targetLat = ride.getPickupLatitude();
                targetLng = ride.getPickupLongitude();
            }
            double distKm = distanceCalculator.distanceKm(vLat, vLng, targetLat, targetLng);
            etaMinutes = Math.round((distKm / AVG_SPEED_KMH) * 60.0);
        }

        return new TrackingDTO(vLat, vLng, etaMinutes, ride.getStatus().name(),
                ride.getPickupLatitude(), ride.getPickupLongitude(),
                ride.getDestinationLatitude(), ride.getDestinationLongitude(),
                driverName, vehicleModel, plate);

    }

    @Transactional
    public void reportInconsistency(Long rideId, String email, String text) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));

        requireRider(ride, user);

        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Report text is required");
        }

        InconsistencyReport report = new InconsistencyReport();
        report.setRide(ride);
        report.setReporter(user);
        report.setText(text.trim());
        inconsistencyReportRepository.save(report);
    }

    // ---------- Helpers ----------
    private void requireRider(Ride ride, User user) {
        if (ride.getRider() == null || !ride.getRider().getId().equals(user.getId())) {
            throw new ForbiddenActionException("You are not part of this ride.");
        }
    }
}
