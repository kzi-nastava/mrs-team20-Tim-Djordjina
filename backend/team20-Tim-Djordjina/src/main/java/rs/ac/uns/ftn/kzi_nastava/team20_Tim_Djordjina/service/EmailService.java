package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.Resolution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${sendgrid.from.name}")
    private String fromName;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    /**
     * Send activation email asynchronously using Gmail SMTP
     */
    @Async
    public void sendActivationEmail(String toEmail, String firstName, String token){
        try{
            String activationLink = baseUrl + "/api/auth/activate?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Activate Your Account: - RideOn App");
            message.setText(buildActivationEmailContent(firstName, activationLink));

            mailSender.send(message);

            log.info("Activation email (plain text) sent successfully to: {}", toEmail);
        }
        catch (Exception e){
            log.error("Failed to send activation email to: {}", toEmail, e);
        }

    }

    /**
     * Send HTML email using Gmail SMTP
     */
    @Async
    public void sendActivationEmailHtml(String toEmail, String firstName, String token){
        try{
            String activationLink = baseUrl + "/api/auth/activate?token=" + token;

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Activate Your Account: - RideOn App");
            helper.setText(buildActivationEmailHtml(firstName, activationLink), true);

            mailSender.send(mimeMessage);

            log.info("Activation email sent successfully to: {}", toEmail);
        }
        catch (MessagingException e){
            log.error("Failed to build/send HTML activation email to: {}", toEmail, e);
        }
        catch (Exception e){
            log.error("Unexpected error sending HTML activation email to: {}", toEmail, e);
        }

    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String firstName, String token){
        try {
            String resetLink = baseUrl + "/api/auth/reset-redirect?token=" + token;

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Reset Your Password - RideOn App");
            helper.setText(buildPasswordResetHtml(firstName, resetLink), true);

            mailSender.send(mimeMessage);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to build/send password reset email to: {}", toEmail, e);
        } catch (Exception e){
            log.error("Unexpected error sending password reset email to: {}", toEmail, e);
        }
    }

    @Async
    public void sendDriverWelcomeEmail(String toEmail, String firstName, String token){
        try{
            String setPasswordLink = baseUrl + "/api/auth/reset-redirect?token=" + token;

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to RideOn - Set Your Driver Password");
            helper.setText(buildDriverWelcomeHtml(firstName, setPasswordLink), true);

            mailSender.send(mimeMessage);

            log.info("Driver welcome email sent to: {}", toEmail);
        }
        catch (MessagingException e){
            log.error("Failed to build/send driver welcome email to: {}", toEmail, e);
        }
        catch (Exception e){
            log.error("Unexpected error sending driver welcome email to: {}", toEmail, e);
        }

    }

    /**
     * Send a plain-text notification email
     * */
    @Async
    public void sendNotificationEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Notification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send notification email to: {}", toEmail, e);
        }
    }

    private String buildDriverWelcomeHtml(String firstName, String setPasswordLink) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #0A1233; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background-color: #f9f9f9; }
                        .button { display: inline-block; padding: 12px 24px; background-color: #0A1233; color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }
                        .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header"><h1>Welcome to RideOn</h1></div>
                        <div class="content">
                            <p>Hi %s,</p>
                            <p>An administrator has created a driver account for you on RideOn.
                               Open this on your phone to set your password and get started:</p>
                            <p style="text-align: center;">
                                <a href="%s" class="button">Set Your Password</a>
                            </p>
                            <p><strong>Note:</strong> this link expires in 72 hours.</p>
                        </div>
                        <div class="footer"><p>The RideOn App Team</p></div>
                    </div>
                </body>
                </html>
                """, firstName, setPasswordLink);
    }

    private String buildPasswordResetHtml(String firstName, String resetLink) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                    <head>
                        <style>
                            body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                            .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                            .header { background-color: #0A1233; color: white; padding: 20px; text-align: center; }
                            .content { padding: 20px; background-color: #f9f9f9; }
                            .button { display: inline-block; padding: 12px 24px; background-color: #0A1233; color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }
                            .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header"><h1>Password Reset</h1></div>
                            <div class="content">
                                <p>Hi %s,</p>
                                <p>We received a request to reset your RideOn password. Open this on your phone to continue:</p>
                                <p style="text-align: center;">
                                    <a href="%s" class="button">Reset Password</a>
                                </p>
                                <p><strong>Note:</strong> this link expires in 1 hour and can be used once.</p>
                                <p>If you didn't request this, you can safely ignore this email.</p>
                            </div>
                            <div class="footer"><p>The RideOn App Team</p></div>
                        </div>
                    </body>
                </html>""", firstName, resetLink);
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
