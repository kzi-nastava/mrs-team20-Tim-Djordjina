package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.ChangePasswordDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.UpdateProfileDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.DriverRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.ProfileChangeRequestRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.VehicleRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.security.JwtTokenProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Profile - Integration Tests")
public class ProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ProfileChangeRequestRepository changeRequestRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;


    private String userToken;
    private String driverToken;
    private String adminToken;

    private static final String USER_EMAIL = "user@test.com";
    private static final String DRIVER_EMAIL = "driver@test.com";
    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String KNOWN_PASSWORD = "password123";

    @BeforeEach
    void setup() {
        // Regular user
        User user = new User("John", "Doe", USER_EMAIL,
                passwordEncoder.encode(KNOWN_PASSWORD), "+381600000001", "Addr 1", Role.USER);
        user.setActivated(true);
        userRepository.save(user);

        // Driver + vehicle
        User driverUser = new User("Jane", "Driver", DRIVER_EMAIL,
                passwordEncoder.encode(KNOWN_PASSWORD), "+381600000002", "Addr 2", Role.DRIVER);
        driverUser.setActivated(true);
        driverUser = userRepository.save(driverUser);

        Driver driver = new Driver();
        driver.setUser(driverUser);
        driver.setWorkingMinutesLast24Hours(120);
        driver = driverRepository.save(driver);

        Vehicle vehicle = new Vehicle();
        vehicle.setDriver(driver);
        vehicle.setModel("Skoda Octavia");
        vehicle.setVehicleType(VehicleType.STANDARD);
        vehicle.setLicensePlate("NS123AB");
        vehicle.setSeats(4);
        vehicle.setBabyTransport(true);
        vehicle.setPetTransport(false);
        vehicleRepository.save(vehicle);

        userToken = jwtTokenProvider.generateToken(USER_EMAIL, Role.USER.toString());
        driverToken = jwtTokenProvider.generateToken(DRIVER_EMAIL, Role.DRIVER.toString());
        adminToken = jwtTokenProvider.generateToken(ADMIN_EMAIL, Role.ADMIN.toString());
    }

    // ---------- View ----------

    @Test
    @DisplayName("GET /me returns the user's profile")
    void getMe_asUser_returnsProfile() throws Exception {
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.vehicle").doesNotExist());
    }

    @Test
    @DisplayName("GET /me for a driver includes working minutes and vehicle")
    void getMe_asDriver_includesHoursAndVehicle() throws Exception {
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workingMinutesLast24Hours").value(120))
                .andExpect(jsonPath("$.vehicle.licensePlate").value("NS123AB"));
    }

    @Test
    @DisplayName("GET /me without a token returns 401")
    void getMe_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- Edit ----------

    @Test
    @DisplayName("A regular user's profile edit is applied immediately")
    void updateProfile_asUser_appliesImmediately() throws Exception {
        UpdateProfileDTO dto = update("Johnny", "Doe", "+381600000001", "New Address");

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        assertEquals("Johnny", userRepository.findByEmail(USER_EMAIL).orElseThrow().getFirstName());
    }

    @Test
    @DisplayName("A driver's edit is not applied, it creates a pending change request")
    void updateProfile_asDriver_createsPendingRequest() throws Exception {
        UpdateProfileDTO dto = update("Janet", "Driver", "+381600000002", "Addr 2");

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        // Live profile unchanged
        assertEquals("Jane", userRepository.findByEmail(DRIVER_EMAIL).orElseThrow().getFirstName());
        // One pending request exists
        assertEquals(1, changeRequestRepository.findByStatus(ProfileChangeStatus.PENDING).size());
    }

    @Test
    @DisplayName("Admin approval applies the driver's pending change")
    void adminApprove_appliesDriverChange() throws Exception {
        // Driver submits
        UpdateProfileDTO dto = update("Janet", "Driver", "+381600000002", "Addr 2");
        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        Long requestId = changeRequestRepository.findByStatus(ProfileChangeStatus.PENDING).get(0).getId();

        // Admin lists + approves
        mockMvc.perform(get("/api/admin/profile-changes")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].driverEmail").value(DRIVER_EMAIL));

        mockMvc.perform(post("/api/admin/profile-changes/" + requestId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // The live profile reflects the change
        assertEquals("Janet", userRepository.findByEmail(DRIVER_EMAIL).orElseThrow().getFirstName());
    }

    @Test
    @DisplayName("Non-admin cannot access the profile-changes review endpoint")
    void listPendingChanges_asUser_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/profile-changes")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ---------- Change Password ----------

    @Test
    @DisplayName("Change password with the wrong current password returns 400")
    void changePassword_wrongCurrent_returns400() throws Exception {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setCurrentPassword("wrongpassword");
        dto.setNewPassword("newpassword1");
        dto.setConfirmPassword("newpassword1");

        mockMvc.perform(post("/api/users/me/change-password")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Change password with the correct current password succeeds")
    void changePassword_valid_succeeds() throws Exception {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setCurrentPassword(KNOWN_PASSWORD);
        dto.setNewPassword("newpassword1");
        dto.setConfirmPassword("newpassword1");

        mockMvc.perform(post("/api/users/me/change-password")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        User updated = userRepository.findByEmail(USER_EMAIL).orElseThrow();
        assertTrue(passwordEncoder.matches("newpassword1", updated.getPasswordHash()));
    }

    // ---------- Helper ----------

    private UpdateProfileDTO update(String first, String last, String phone, String address) {
        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setFirstName(first);
        dto.setLastName(last);
        dto.setPhoneNumber(phone);
        dto.setAddress(address);
        return dto;
    }
}