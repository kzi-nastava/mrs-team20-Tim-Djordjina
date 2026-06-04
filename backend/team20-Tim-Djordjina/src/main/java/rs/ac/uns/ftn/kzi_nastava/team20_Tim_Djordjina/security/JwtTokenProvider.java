package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpirationMs;


    /**
     * Generate JWT token for authenticated user
     */
    public String generateToken(String email, String role){
        log.debug("Generating JWT token for user: {}", email);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        String token = Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        log.info("JWT token generated successfully for user: {}", email);
        return token;
    }

    /**
     * Extract email from jwt token
     */
    public String getEmailFromToken(String token){
        try{
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch(Exception ex){
            log.error("Failed to get email from JWT token", ex);
            return null;
        }
    }

    /**
     * Extract role from jwt token
     */
    public String getRoleFromToken(String token){
        try{
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return (String)claims.get("role");
        } catch(Exception ex){
            log.error("Failed to get role from JWT token", ex);
            return null;
        }
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token){
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            log.debug("JWT token validated successfully");
            return true;
        } catch (Exception ex){
            log.error("JWT token validation failed", ex);
            return false;
        }
    }


}
