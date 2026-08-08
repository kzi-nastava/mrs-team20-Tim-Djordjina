package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.RideReminderService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "reminder.check-interval-ms=3600000",
        "ride.scheduler.interval-ms=3600000",
        "reminder.lead-minutes=15",
        "reminder.interval-minutes=5"
})
@DisplayName("Ride Reminders - Integration Tests")
public class RideReminderIntegrationTest {

    public static final double PICKUP_LAT = 45.2671;
    public static final double PICKUP_LNG = 19.8335;
    private User rider;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private RideRepository rideRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RideReminderService rideReminderService;

    @BeforeEach
    void setUp() {
        cleanup();
        rider = new User("Sam", "Rider", "rider@test.com",
                "hash", "+381600000000", "Addr", Role.USER);
        rider.setActivated(true);
        rider = userRepository.save(rider);
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        notificationRepository.deleteAll();
        rideRepository.deleteAll();
        vehicleRepository.deleteAll();
        driverRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ---------- Tests ----------

    @Test
    @DisplayName("A ride within the window gets a first reminder")
    void firstReminder_sentForRideInWindow() throws Exception {
        Ride ride = seedScheduledRide(LocalDateTime.now().plusMinutes(10), null);

        rideReminderService.sendDueReminders();

        assertEquals(1, reminderCount(), "one reminder should be sent");
        Ride after = rideRepository.findById(ride.getId()).orElseThrow();
        assertNotNull(after.getLastReminderSentAt(), "lastReminderSentAt should be set");
    }

    @Test
    @DisplayName("No second reminder within the interval")
    void noSecondReminder_withinInterval() throws Exception {
        seedScheduledRide(LocalDateTime.now().plusMinutes(10), LocalDateTime.now().minusMinutes(2));

        rideReminderService.sendDueReminders();

        assertEquals(0, reminderCount(), "no reminder should be sent within the interval");
    }

    @Test
    @DisplayName("Another reminder after the interval has passed")
    void anotherReminder_afterInterval() throws Exception {
        Ride ride = seedScheduledRide(LocalDateTime.now().plusMinutes(10),
                LocalDateTime.now().minusMinutes(6));
        LocalDateTime previous = ride.getLastReminderSentAt();

        rideReminderService.sendDueReminders();

        assertEquals(1, reminderCount(), "a follow-up reminder should be sent");
    }

    @Test
    @DisplayName("No reminder for a ride outside the window")
    void noReminder_outsideWindow() throws Exception {
        seedScheduledRide(LocalDateTime.now().plusMinutes(30), null);

        rideReminderService.sendDueReminders();

        assertEquals(0, reminderCount(), "no reminder outside the 15-minute window");
    }

    @Test
    @DisplayName("No reminder for a non-scheduled (already started) ride")
    void noReminder_forNonScheduledRide() throws Exception {
        Ride r = seedScheduledRide(LocalDateTime.now().plusMinutes(10), null);
        r.setStatus(RideStatus.ASSIGNED);
        rideRepository.save(r);

        rideReminderService.sendDueReminders();

        assertEquals(0, reminderCount(), "started/assigned rides should not be reminded");
    }


    // ---------- Helpers ----------

    private long reminderCount() {
        return  notificationRepository.findByRecipientIdOrderByCreatedAtDesc(rider.getId()).stream()
                .filter(n -> n.getType() == NotificationType.RIDE_REMINDER)
                .count();
    }

    private Ride seedScheduledRide(LocalDateTime scheduledFor, LocalDateTime lastReminderSentAt) {
        Ride r = new Ride();
        r.setRider(rider);
        r.setPickupAddress("Trg slobode");
        r.setPickupLatitude(PICKUP_LAT);
        r.setPickupLongitude(PICKUP_LNG);
        r.setDestinationAddress("Strand");
        r.setDestinationLatitude(45.2400);
        r.setDestinationLongitude(19.8500);
        r.setVehicleType(VehicleType.STANDARD);
        r.setDistanceKM(2.0);
        r.setFare(440.0);
        r.setStatus(RideStatus.SCHEDULED);
        r.setScheduledFor(scheduledFor);
        r.setScheduledFor(scheduledFor);
        r.setLastReminderSentAt(lastReminderSentAt);
        return rideRepository.save(r);
    }
}
