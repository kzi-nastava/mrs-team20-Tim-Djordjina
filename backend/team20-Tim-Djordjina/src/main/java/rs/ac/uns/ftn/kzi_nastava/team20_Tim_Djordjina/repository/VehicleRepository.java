package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Vehicle;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.VehicleType;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /*
    * Find vehicle by driver ID
    * */
    Optional<Vehicle> findByDriverId(Long driverId);

    /*
    * Find vehicle by license plate
    * */
    Optional<Vehicle> findByLicensePlate(String licensePlate);

    /*
    * Check if license plate exists
    * */
    boolean existsByLicensePlate(String licensePlate);

    /*
    * Find vehicles by type
    * */
    List<Vehicle> findByVehicleType(VehicleType vehicleType);

    /*
    * Find vehicles that allow baby transport
    * */
    List<Vehicle> findByBabyTransportTrue();

    /*
     * Find vehicles that allow pet transport
     * */
    List<Vehicle> findByPetTransportTrue();
}
