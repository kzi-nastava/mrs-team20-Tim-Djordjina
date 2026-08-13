package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.DriverRatingSummaryDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RatingDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.ForbiddenActionException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.RideStateException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Rating;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Ride;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.RideStatus;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.RatingRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.RideRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {

    private static final int RATING_WINDOW_DAYS = 3;

    private final UserRepository userRepository;
    private final RideRepository rideRepository;
    private final RatingRepository ratingRepository;

    @Transactional
    public RatingDTO rateRide(Long rideId, String email, RatingDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));

        if (ride.getRider() == null || !ride.getRider().getId().equals(user.getId())) {
            throw new ForbiddenActionException("You can only rate your own ride.");
        }

        if (ride.getStatus() != RideStatus.FINISHED) {
            throw new RideStateException("You can only rate a finished ride.");
        }

        if (ride.getFinishedAt() == null
                || ride.getFinishedAt().plusDays(RATING_WINDOW_DAYS).isBefore(LocalDateTime.now())) {
            throw new RideStateException("The rating window (" + RATING_WINDOW_DAYS + " days) has passed.");
        }

        if (ratingRepository.existsByRideId(rideId)) {
            throw new RideStateException("The ride has already been rated.");
        }

        validateScore(dto.getDriverRating());
        validateScore(dto.getVehicleRating());

        Rating rating = new Rating();
        rating.setRide(ride);
        rating.setRater(user);
        rating.setDriverRating(dto.getDriverRating());
        rating.setVehicleRating(dto.getVehicleRating());
        rating.setComment(dto.getComment());
        Rating saved = ratingRepository.save(rating);

        return new RatingDTO(saved.getId(), ride.getId(),
                saved.getDriverRating(), saved.getVehicleRating(),
                saved.getComment(), saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public DriverRatingSummaryDTO getDriverSummary(Long driverId) {
        Double avgDriver = ratingRepository.averageDriverRating(driverId);
        Double avgVehicle = ratingRepository.averageVehicleRating(driverId);
        long count = ratingRepository.countByRide_Driver_Id(driverId);
        return new DriverRatingSummaryDTO(driverId, round(avgDriver), round(avgVehicle), count);
    }

    private void validateScore(Integer score) {
        if (score == null || score < 0 || score > 5) {
            throw new IllegalArgumentException("Ratings must be between 0 and 5.");
        }
    }

    private Double round(Double value) {
        if (value == null) return null;
        return Math.round(value * 100.0) / 100.0;
    }
}
