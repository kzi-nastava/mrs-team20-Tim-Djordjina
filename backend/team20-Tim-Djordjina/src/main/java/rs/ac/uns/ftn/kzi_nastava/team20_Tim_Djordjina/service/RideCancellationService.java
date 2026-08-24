package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.ForbiddenActionException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.RideStateException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.DriverRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.RideRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RideCancellationService {

    private static final int RIDER_CANCEL_CUTOFF_MIN = 10;
    private final UserRepository userRepository;
    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;
    private final NotificationService notificationService;

    @Transactional
    public void cancelRide(Long rideId, String email, String reason) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));

        boolean isRider = ride.getRider() != null
                && ride.getRider().getId().equals(user.getId());
        boolean isDriver = ride.getDriver() != null
                && ride.getDriver().getUser() != null
                && ride.getDriver().getUser().getId().equals(user.getId());
        if (!isRider && !isDriver) {
            throw new ForbiddenActionException("You are not part of this ride.");
        }

        RideStatus status = ride.getStatus();
        if (isNotCancellableBeforeRideStarts(status)) {
            throw new RideStateException("This ride can no longer be cancelled.");
        }

        if (isDriver) {
            if (isReasonEmpty(reason)) {
                throw new IllegalArgumentException("A cancellation reason is required.");
            }
            ride.setCancelledBy("DRIVER");
            ride.setCancelReason(reason.trim());
        } else {
            if (ride.getScheduledFor() != null) {
                LocalDateTime cutoff = ride.getScheduledFor().minusMinutes(RIDER_CANCEL_CUTOFF_MIN);
                if (LocalDateTime.now().isAfter(cutoff)) {
                    throw new RideStateException("A ride can only be cancelled at least "
                        + RIDER_CANCEL_CUTOFF_MIN + " minutes before it starts");
                }
            }
            ride.setCancelledBy("RIDER");
            if (!isReasonEmpty(reason)) {
                ride.setCancelReason(reason.trim());
            }
        }

        ride.setStatus(RideStatus.CANCELLED);
        rideRepository.save(ride);

        // Free the driver if one was assigned
        Driver driver = ride.getDriver();
        if (driver != null) {
            driver.setHasActiveRide(false);
            driver.setAvailable(true);
            driver.setCurrentRideId(null);
            driverRepository.save(driver);
        }

        notifyOtherParty(ride, isDriver);
    }

    private void notifyOtherParty(Ride ride, boolean cancelledByDriver) {
        String msg = "Ride #" + ride.getId() + " was cancelled"
                + (ride.getCancelReason() != null ? ": " + ride.getCancelReason() : ".");
        if (cancelledByDriver) {
            if (ride.getRider() != null) {
                notificationService.create(ride.getRider(),
                        NotificationType.RIDE_CANCELLED, msg, ride.getId());
            }
        } else if (ride.getDriver() != null && ride.getDriver().getUser() != null) {
            notificationService.create(ride.getDriver().getUser(),
                    NotificationType.RIDE_CANCELLED, msg, ride.getId());
        }
    }

    private static boolean isReasonEmpty(String reason) {
        return reason == null || reason.trim().isEmpty();
    }

    private static boolean isNotCancellableBeforeRideStarts(RideStatus status) {
        return status == RideStatus.IN_PROGRESS || status == RideStatus.FINISHED
                || status == RideStatus.CANCELLED || status == RideStatus.REJECTED;
    }
}
