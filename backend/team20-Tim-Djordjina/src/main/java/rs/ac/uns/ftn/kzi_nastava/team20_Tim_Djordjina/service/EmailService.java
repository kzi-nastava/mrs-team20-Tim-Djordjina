package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.Resolution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name}")
    private String fromName;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Send activation email asynchronously using Sendgrid
     */
    @Async
    public void sendActivationEmail(String toEmail, String firstName, String token){
        try{
            String activationLink = baseUrl + "/api/auth/activate?token=" + token;

            Email from = new Email(fromEmail, fromName);
            Email to = new Email(toEmail);
            String subjcet = "Activate Your Account - RideOn App";
            Content content = new Content("text/plain", buildActivationEmailContent(firstName, activationLink));

            Mail mail = new Mail(from, subjcet, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300){
                log.info("Activation email sent successfully to: {} (Status: {})", toEmail, response.getStatusCode());
            }
            else{
                log.error("Failed to send activation email to: {}. Status: {}, Body: {}",
                        toEmail, response.getStatusCode(), response.getBody());
            }
        }
        catch (IOException e){
            log.error("Failed to send activation email to: {}", toEmail, e);
        }

    }

    /**
     * Send HTML email using Sendgrid
     */
    @Async
    public void sendActivationEmailHtml(String toEmail, String firstName, String token){
        try{
            String activationLink = baseUrl + "/api/auth/activate?token=" + token;

            Email from = new Email(fromEmail, fromName);
            Email to = new Email(toEmail);
            String subjcet = "Activate Your Account - RideOn App";

            // HTML content for better formating
            String htmlContent = buildActivationEmailHtml(firstName, activationLink);
            Content content = new Content("text/html", htmlContent);

            Mail mail = new Mail(from, subjcet, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300){
                log.info("Activation email sent successfully to: {} (Status: {})", toEmail, response.getStatusCode());
            }
            else{
                log.error("Failed to send activation email to: {}. Status: {}, Body: {}",
                        toEmail, response.getStatusCode(), response.getBody());
            }
        }
        catch (IOException e){
            log.error("Failed to send activation email to: {}", toEmail, e);
        }

    }



    private String buildActivationEmailContent(String firstName, String activationLink) {
        return String.format("""
                Hi %s,
                
                Welcome to our RideOn App!
                
                Please click the link below to activate your account:
                %s
                
                This link will expire in 24 hours.
                
                If you didn't create an account, please ignore this email.
                
                Best regards,
                The RideOn App Team
                """, firstName, activationLink);
    }

    private String buildActivationEmailHtml(String firstName, String activationLink) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background-color: #f9f9f9; }
                        .button { display: inline-block; padding: 12px 24px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }
                        .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Welcome to RideOn App!</h1>
                        </div>
                        <div class="content">
                            <p>Hi %s,</p>
                            <p>Thank you for registering with our RideOn App. Please activate your account to get started.</p>
                            <p style="text-align: center;">
                                <a href="%s" class="button">Activate Account</a>
                            </p>
                            <p>Or copy and paste this link into your browser:</p>
                            <p style="word-break: break-all; color: #666;">%s</p>
                            <p><strong>Note:</strong> This link will expire in 24 hours.</p>
                            <p>If you didn't create an account, please ignore this email.</p>
                        </div>
                        <div class="footer">
                            <p>Best regards,<br>The RideOn App Team</p>
                        </div>
                    </div>
                </body>
                </html>
                """, firstName, activationLink, activationLink);
    }
}
