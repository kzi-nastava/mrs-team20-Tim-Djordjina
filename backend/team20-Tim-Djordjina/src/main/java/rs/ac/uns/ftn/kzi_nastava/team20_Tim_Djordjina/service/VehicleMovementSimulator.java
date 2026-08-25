package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Ride;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.RideStatus;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Vehicle;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.RideRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.VehicleRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class VehicleMovementSimulator {

    private static final double SNAP_THRESHOLD_KM = 0.05;

    private final RideRepository rideRepository;
    private final VehicleRepository vehicleRepository;
    private final DistanceCalculator distanceCalculator;


    @Value("${simulation.step-fraction:0.2}")
    private double stepFraction;

    @Value("${simulation.enabled:true}")
    private boolean enabled;

    @Scheduled(fixedDelayString = "${simulation.tick-ms:3000}")
    @Transactional
    public void tick() {
        if (!enabled) return;

        List<Ride> rides = rideRepository.findByStatusIn(
                List.of(RideStatus.ASSIGNED, RideStatus.IN_PROGRESS));

        for (Ride ride : rides) {
            if (ride.getDriver() == null) continue;

            Vehicle v = vehicleRepository.findByDriverId(ride.getDriver().getId()).orElse(null);
            if (v == null || v.getCurrentLatitude() == null || v.getCurrentLongitude() == null) {
                continue;
            }

            double targetLat, targetLng;
            if (ride.getStatus() == RideStatus.IN_PROGRESS) {
                targetLat = ride.getDestinationLatitude();
                targetLng = ride.getDestinationLongitude();
            } else {    // ASSIGNED -> heading to pickup
                targetLat = ride.getPickupLatitude();
                targetLng = ride.getPickupLongitude();
            }

            double curLat = v.getCurrentLatitude();
            double curLng = v.getCurrentLongitude();
            double remainingKm = distanceCalculator.distanceKm(curLat, curLng, targetLat, targetLng);

            if (remainingKm <= SNAP_THRESHOLD_KM) {
                v.setCurrentLatitude(targetLat);
                v.setCurrentLongitude(targetLng);
            } else {
                v.setCurrentLatitude(curLat + (targetLat - curLat) * stepFraction);
                v.setCurrentLongitude(curLng + (targetLng - curLng) * stepFraction);
            }
            vehicleRepository.save(v);
        }
    }
}
