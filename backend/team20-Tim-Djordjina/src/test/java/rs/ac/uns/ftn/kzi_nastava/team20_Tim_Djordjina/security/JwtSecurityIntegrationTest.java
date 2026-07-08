package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Role;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.EmailService;

import java.net.UnknownServiceException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(JwtSecurityIntegrationTest.SecurityTestController.class)
@DisplayName("JWT Endpoint Security - Integration Tests")
public class JwtSecurityIntegrationTest {

    private static final String USER_EMAIL = "user@test.com";
    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String PROTECTED_URL = "/api/users/me";
    private static final String ADMIN_URL = "/api/admin/security-test-ping";
    private static final String LOGIN_URL = "/api/auth/login";

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

    // ---------- Role-restricted endpoint (ADMIN)  ----------------------

    @Test
    @DisplayName("Admin endpoint with a USER token returns 403")
    void adminEndpoint_userToken_returns403() throws Exception {
        mockMvc.perform(get(ADMIN_URL).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }


    @Test
    @DisplayName("Admin endpoint with a ADMIN token returns 200")
    void adminEndpoint_adminToken_returns200() throws Exception {
        mockMvc.perform(get(ADMIN_URL).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin endpoint without a token returns 401")
    void adminEndpoint_noToken_returns401() throws Exception {
        mockMvc.perform(get(ADMIN_URL))
                .andExpect(status().isUnauthorized());
    }


    // ---------- Public auth endpoints  ----------------------

    @Test
    @DisplayName("Public auth endpoint is reachable without a token (not 401/403)")
    void publicAuthEndpoint_reachableWithoutToken() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(result -> {
                    int statusCode = result.getResponse().getStatus();
                    assertNotEquals(401, statusCode, "Public endpoint must not be blocked with 401");
                    assertNotEquals(403, statusCode, "Public endpoint must not be blocked with 403");
                });
    }

    // ---------- Test only controller providing an ADMIN-restricted path  ----------------------
    /**
    * Minimal controller exposed only during this test, so we can verify the ADMIN-allow path.
    **/
    @RestController
    static class SecurityTestController {
        @GetMapping("/api/admin/security-test-ping")
        public String adminPing(){
            return "admin-ok";
        }
    }

}
