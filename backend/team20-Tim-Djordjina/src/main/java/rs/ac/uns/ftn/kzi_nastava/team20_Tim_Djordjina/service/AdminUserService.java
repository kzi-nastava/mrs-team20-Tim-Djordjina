package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.UserListItemDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Driver;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Role;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.DriverRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;

import java.util.List;
import java.util.Optional;

/*
*  Admin operations on users and drivers */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;

    /*
    * List users for the admin screen, optionally filtered by role
    * Admin accounts are never listed
    * */
    @Transactional(readOnly = true)
    public List<UserListItemDTO> listUsers(Role roleFilter) {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() != Role.ADMIN)
                .filter(u -> roleFilter == null || u.getRole() == roleFilter)
                .map(this::toDto)
                .toList();
    }

    /* Blocks a user/driver and stores the admin's explanatory note. */
    @Transactional
    public UserListItemDTO blockUser(Long userId, String blockNote) {
        User user = findBlockableUser(userId);

        if (user.isBlocked()){
            throw new IllegalArgumentException("User is already blocked");
        }

        user.setBlocked(true);
        user.setBlockNote(blockNote);
        userRepository.save(user);

        if (user.getRole() == Role.DRIVER){
            Optional<Driver> driver = driverRepository.findByUserId(user.getId());
            driver.ifPresent(d -> {
                d.setAvailable(false);
                d.setActive(false);
                driverRepository.save(d);
            });
        }

        log.info("Admin blocked user {} ({})", user.getEmail(), user.getRole());
        return toDto(user);
    }

    /* Unblocks a user/driver and clears the note */
    @Transactional
    public UserListItemDTO unblockUser(Long userId) {
        User user = findBlockableUser(userId);

        if (!user.isBlocked()){
            throw new IllegalArgumentException("User is not blocked");
        }

        user.setBlocked(false);
        user.setBlockNote(null);
        userRepository.save(user);

        log.info("Admin unblocked user {} ({})", user.getEmail(), user.getRole());
        return toDto(user);
    }

    /* Loads a user and refuses to act on admin accounts. */
    private User findBlockableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() == Role.ADMIN){
            throw new IllegalArgumentException("Administrator account cannot be blocked");
        }
        return user;
    }

    private UserListItemDTO toDto(User u) {
        return new UserListItemDTO(
                u.getId(),
                u.getFirstName(),
                u.getLastName(),
                u.getEmail(),
                u.getPhoneNumber(),
                u.getRole(),
                u.isActivated(),
                u.isBlocked(),
                u.getBlockNote()
        );
    }
}
