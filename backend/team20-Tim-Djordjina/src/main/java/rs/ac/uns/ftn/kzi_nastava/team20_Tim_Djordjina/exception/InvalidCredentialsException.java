package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception;

/**
 * Exception thrown when login credentials is invalid
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message){
        super(message);
    }
}
