package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.ActiveVehicleDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Driver;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Vehicle;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.DriverRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.VehicleRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActiveVehicleService {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional(readOnly = true)
    public List<ActiveVehicleDTO> getActiveVehicles() {
        List<Driver> drivers = driverRepository.findActiveOnDutyDrivers();
        List<ActiveVehicleDTO> result = new ArrayList<>();
        for (Driver d : drivers) {
            Vehicle v = vehicleRepository.findByDriverId(d.getId()).orElse(null);
            if (v == null || v.getCurrentLatitude() == null || v.getCurrentLongitude() == null) {
                continue;
            }
            result.add(new ActiveVehicleDTO(
                    v.getId(),
                    v.getCurrentLatitude(),
                    v.getCurrentLongitude(),
                    d.isHasActiveRide(),
                    v.getVehicleType() != null ? v.getVehicleType().name() : null,
                    v.getModel()
            ));
        }
        return result;
    }
}
