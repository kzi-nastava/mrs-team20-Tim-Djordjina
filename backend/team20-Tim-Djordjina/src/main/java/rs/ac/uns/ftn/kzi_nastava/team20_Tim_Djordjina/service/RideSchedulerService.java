package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Driver;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.NotificationType;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Ride;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.RideStatus;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.DriverRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.RideRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Activates scheduled rides at their due time
 * */
@Service
@RequiredArgsConstructor
@Slf4j
public class RideSchedulerService {

    private final RideRepository rideRepository;
    private final DriverMatchingService driverMatchingService;
    private final DriverRepository driverRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedRateString = "${ride.scheduler.interval-ms}")
    @Transactional
    public void assignDueScheduledRides() {
        List<Ride> due = rideRepository
                .findByStatusAndScheduledForLessThanEqualOrderByScheduledForAsc(
                        RideStatus.SCHEDULED, LocalDateTime.now());

        if (due.isEmpty()) return;
        log.info("Scheduler: {} scheduled ride(s) due for assignment", due.size());

        for (Ride ride : due) {
            driverMatchingService.findDriverForRide(
                    ride.getVehicleType(),
                    ride.isBabyTransport(),
                    ride.isPetTransport(),
                    ride.getPickupLatitude(),
                    ride.getPickupLongitude())
                    .ifPresentOrElse(
                            driver -> activate(ride, driver),
                            () -> log.info("Scheduler: no driver available yet for ride {}", ride.getId()));
        }
    }

    private void activate(Ride ride, Driver driver) {
        ride.setDriver(driver);
        ride.setStatus(RideStatus.ASSIGNED);
        rideRepository.save(ride);

        driver.setHasActiveRide(true);
        driver.setCurrentRideId(ride.getId());
        driver.setAvailable(false);
        driverRepository.save(driver);

        notificationService.create(driver.getUser(), NotificationType.NEW_RIDE,
                "Your scheduled ride is now active: " + ride.getPickupAddress()
                        + " to " + ride.getDestinationAddress() + ".", ride.getId());
        notificationService.create(ride.getRider(), NotificationType.RIDE_ACCEPTED,
                "A driver has been assigned to your scheduled ride.", ride.getId());

        log.info("Scheduler: activated scheduler ride {} -> driver {}", ride.getId(), driver.getId());
    }
}
