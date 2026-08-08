package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.NotificationType;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Ride;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.RideStatus;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.RideRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideReminderService {

    private final RideRepository rideRepository;
    private final NotificationService notificationService;

    @Value("${reminder.lead-minutes}")
    private long leadMinutes;

    @Value("${reminder.interval-minutes}")
    private long intervalMinutes;

    @Scheduled(fixedRateString = "${reminder.check-interval-ms}")
    @Transactional
    public void sendDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusMinutes(leadMinutes);

        List<Ride> upcoming = rideRepository.findByStatusAndScheduledForBetween(RideStatus.SCHEDULED, now, windowEnd);

        for (Ride ride : upcoming) {
            if (!ride.getScheduledFor().isAfter(now)) continue;
            if (shouldSend(ride, now)){
                sendReminder(ride, now);
            }
        }
    }

    /** First reminder as soon as it enters the window, the every intervalMiuntes. */
    private boolean shouldSend(Ride ride, LocalDateTime now) {
        if (ride.getLastReminderSentAt() == null) return true;
        return Duration.between(ride.getLastReminderSentAt(), now).toMinutes() >= intervalMinutes;
    }

    private void sendReminder(Ride ride, LocalDateTime now) {
        notificationService.create(ride.getRider(), NotificationType.RIDE_REMINDER,
                "Reminder: your scheduled ride from " + ride.getPickupAddress()
                        + " to " + ride.getDestinationAddress()
                        + " is at " + ride.getScheduledFor() + ".",
                ride.getId());
        ride.setLastReminderSentAt(now);
        rideRepository.save(ride);
        log.info("Reminder sent for scheduled ride {}", ride.getId());
    }
}
