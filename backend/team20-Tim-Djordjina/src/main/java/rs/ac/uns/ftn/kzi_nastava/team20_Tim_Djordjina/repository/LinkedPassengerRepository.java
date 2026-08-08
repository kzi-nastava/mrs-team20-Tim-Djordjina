package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.LinkedPassenger;

import java.util.List;

public interface LinkedPassengerRepository extends JpaRepository<LinkedPassenger, Long> {

    List<LinkedPassenger> findByRideId(Long rideId);
    List<LinkedPassenger> findByEmail(String email);
}
