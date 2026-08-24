package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.DriverStateDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.RideStateException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Driver;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.DriverRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverStateService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;

    @Transactional(readOnly = true)
    public DriverStateDTO getState(String email) {
        return toDto(loadDriver(email));
    }

    /** Toggle on/off duty */
    @Transactional
    public DriverStateDTO setActive(String email, boolean active) {
        Driver driver = loadDriver(email);
        driver.setActive(active);
        if (!active) {
            driver.setAvailable(false);
        } else {
            driver.resetWorkingHoursIfNeeded();
            driver.setAvailable(!driver.isHasActiveRide() && !driver.hasExceededWorkingHours());
        }
        driverRepository.save(driver);
        log.info("Driver {} set active={}", driver.getId(), active);
        return toDto(driver);
    }

    /** Driver logout */
    @Transactional
    public void logout(String email) {
        Driver driver = loadDriver(email);
        if (driver.isHasActiveRide()) {
            throw new RideStateException("You cannot log out during an active ride.");
        }
        driver.setLoggedIn(false);
        driver.setActive(false);
        driver.setAvailable(false);
        driverRepository.save(driver);
        log.info("Driver {} logged out", driver.getId());
    }


    private Driver loadDriver(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return driverRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));
    }

    private DriverStateDTO toDto(Driver d) {
        return new DriverStateDTO(d.isLoggedIn(), d.isActive(), d.isAvailable(),
                d.isHasActiveRide(), d.getWorkingMinutesLast24Hours());
    }

}
