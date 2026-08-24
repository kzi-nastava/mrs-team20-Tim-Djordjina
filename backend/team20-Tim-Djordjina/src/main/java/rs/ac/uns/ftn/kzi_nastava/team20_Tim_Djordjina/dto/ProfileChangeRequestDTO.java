package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/* Admin view of pending driver profile change request */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileChangeRequestDTO {
    private Long id;
    private Long userId;
    private String driverEmail;

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String profilePicture;
    private String status;
}
