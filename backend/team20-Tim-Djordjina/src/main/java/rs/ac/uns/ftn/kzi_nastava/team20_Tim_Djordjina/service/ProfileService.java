package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.ProfileResponseDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.UpdateProfileDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.ProfileChangeRequest;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.ProfileChangeStatus;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Role;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.DriverRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.ProfileChangeRequestRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.VehicleRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final ProfileChangeRequestRepository changeRequestRepository;

    // -------- View --------

    @Transactional(readOnly = true)
    public ProfileResponseDTO getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ProfileResponseDTO dto = new ProfileResponseDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setRole(user.getRole());

        if (user.getRole() == Role.DRIVER) {
            driverRepository.findByUserId(user.getId()).ifPresent(driver -> {
                dto.setWorkingMinutesLast24Hours(driver.getWorkingMinutesLast24Hours());
                dto.setHasPendingProfileChanges(driver.isHasPendingProfileChanges());

                vehicleRepository.findByDriverId(driver.getId()).ifPresent(v -> {
                    ProfileResponseDTO.VehicleInfo vi = new ProfileResponseDTO.VehicleInfo();
                    vi.setModel(v.getModel());
                    vi.setVehicleType(v.getVehicleType().name());
                    vi.setLicensePlate(v.getLicensePlate());
                    vi.setSeats(v.getSeats());
                    vi.setBabyTransport(v.isBabyTransport());
                    vi.setPetTransport(v.isPetTransport());
                    dto.setVehicle(vi);
                });
            });
        }
        return dto;
    }

    // -------- Edit --------

    /**
     * Updates the profile.
     * For DRIVER this function creates a pending change request (not applied yet).
     * For USER/ADMIN the change is applied immediately.
     *
     * @return true if applied immediately, false if it went to admin approval
     * */
    @Transactional
    public boolean updateProfile(String email, UpdateProfileDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() == Role.DRIVER) {
            submitDriverChangeRequest(user, dto);
            return false;
        }

        applyToUser(user, dto);
        userRepository.save(user);
        log.info("Profile updated directly for {}", email);
        return true;
    }

    private void submitDriverChangeRequest(User user, UpdateProfileDTO dto) {
        ProfileChangeRequest req = new ProfileChangeRequest();
        req.setUser(user);
        req.setFirstName(dto.getFirstName());
        req.setLastName(dto.getLastName());
        req.setPhoneNumber(dto.getPhoneNumber());
        req.setAddress(dto.getAddress());
        req.setProfilePicture(
                (dto.getProfilePicture() == null || dto.getProfilePicture().isEmpty()
                        ? user.getProfilePicture() : dto.getProfilePicture())
        );
        req.setStatus(ProfileChangeStatus.PENDING);
        changeRequestRepository.save(req);

        driverRepository.findByUserId(user.getId()).ifPresent(d -> {
            d.setHasPendingProfileChanges(true);
            d.setPendingChangesDescription("Profile edit awaiting approval");
            driverRepository.save(d);
        });
        log.info("Driver {} submitted a profile change request", user.getEmail());
    }

    private void applyToUser(User user, UpdateProfileDTO dto) {
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setAddress(dto.getAddress());
        if (dto.getProfilePicture() != null && !dto.getProfilePicture().isEmpty()) {
            user.setProfilePicture(dto.getProfilePicture());
        }
    }

}
