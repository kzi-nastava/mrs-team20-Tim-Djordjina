package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideResponseDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.LinkedPassenger;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.NotificationType;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Ride;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.LinkedPassengerRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LinkedPassengerService {

    private final UserRepository userRepository;
    private final LinkedPassengerRepository linkedPassengerRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    /** Create links for the given emails, send each an added notification. */
    @Transactional
    public void linkPassengers(Ride ride, List<String> emails) {
        if (emails == null || emails.isEmpty()) return;
        User creator = ride.getRider();

        for(String raw : emails) {
            if (raw == null) continue;
            String email = raw.trim().toLowerCase();
            if (emails.isEmpty()) continue;
            if (creator != null && email.equalsIgnoreCase(creator.getEmail())) continue;

            LinkedPassenger lp = new LinkedPassenger();
            lp.setRide(ride);
            lp.setEmail(email);
            userRepository.findByEmail(email).ifPresent(lp::setUser);
            linkedPassengerRepository.save(lp);

            String msg = "You were added as a passenger on a ride from "
                    + ride.getPickupAddress() + " to " + ride.getDestinationAddress() + ".";
            notify(lp, "Added to a RideOn ride", msg, NotificationType.RIDE_ACCEPTED, ride.getId());
        }
    }

    /** Notify linked passengers that the ride was accepted */
    @Transactional
    public void notifyAccepted(Ride ride) {
        String msg = "Your linked ride has been accepted and a driver is on the way";
        for (LinkedPassenger lp : linkedPassengerRepository.findByRideId(ride.getId())) {
            notify(lp, "Your RideOn ride is confirmed", msg, NotificationType.RIDE_ACCEPTED, ride.getId());
        }
    }

    /** Notify linked passengers that the ride finished */
    @Transactional
    public void notifyFinished(Ride ride) {
        String msg = "Your linked ride has finished. Thanks for riding with RideOn.";
        for (LinkedPassenger lp : linkedPassengerRepository.findByRideId(ride.getId())) {
            notify(lp, "Your RideOn ride has finished", msg, NotificationType.RIDE_FINISHED, ride.getId());
        }
    }

    @Transactional(readOnly = true)
    public List<RideResponseDTO> getLinkedRides(String email) {
        return linkedPassengerRepository.findByEmail(email.toLowerCase()).stream()
                .map(LinkedPassenger::getRide)
                .map(this::toResponse)
                .toList();
    }

    // ---------- Helpers ----------

    private void notify(LinkedPassenger lp, String subject, String message,
                        NotificationType type, Long rideId) {
        emailService.sendNotificationEmail(lp.getEmail(), subject, message);
        if (lp.getUser() != null) {
            notificationService.create(lp.getUser(), type, message, rideId);
        }
    }

    private RideResponseDTO toResponse(Ride ride) {
        RideResponseDTO dto = new RideResponseDTO();
        dto.setId(ride.getId());
        dto.setStatus(ride.getStatus().name());
        dto.setPickupAddress(ride.getPickupAddress());
        dto.setDestinationAddress(ride.getDestinationAddress());
        dto.setDistanceKm(ride.getDistanceKM());
        dto.setFare(ride.getFare());
        dto.setVehicleType(ride.getVehicleType() != null ? ride.getVehicleType().name() : null);
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
