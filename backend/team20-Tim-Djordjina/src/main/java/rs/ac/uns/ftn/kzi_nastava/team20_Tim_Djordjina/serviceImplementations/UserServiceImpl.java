/*
package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.serviceImplementations;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RegistrationRequestDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.entities.ActivationToken;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.entities.User;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repositories.ActivationTokenRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repositories.UserRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.services.EmailService;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.services.UserService;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ActivationTokenRepository tokenRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public void register(RegistrationRequestDTO request, MultipartFile profileImage) throws MessagingException {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setAddress(request.getAddress());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setCreatedAt(LocalDateTime.now());
        user.setActivated(false);

        if (profileImage != null && !profileImage.isEmpty()) {
            user.setProfileImageUrl(saveImage(profileImage));
        }

        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        ActivationToken activationToken = new ActivationToken(
                null,
                token,
                user,
                LocalDateTime.now().plusHours(24)
        );

        tokenRepository.save(activationToken);

        String link = "http://localhost:8080/auth/activate?token=" + token;
        emailService.sendActivationEmail(user.getEmail(), link);
    }

    @Override
    public void activateAccount(User user) {
        user.setActivated(true);
        userRepository.save(user);
    }

    private String saveImage(MultipartFile file) {
        //TODO add storage and entity for images
        return "uploads/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
    }
}*/
