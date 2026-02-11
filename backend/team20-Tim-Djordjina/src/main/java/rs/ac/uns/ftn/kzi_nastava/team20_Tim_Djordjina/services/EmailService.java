package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.services;

import jakarta.mail.MessagingException;
public interface EmailService {
    void sendEmail(String to, String subject, String body);

    void sendActivationEmail(String to, String link) throws MessagingException;

}