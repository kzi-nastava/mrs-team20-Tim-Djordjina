package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Panic;

import java.util.List;

public interface PanicRepository extends JpaRepository<Panic, Long> {
    List<Panic> findAllByOrderByCreatedAtDesc();

    boolean existsByRideId(Long rideId);
}
