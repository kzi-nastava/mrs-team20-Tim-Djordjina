/*
package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controllers;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RegistrationRequestDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.entities.ActivationToken;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.entities.User;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repositories.ActivationTokenRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.services.UserService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private ActivationTokenRepository tokenRepository;


    @PostMapping(
            value = "/register",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> register(
            @RequestPart("data") RegistrationRequestDTO request,
            @RequestPart(value = "image", required = false) MultipartFile image) throws MessagingException {

        userService.register(request, image);
        return ResponseEntity.ok("Registration successful. Check email to activate the account.");
    }


    @GetMapping("/activate")
    public ResponseEntity<?> activate(@RequestParam String token) {

        ActivationToken t = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (t.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        userService.activateAccount(t.getUser());
        return ResponseEntity.ok("Account activated");
    }

}*/
