package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.services;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
