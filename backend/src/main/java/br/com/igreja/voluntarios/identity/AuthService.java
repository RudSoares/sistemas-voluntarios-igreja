package br.com.igreja.voluntarios.identity;

import br.com.igreja.voluntarios.security.JwtTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final JwtTokenService tokens;
    public AuthService(AppUserRepository users, PasswordEncoder encoder, JwtTokenService tokens) {
        this.users = users; this.encoder = encoder; this.tokens = tokens;
    }
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        AppUser user = users.findByEmailIgnoreCase(request.email().trim())
                .filter(AppUser::isActive)
                .filter(found -> found.getChurch() == null || found.getChurch().isActive())
                .filter(found -> found.getVolunteer() == null || found.getVolunteer().isAtivo())
                .filter(found -> encoder.matches(request.senha(), found.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos."));
        return new LoginResponse(tokens.create(user), user.getName(), user.getEmail(), user.getRole(),
                user.getChurch() == null ? null : user.getChurch().getId(),
                user.getBranch() == null ? null : user.getBranch().getId());
    }
}
