package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideHistoryDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Ride;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.RideStatus;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.RideRepository;

import java.util.List;

/**
 * Admin ride state view: all rides, optionally filtered by status
 * */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminRideService {

    private final RideRepository rideRepository;
    private final RideHistoryMapper mapper;

    @Transactional(readOnly = true)
    public List<RideHistoryDTO> getAllRides(String status) {
        List<Ride> rides = (status == null || status.isBlank())
                ? rideRepository.findAllByOrderByCreatedAtDesc()
                : rideRepository.findByStatusOrderByCreatedAtDesc(parseStatus(status));
        return rides.stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public RideHistoryDTO getRide(Long id) {
        Ride ride = rideRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));
        return mapper.toDto(ride);
    }


    private RideStatus parseStatus(String status) {
        try {
            return RideStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ride status: " + status);
        }
    }
}
