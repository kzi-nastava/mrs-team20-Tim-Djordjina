package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Rating;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    long countByRide_Driver_Id(Long driverId);

    boolean existsByRideId(Long rideId);

    @Query("SELECT AVG(r.driverRating) FROM Rating r WHERE r.ride.driver.id = :driverId")
    Double averageDriverRating(@Param("driverId") Long driverId);

    @Query("SELECT AVG(r.vehicleRating) FROM Rating r WHERE r.ride.driver.id = :driverId")
    Double averageVehicleRating(@Param("driverId") Long driverId);
}
