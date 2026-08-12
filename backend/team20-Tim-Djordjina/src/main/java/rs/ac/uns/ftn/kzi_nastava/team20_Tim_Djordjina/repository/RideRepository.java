package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Ride;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.RideStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {

    List<Ride> findByRiderIdOrderByCreatedAtDesc(Long riderId);

    List<Ride> findByDriverIdOrderByCreatedAtDesc(Long driverId);

    List<Ride> findByStatus(RideStatus status);

    List<Ride> findByStatusAndScheduledForLessThanEqualOrderByScheduledForAsc(RideStatus status, LocalDateTime time);

    List<Ride> findByStatusAndScheduledForBetween(RideStatus status, LocalDateTime start, LocalDateTime end);

    List<Ride> findByRiderIdAndStatusOrderByFinishedAtDesc(Long riderId, RideStatus status);

    List<Ride> findByDriverIdAndStatusOrderByFinishedAtDesc(Long driverId, RideStatus status);

    // Does this rider currently have a ride that isn't finished/canceled/rejected?
    boolean existsByRiderIdAndStatusIn(Long riderId, List<RideStatus> statuses);
}
