package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Role;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.EmailService;

import java.net.UnknownServiceException;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("JWT Endpoint Security - Integration Tests")
public class JwtSecurityIntegrationTest {

    private static final String USER_EMAIL = "user@test.com";
    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String PROTECTED_URL = "/api/users/me";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private EmailService emailService;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setup(){
        // Real token signed by the real provider
        userToken = jwtTokenProvider.generateToken(USER_EMAIL, Role.USER.toString());
        adminToken = jwtTokenProvider.generateToken(ADMIN_EMAIL, Role.ADMIN.toString());

        // For valid token case, controller looks up the user by email
        User user = new User();
        user.setId(1L);
        user.setEmail(USER_EMAIL);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.USER);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
    }

    // ---------- Protected endpoint ----------------------

    @Test
    @DisplayName("Protected endpoint without a token returns 401")
    void protectedEndpoint_noToken_returns401() throws Exception {
        mockMvc.perform(get(PROTECTED_URL)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected endpoint with an invalid token returns 401")
    void protectedEndpoint_invalidToken_returns401() throws Exception {
        mockMvc.perform(get(PROTECTED_URL).header("Authorization", "Bearer thisis.notavalid.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected endpoint with a valid token returns 200 and the user data")
    void protectedEndpoint_validToken_returns200() throws Exception {
        mockMvc.perform(get(PROTECTED_URL).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(USER_EMAIL));
    }
}
