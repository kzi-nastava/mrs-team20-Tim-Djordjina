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

    private static final String USER_EMAIL = "user@test.com";
    private static final String DRIVER_EMAIL = "driver@test.com";
    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String KNOWN_PASSWORD = "password123";
    public static final String USER_FIRST_NAME = "John";
    public static final String USER_LAST_NAME = "Doe";
    public static final String USER_PHONE_NUMBER = "+381600000001";
    public static final String USER_ADDRESS = "Addr 1";
    public static final Role USER_ROLE = Role.USER;
    public static final String DRIVER_FIRST_NAME = "Jane";
    public static final String DRIVER_LAST_NAME = "Driver";
    public static final String DRIVER_PHONE_NUMBER = "+381600000002";
    public static final String DRIVER_ADDRESS = "Addr 2";
    public static final Role DRIVER_ROLE = Role.DRIVER;
    public static final Role ADMIN_ROLE = Role.ADMIN;
    public static final int WORKING_MINUTES_LAST_24_HOURS = 120;
    public static final String MODEL = "Skoda Octavia";
    public static final VehicleType VEHICLE_TYPE_STANDARD = VehicleType.STANDARD;
    public static final String LICENSE_PLATE = "NS123AB";
    public static final int SEATS = 4;
    public static final boolean BABY_TRANSPORT = true;
    public static final boolean PET_TRANSPORT = false;
    public static final String DRIVER_FIRST_NAME_UPDATED = "Janet";
    public static final String USER_FIRST_NAME_UPDATED = "Johnny";
    public static final String USER_ADDRESS_UPDATED = "New Address";
    public static final String WRONG_PASSWORD = "wrongpassword";
    public static final String NEW_PASSWORD = "newpassword1";
    public static final int ONE_PENDING_REQUEST = 1;
    public static final int FIRST_ELEMENT_INDEX = 0;
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String USER_ROLE_TOSTRING = "USER";
    public static final String API_USERS_ME_PATH = "/api/users/me";
    public static final String API_ADMIN_PROFILE_CHANGES_PATH = "/api/admin/profile-changes";
    public static final String API_ADMIN_PROFILE_CHANGES_SLASH_PATH = API_ADMIN_PROFILE_CHANGES_PATH + "/";
    public static final String APPROVE_PATH = "/approve";
    public static final String CHANGE_PASSWORD_PATH = "/change-password";

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



    @BeforeEach
    void setup() {
        // Regular user
        User user = new User(USER_FIRST_NAME, USER_LAST_NAME, USER_EMAIL,
                passwordEncoder.encode(KNOWN_PASSWORD), USER_PHONE_NUMBER, USER_ADDRESS, USER_ROLE);
        user.setActivated(true);
        userRepository.save(user);

        // Driver + vehicle
        User driverUser = new User(DRIVER_FIRST_NAME, DRIVER_LAST_NAME, DRIVER_EMAIL,
                passwordEncoder.encode(KNOWN_PASSWORD), DRIVER_PHONE_NUMBER, DRIVER_ADDRESS, DRIVER_ROLE);
        driverUser.setActivated(true);
        driverUser = userRepository.save(driverUser);

        Driver driver = new Driver();
        driver.setUser(driverUser);
        driver.setWorkingMinutesLast24Hours(WORKING_MINUTES_LAST_24_HOURS);
        driver = driverRepository.save(driver);

        Vehicle vehicle = new Vehicle();
        vehicle.setDriver(driver);
        vehicle.setModel(MODEL);
        vehicle.setVehicleType(VEHICLE_TYPE_STANDARD);
        vehicle.setLicensePlate(LICENSE_PLATE);
        vehicle.setSeats(SEATS);
        vehicle.setBabyTransport(BABY_TRANSPORT);
        vehicle.setPetTransport(PET_TRANSPORT);
        vehicleRepository.save(vehicle);

        userToken = jwtTokenProvider.generateToken(USER_EMAIL, USER_ROLE.toString());
        driverToken = jwtTokenProvider.generateToken(DRIVER_EMAIL, DRIVER_ROLE.toString());
        adminToken = jwtTokenProvider.generateToken(ADMIN_EMAIL, ADMIN_ROLE.toString());
    }

    // ---------- View ----------

    @Test
    @DisplayName("GET /me returns the user's profile")
    void getMe_asUser_returnsProfile() throws Exception {
        mockMvc.perform(get(API_USERS_ME_PATH).header(AUTHORIZATION, BEARER + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.role").value(USER_ROLE_TOSTRING))
                .andExpect(jsonPath("$.vehicle").doesNotExist());
    }

    @Test
    @DisplayName("GET /me for a driver includes working minutes and vehicle")
    void getMe_asDriver_includesHoursAndVehicle() throws Exception {
        mockMvc.perform(get(API_USERS_ME_PATH).header(AUTHORIZATION, BEARER + driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workingMinutesLast24Hours").value(WORKING_MINUTES_LAST_24_HOURS))
                .andExpect(jsonPath("$.vehicle.licensePlate").value(LICENSE_PLATE));
    }

    @Test
    @DisplayName("GET /me without a token returns 401")
    void getMe_noToken_returns401() throws Exception {
        mockMvc.perform(get(API_USERS_ME_PATH))
                .andExpect(status().isUnauthorized());
    }

    // ---------- Edit ----------

    @Test
    @DisplayName("A regular user's profile edit is applied immediately")
    void updateProfile_asUser_appliesImmediately() throws Exception {
        UpdateProfileDTO dto = update(USER_FIRST_NAME_UPDATED, USER_LAST_NAME, USER_PHONE_NUMBER, USER_ADDRESS_UPDATED);

        mockMvc.perform(put(API_USERS_ME_PATH)
                        .header(AUTHORIZATION, BEARER + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        assertEquals(USER_FIRST_NAME_UPDATED, userRepository.findByEmail(USER_EMAIL).orElseThrow().getFirstName());
    }

    @Test
    @DisplayName("A driver's edit is not applied, it creates a pending change request")
    void updateProfile_asDriver_createsPendingRequest() throws Exception {
        UpdateProfileDTO dto = update(DRIVER_FIRST_NAME_UPDATED, DRIVER_LAST_NAME, DRIVER_PHONE_NUMBER, DRIVER_ADDRESS);

        mockMvc.perform(put(API_USERS_ME_PATH)
                        .header(AUTHORIZATION, BEARER + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        // Live profile unchanged
        assertEquals(DRIVER_FIRST_NAME, userRepository.findByEmail(DRIVER_EMAIL).orElseThrow().getFirstName());
        // One pending request exists
        assertEquals(ONE_PENDING_REQUEST, changeRequestRepository.findByStatus(ProfileChangeStatus.PENDING).size());
    }

    @Test
    @DisplayName("Admin approval applies the driver's pending change")
    void adminApprove_appliesDriverChange() throws Exception {
        // Driver submits
        UpdateProfileDTO dto = update(DRIVER_FIRST_NAME_UPDATED, DRIVER_LAST_NAME, DRIVER_PHONE_NUMBER, DRIVER_ADDRESS);
        mockMvc.perform(put(API_USERS_ME_PATH)
                        .header(AUTHORIZATION, BEARER + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        Long requestId = changeRequestRepository.findByStatus(ProfileChangeStatus.PENDING).get(FIRST_ELEMENT_INDEX).getId();

        // Admin lists + approves
        mockMvc.perform(get(API_ADMIN_PROFILE_CHANGES_PATH)
                        .header(AUTHORIZATION, BEARER + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].driverEmail").value(DRIVER_EMAIL));

        mockMvc.perform(post(API_ADMIN_PROFILE_CHANGES_SLASH_PATH + requestId + APPROVE_PATH)
                        .header(AUTHORIZATION, BEARER + adminToken))
                .andExpect(status().isOk());

        // The live profile reflects the change
        assertEquals(DRIVER_FIRST_NAME_UPDATED, userRepository.findByEmail(DRIVER_EMAIL).orElseThrow().getFirstName());
    }

    @Test
    @DisplayName("Non-admin cannot access the profile-changes review endpoint")
    void listPendingChanges_asUser_returns403() throws Exception {
        mockMvc.perform(get(API_ADMIN_PROFILE_CHANGES_PATH)
                        .header(AUTHORIZATION, BEARER + userToken))
                .andExpect(status().isForbidden());
    }

    // ---------- Change Password ----------

    @Test
    @DisplayName("Change password with the wrong current password returns 400")
    void changePassword_wrongCurrent_returns400() throws Exception {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setCurrentPassword(WRONG_PASSWORD);
        dto.setNewPassword(NEW_PASSWORD);
        dto.setConfirmPassword(NEW_PASSWORD);

        mockMvc.perform(post(API_USERS_ME_PATH + CHANGE_PASSWORD_PATH)
                        .header(AUTHORIZATION, BEARER + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Change password with the correct current password succeeds")
    void changePassword_valid_succeeds() throws Exception {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setCurrentPassword(KNOWN_PASSWORD);
        dto.setNewPassword(NEW_PASSWORD);
        dto.setConfirmPassword(NEW_PASSWORD);

        mockMvc.perform(post(API_USERS_ME_PATH + CHANGE_PASSWORD_PATH)
                        .header(AUTHORIZATION, BEARER + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        User updated = userRepository.findByEmail(USER_EMAIL).orElseThrow();
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, updated.getPasswordHash()));
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