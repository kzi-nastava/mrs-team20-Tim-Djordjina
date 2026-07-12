package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.DriverRegistrationDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.VehicleDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Driver;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Role;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.VehicleType;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.DriverRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.VehicleRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.security.JwtTokenProvider;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.EmailService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Admin Driver Registration - Integration Tests")
public class AdminDriverRegistrationIntegrationTest {

    private static final String ADMIN_URL = "/api/admin/drivers";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @MockitoBean
    private EmailService emailService;

    private String adminToken;
    private String userToken;



    @BeforeEach
    void setup(){
        adminToken = jwtTokenProvider.generateToken("admin@test.com", Role.ADMIN.toString());
        userToken = jwtTokenProvider.generateToken("user@test.com", Role.USER.toString());
    }

    @Test
    @DisplayName("Admin can register a driver -> 201 and persists User + Driver + Vehicle")
    void adminToken_validRequest_returns201AndPersists() throws Exception {
        String email = "jane.driver@test.com";
        String plate = "NS123AB";

        mockMvc.perform(post(ADMIN_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validDto(email, plate))))
                .andExpect(status().isCreated());

        Optional<User> savedUser = userRepository.findByEmail(email);
        assertTrue(savedUser.isPresent(), "User should be created");
        assertEquals(Role.DRIVER, savedUser.get().getRole());
        assertTrue(savedUser.get().isActivated());

        Optional<Driver> savedDriver = driverRepository.findByUserId(savedUser.get().getId());
        assertTrue(savedDriver.isPresent(), "Driver should be created");
        assertTrue(vehicleRepository.existsByLicensePlate(plate), "Vehicle should be created");

        verify(emailService).sendDriverWelcomeEmail(eq(email), anyString(), anyString());
    }

    @Test
    @DisplayName("Non-admin (USER) token -> 403 and nothing is created")
    void userToken_returns403() throws Exception {
        String email = "blocked.driver@test.com";

        mockMvc.perform(post(ADMIN_URL)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validDto(email, "NS999ZZ"))))
                .andExpect(status().isForbidden());

        assertFalse(userRepository.existsByEmail(email), "No user should be created");
        verify(emailService, never()).sendDriverWelcomeEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("No token -> 401")
    void noToken_returns401() throws Exception {
        mockMvc.perform(post(ADMIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validDto("no.token@test.com", "NS000AA"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Duplicate email -> 409")
    void duplicateEmail_returns409() throws Exception {
        String email = "duplicate@test.com";

        // First registration succeeds
        mockMvc.perform(post(ADMIN_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validDto(email, "NS111AA"))))
                .andExpect(status().isCreated());

        // Second registration -> same email, different plate -> 409
        mockMvc.perform(post(ADMIN_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validDto(email, "NS222BB"))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Duplicate license plate -> 400")
    void duplicateLicensePlate_returns400() throws Exception {
        String plate = "NS333CC";

        // First registration succeeds
        mockMvc.perform(post(ADMIN_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validDto("first@test.com", plate))))
                .andExpect(status().isCreated());

        // Second registration -> different email, same plate -> 400
        mockMvc.perform(post(ADMIN_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validDto("second@test.com", plate))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Missing required fields -> 400")
    void invalidRequest_returns400() throws Exception {
        DriverRegistrationDTO dto = validDto("invalid@test.com", "NS444DD");
        dto.setFirstName("");           // violates @NotBlank
        dto.getVehicle().setSeats(0);   // violates @Min(1)

        mockMvc.perform(post(ADMIN_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest());
    }

    private DriverRegistrationDTO validDto(String email, String licensePlate) {
        VehicleDTO vehicle = new VehicleDTO();
        vehicle.setModel("Skoda Octavia");
        vehicle.setVehicleType(VehicleType.STANDARD);
        vehicle.setLicensePlate(licensePlate);
        vehicle.setSeats(4);
        vehicle.setBabyTransport(true);
        vehicle.setPetTransport(false);

        DriverRegistrationDTO dto = new DriverRegistrationDTO();
        dto.setFirstName("Jane");
        dto.setLastName("Driver");
        dto.setEmail(email);
        dto.setPhoneNumber("+381641234567");
        dto.setAddress("Bulevar 1, Novi Sad");
        dto.setVehicle(vehicle);

        return dto;
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }
}
