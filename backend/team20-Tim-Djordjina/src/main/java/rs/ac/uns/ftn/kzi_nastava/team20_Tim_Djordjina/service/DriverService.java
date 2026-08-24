package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.DriverRegistrationDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception.EmailAlreadyExistsException;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Driver;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Role;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Vehicle;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.DriverRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.VehicleRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final int PASSWORD_SETUP_VALID_HOURS = 24;


    @Transactional
    public void registerDriverByAdmin(DriverRegistrationDTO dto) {
        // Check for uniqueness
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("A user with this email already exists");
        }
        if (vehicleRepository.existsByLicensePlate(dto.getVehicle().getLicensePlate())) {
            throw new IllegalArgumentException("A vehicle with this license plate already exists");
        }

        // Create the User (Driver). Password is a random placeholder the
        // driver never know, they set a real one via the email link
        User user = new User(
                dto.getFirstName(),
                dto.getLastName(),
                dto.getEmail(),
                passwordEncoder.encode(UUID.randomUUID().toString()),
                dto.getPhoneNumber(),
                dto.getAddress(),
                Role.DRIVER
        );
        user.setActivated(true);

        String setupToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(setupToken);
        user.setResetPasswordTokenExpirationDate(LocalDateTime.now().plusHours(PASSWORD_SETUP_VALID_HOURS));

        User savedUser = userRepository.save(user);

        // Create the driver linked to the user
        Driver driver = new Driver();
        driver.setUser(savedUser);
        Driver savedDriver = driverRepository.save(driver);

        // Create the vehicle linked to the driver
        Vehicle vehicle = new Vehicle();
        vehicle.setDriver(savedDriver);
        vehicle.setModel(dto.getVehicle().getModel());
        vehicle.setVehicleType(dto.getVehicle().getVehicleType());
        vehicle.setLicensePlate(dto.getVehicle().getLicensePlate());
        vehicle.setSeats(dto.getVehicle().getSeats());
        vehicle.setBabyTransport(dto.getVehicle().isBabyTransport());
        vehicle.setPetTransport(dto.getVehicle().isPetTransport());
        vehicleRepository.save(vehicle);

        // Email the driver a link to set their password
        emailService.sendDriverWelcomeEmail(savedUser.getEmail(), savedUser.getFirstName(), setupToken);

        log.info("Admin registered new driver: {}", savedUser.getEmail());
    }
}
