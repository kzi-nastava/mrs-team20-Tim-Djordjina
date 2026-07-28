package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListItemDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Role role;
    private boolean activated;
    private boolean blocked;
    private String blockNote;
}
