package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.PanicDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.ForbiddenActionException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.RideStateException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.PanicRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.RideRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PanicService {

    private final UserRepository userRepository;
    private final RideRepository rideRepository;
    private final PanicRepository panicRepository;
    private final NotificationService notificationService;

    @Transactional
    public PanicDTO triggerPanic(Long rideId, String email, String note){
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
            throw new ForbiddenActionException("You are not part of this ride");
        }

        if (ride.getStatus() != RideStatus.ASSIGNED && ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new RideStateException("Panic can only be triggered during an active ride");
        }

        Panic panic = new Panic();
        panic.setRide(ride);
        panic.setTriggeredBy(user);
        panic.setNote(note);
        Panic saved = panicRepository.save(panic);

        notifyAdmins(ride, user);

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<PanicDTO> getAllPanics() {
        return panicRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toDto).toList();
    }

    // ---------- Helpers ----------

    private void notifyAdmins(Ride ride, User triggeredBy) {
        String message = "PANIC on ride #" + ride.getId() + " from"
                + triggeredBy.getFirstName() + " " + triggeredBy.getLastName();
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        for (User admin : admins) {
            notificationService.create(admin, NotificationType.PANIC, message, ride.getId());
        }
    }

    private PanicDTO toDto(Panic p) {
        Ride r = p.getRide();
        User u = p.getTriggeredBy();
        return new PanicDTO(
                p.getId(),
                r != null ? r.getId() : null,
                r != null ? r.getStatus().name() : null,
                r != null ? r.getPickupAddress() : null,
                r != null ? r.getDestinationAddress() : null,
                u != null ? (u.getFirstName() + " " + u.getLastName()) : null,
                u != null ? u.getEmail() : null,
                u != null ? u.getRole().name() : null,
                p.getNote(),
                p.getCreatedAt()
        );

    }
}
