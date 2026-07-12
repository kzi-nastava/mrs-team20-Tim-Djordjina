package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception;

/**
 * Thrown when a password reset token is missing, unknown or expired
 * Mapped to HTTP 400 in GlobalExceptionHandler
 */
public class InvalidPasswordResetTokenException extends RuntimeException {
    public InvalidPasswordResetTokenException(String message){
        super(message);
    }
}
