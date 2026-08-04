package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception;

/** Thrown when a ride operation conflicts with the ride's
 * current state. Mapped to HTTP 409. */
public class RideStateException extends RuntimeException {
    public RideStateException(String message) {
        super(message);
    }
}
