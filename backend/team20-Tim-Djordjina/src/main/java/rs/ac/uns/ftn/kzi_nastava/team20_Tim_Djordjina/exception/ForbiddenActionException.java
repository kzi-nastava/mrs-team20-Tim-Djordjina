package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception;

/** Thrown when a user tries to act on a resource they don't own.
 *  Mapped to HTTP 403. */
public class ForbiddenActionException extends RuntimeException {
    public ForbiddenActionException(String message) {
        super(message);
    }
}
