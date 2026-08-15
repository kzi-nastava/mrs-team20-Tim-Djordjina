package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideHistoryDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.ForbiddenActionException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.DriverRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.RideRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideHistoryService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final RideRepository rideRepository;
    private final RideHistoryMapper mapper;

    @Transactional(readOnly = true)
    public List<RideHistoryDTO> getHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Ride> rides;
        if (user.getRole() == Role.DRIVER) {
            Driver driver = driverRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Driver not found"));
            rides = rideRepository.findByDriverIdAndStatusOrderByFinishedAtDesc(
                    driver.getId(), RideStatus.FINISHED);
        } else {
            rides = rideRepository.findByRiderIdAndStatusOrderByFinishedAtDesc(
                    user.getId(), RideStatus.FINISHED);
        }
        return rides.stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public RideHistoryDTO getDetail(Long rideId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));

        boolean isRider = ride.getRider() != null
                && ride.getRider().getId().equals(user.getId());
        boolean isDriver =  ride.getDriver() != null
                && ride.getDriver().getUser() != null
                && ride.getDriver().getUser().getId().equals(user.getId());
        if (!isRider && !isDriver) {
            throw new ForbiddenActionException("This ride is not part of your history");
        }
        return mapper.toDto(ride);
    }


}
