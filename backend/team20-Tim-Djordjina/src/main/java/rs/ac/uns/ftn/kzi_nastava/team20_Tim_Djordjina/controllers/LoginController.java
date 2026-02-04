package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.services.EmailService;

@Controller
public class LoginController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/test-mail")
    public ResponseEntity<String> testMail() {
        emailService.sendEmail(
                "test@example.com",
                "MailDev test",
                "If you see this, MailDev works"
        );
        return new ResponseEntity<>("email sent", HttpStatus.OK);
    }
}
