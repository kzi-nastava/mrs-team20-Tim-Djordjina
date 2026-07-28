package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BlockUserDTO {
    @NotBlank(message = "A note explaining the block is required")
    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    private String blockNote;
}
