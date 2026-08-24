package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto;


import lombok.Data;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Role;

/* Response for GET /api/users/me
*  Common fields for everyone, driver-only fields are null for non-drivers */
@Data
public class ProfileResponseDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private String profilePicture;      // may be null -> client shows a default
    private Role role;

    // DRIVER-only (null for USER/ADMIN)
    private Integer workingMinutesLast24Hours;
    private Boolean hasPendingProfileChanges;
    private VehicleInfo vehicle;

    @Data
    public static class VehicleInfo {
        private String model;
        private String vehicleType;
        private String licensePlate;
        private int seats;
        private boolean babyTransport;
        private boolean petTransport;
    }
}
