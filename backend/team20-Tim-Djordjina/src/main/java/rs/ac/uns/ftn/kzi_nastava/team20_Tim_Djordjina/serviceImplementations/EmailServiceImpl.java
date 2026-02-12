/*
package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.serviceImplementations;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.services.EmailService;

import java.io.File;

@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final String mailFrom = "no-reply@ride.on";

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setFrom(mailFrom);
        msg.setSubject(subject);
        msg.setText(body);

        mailSender.send(msg);
    }

    public void sendActivationEmail(String to, String link) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("RideOn Account Activation");
        helper.setText(buildHtml(link), true);
        helper.setFrom(mailFrom);

        FileSystemResource logo = new FileSystemResource(new File("backend/team20-Tim-Djordjina/src/main/resources/images/rideon_logo.png"));
        helper.addInline("appLogo", logo);

        mailSender.send(message);
    }

    private String buildHtml(String link) {
        return """
            <html>
              <body style="font-family: Arial; text-align:center">
                <img src="cid:appLogo" alt="RideOn Logo" width="150"/><br/>
                <h2>Welcome to RideOn!</h2>
                <p>Please activate your account</p>
                <a href="%s"
                   style="padding:12px 20px;
                          background:#1976d2;
                          color:white;
                          text-decoration:none;
                          border-radius:5px;">
                   Activate account
                </a>
                <p style="margin-top:20px;font-size:12px">
                  Link expires in 24 hours
                </p>
              </body>
            </html>
        """.formatted(link);
    }

}*/