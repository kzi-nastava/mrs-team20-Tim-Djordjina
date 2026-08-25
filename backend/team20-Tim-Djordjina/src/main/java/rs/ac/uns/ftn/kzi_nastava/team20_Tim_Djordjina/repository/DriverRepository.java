package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Driver;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    /**
     * Find driver by user ID
     */
    Optional<Driver> findByUserId(Long userId);

    /**
     * Find driver by user email
     */
    @Query("SELECT d FROM Driver d WHERE d.user.email = :email")
    Optional<Driver> findByUserEmail(String email);

    /**
     * Find all available drivers who can accept rides
     */
    @Query("SELECT d FROM Driver d WHERE d.isLoggedIn = true " +
            "AND d.isActive = true " +
            "AND d.isAvailable = true " +
            "AND d.hasActiveRide = false " +
            "AND d.workingMinutesLast24Hours < 480 " +
            "AND d.user.isBlocked = false " +
            "AND d.user.isActivated = true")
    List<Driver> findAllAvailableDrivers();

    /**
    * Find drivers with pending profile changes (for admin approval)
    */
    List<Driver> findByHasPendingProfileChangesTrue();

    @Query("SELECT d FROM Driver d WHERE d.isLoggedIn = true AND d.isActive = true")
    List<Driver> findActiveOnDutyDrivers();
}
