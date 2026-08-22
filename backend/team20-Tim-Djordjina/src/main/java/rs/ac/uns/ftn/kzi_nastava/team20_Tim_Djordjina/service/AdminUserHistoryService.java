package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.AdminRideHistoryDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideHistoryDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserHistoryService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final RideRepository rideRepository;
    private final RideHistoryMapper mapper;
    private final PanicRepository panicRepository;
    private final RatingRepository ratingRepository;

    @Transactional(readOnly = true)
    public List<AdminRideHistoryDTO> getUserHistory(Long userId, LocalDateTime from, LocalDateTime to) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Ride> rides;
        if (user.getRole() == Role.DRIVER) {
            Driver driver = driverRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Driver not found"));
            rides = rideRepository.findByDriverIdOrderByCreatedAtDesc(driver.getId());
        } else {
            rides = rideRepository.findByRiderIdOrderByCreatedAtDesc(userId);
        }

        List<AdminRideHistoryDTO> results = new ArrayList<>();
        for (Ride ride : rides) {
            if(!inRange(ride.getCreatedAt(), from, to)) continue;
            results.add(toEnriched(ride));
        }
        return results;
    }

    @Transactional(readOnly = true)
    public AdminRideHistoryDTO getRideFull(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));
        return toEnriched(ride);
    }



    // ---------- Helpers ----------

    private AdminRideHistoryDTO toEnriched(Ride ride) {
        RideHistoryDTO base = mapper.toDto(ride);

        boolean cancelled = ride.getStatus() == RideStatus.CANCELLED;
        boolean panic = panicRepository.existsByRideId(ride.getId());

        Integer driverRating = null;
        Integer vehicleRating = null;
        var rating = ratingRepository.findByRideId(ride.getId());
        if (rating.isPresent()) {
            driverRating = rating.get().getDriverRating();
            vehicleRating = rating.get().getVehicleRating();
        }

        // cancelledBy, cancelReason, inconsistentReports -> pending
        return new AdminRideHistoryDTO(base, cancelled, ride.getCancelledBy(), ride.getCancelReason(), panic,
                driverRating, vehicleRating, new ArrayList<>());
    }


    private boolean inRange(LocalDateTime createdAt, LocalDateTime from, LocalDateTime to) {
        if (createdAt == null) return true;
        if (from != null && createdAt.isBefore(from)) return false;
        if (to != null && createdAt.isAfter(to)) return false;
        return true;
    }
}
