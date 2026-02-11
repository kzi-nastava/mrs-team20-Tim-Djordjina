package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message){
        super(message);
    }
}
