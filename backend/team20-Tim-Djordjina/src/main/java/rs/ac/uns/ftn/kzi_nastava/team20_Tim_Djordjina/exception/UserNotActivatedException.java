package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception;

import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;

/**
 * Exception thrown when user tries to login before activating account
 */
public class UserNotActivatedException extends RuntimeException {
    public UserNotActivatedException(String message){ super(message); }
}
