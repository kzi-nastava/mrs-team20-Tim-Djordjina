package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
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
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.FavouriteRouteDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideStopDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Role;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.FavouriteRouteRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.security.JwtTokenProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Favourite Routes - Integration Tests")
public class FavouriteRouteIntegrationTest {

    public static final String A_EMAIL = "a.test@com";
    public static final String B_EMAIL = "b.test@com";
    public static final String FAVOURITE_ROUTES_URL = "/api/favourite-routes";
    @Autowired
    private FavouriteRouteRepository favouriteRouteRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String tokenA;
    private String tokenB;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        cleanup();
        makeUser("Ann", "A", A_EMAIL);
        makeUser("Bob", "B", B_EMAIL);
        tokenA = jwtTokenProvider.generateToken(A_EMAIL, Role.USER.toString());
        tokenB = jwtTokenProvider.generateToken(B_EMAIL, Role.USER.toString());
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        favouriteRouteRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ---------- Tests ----------
    @Test
    @DisplayName("Save then list returns the route")
    void save_thenListed() throws Exception {
        saveRoute(tokenA, validRoute("Home to Work"));

        mockMvc.perform(get(FAVOURITE_ROUTES_URL)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].label").value("Home to Work"));
    }

    @Test
    @DisplayName("Delete removes the route")
    void delete_removesRoute() throws Exception {
        long id = saveRoute(tokenA, validRoute("Route"));

        mockMvc.perform(delete(FAVOURITE_ROUTES_URL + "/" + id)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());

        mockMvc.perform(get(FAVOURITE_ROUTES_URL)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

    }

    @Test
    @DisplayName("Cannot delete another user's route (400)")
    void delete_othersRoute_returns400() throws Exception {
        long id = saveRoute(tokenA, validRoute("A's route"));

        mockMvc.perform(delete(FAVOURITE_ROUTES_URL + "/" + id)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isBadRequest());

        assertEquals(1, favouriteRouteRepository.count(), "A's route should still exist");
    }

    @Test
    @DisplayName("Saving without a destination is rejected (400)")
    void save_missingDestination_returns400() throws Exception {
        FavouriteRouteDTO dto = validRoute("Bad");
        dto.setDestinationAddress(null);
        dto.setDestinationLatitude(null);
        dto.setDestinationLongitude(null);

        mockMvc.perform(post(FAVOURITE_ROUTES_URL)
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Favourites are per-user")
    void list_isPerUser() throws Exception {
        saveRoute(tokenA, validRoute("A route"));
        saveRoute(tokenB, validRoute("B route"));

        mockMvc.perform(get(FAVOURITE_ROUTES_URL)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].label").value("A route"));
    }

    @Test
    @DisplayName("Stops are saved and returned in order")
    void save_withStops_persistedInOrder() throws Exception {
        FavouriteRouteDTO dto = validRoute("With stops");
        dto.setStops(List.of(stop("Stop A", 45.255, 19.845, 0),
                             stop("Stop B", 45.258, 19.848, 1)));
        saveRoute(tokenA, dto);

        mockMvc.perform(get(FAVOURITE_ROUTES_URL)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stops.length()").value(2))
                .andExpect(jsonPath("$[0].stops[0].stopOrder").value(0))
                .andExpect(jsonPath("$[0].stops[1].stopOrder").value(1));
    }

    // ---------- Helpers ----------

    private long saveRoute(String token, FavouriteRouteDTO dto) throws Exception {
        String body = mockMvc.perform(post(FAVOURITE_ROUTES_URL)
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto)))
                            .andExpect(status().isCreated())
                            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private FavouriteRouteDTO validRoute(String label) {
        FavouriteRouteDTO dto = new FavouriteRouteDTO();
        dto.setLabel(label);
        dto.setPickupAddress("Trg slobode");
        dto.setPickupLatitude(45.2671);
        dto.setPickupLongitude(19.8335);
        dto.setDestinationAddress("Strand");
        dto.setDestinationLatitude(45.2400);
        dto.setDestinationLongitude(19.8500);
        return dto;
    }

    private RideStopDTO stop(String address, double lat, double lng, int order) {
        RideStopDTO s = new RideStopDTO();
        s.setAddress(address);
        s.setLatitude(lat);
        s.setLongitude(lng);
        s.setStopOrder(order);
        return s;
    }

    private User makeUser(String first, String last, String email) {
        User u = new User(first, last, email,
                passwordEncoder.encode("password123"), "+381600000000", "Addr", Role.USER);
        u.setActivated(true);
        return userRepository.save(u);
    }
}
