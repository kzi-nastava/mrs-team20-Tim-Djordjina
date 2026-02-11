package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {

    private boolean success;
    private String message;
    private Object data;

    public ApiResponse(boolean success, String message){
        this.success = success;
        this.message = message;
    }

    public static ApiResponse success(String message) { return new ApiResponse(true, message); }
    public static ApiResponse success(String message, Object data) { return new ApiResponse(true, message, data); }
    public static ApiResponse error(String message) { return new ApiResponse(false, message); }
}

