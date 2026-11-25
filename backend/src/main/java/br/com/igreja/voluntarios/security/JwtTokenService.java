package br.com.igreja.voluntarios.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import br.com.igreja.voluntarios.identity.AppUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
    private final SecretKey signingKey;
    private final long expirationSeconds;

    public JwtTokenService(
            @Value("${JWT_SECRET:dev-only-change-this-jwt-secret-before-deployment-2026}") String secret,
            @Value("${JWT_EXPIRATION_SECONDS:28800}") long expirationSeconds) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    public String create(AppUser user) {
        Instant now = Instant.now();
        return Jwts.builder().subject(user.getEmail())
                .claim("uid", user.getId()).claim("role", user.getRole().name())
                .claim("churchId", user.getChurch() == null ? null : user.getChurch().getId())
                .claim("branchId", user.getBranch() == null ? null : user.getBranch().getId())
                .claim("volunteerId", user.getVolunteer() == null ? null : user.getVolunteer().getId())
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(signingKey, Jwts.SIG.HS256).compact();
    }

    public String parseEmail(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload().getSubject();
    }
}
