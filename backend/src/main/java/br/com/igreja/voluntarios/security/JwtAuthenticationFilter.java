package br.com.igreja.voluntarios.security;

import java.io.IOException;
import java.util.List;

import br.com.igreja.voluntarios.identity.AppUser;
import br.com.igreja.voluntarios.identity.AppUserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenService tokens;
    private final AppUserRepository users;
    public JwtAuthenticationFilter(JwtTokenService tokens, AppUserRepository users) { this.tokens = tokens; this.users = users; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) { chain.doFilter(request, response); return; }
        try {
            String email = tokens.parseEmail(header.substring(7));
            AppUser user = users.findByEmailIgnoreCase(email).orElse(null);
            if (user == null || !user.isActive()
                    || (user.getChurch() != null && !user.getChurch().isActive())
                    || (user.getVolunteer() != null && !user.getVolunteer().isAtivo())) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            TenantPrincipal principal = new TenantPrincipal(user.getId(), user.getEmail(), user.getRole(),
                    user.getChurch() == null ? null : user.getChurch().getId(),
                    user.getBranch() == null ? null : user.getBranch().getId(),
                    user.getVolunteer() == null ? null : user.getVolunteer().getId());
            var authentication = new UsernamePasswordAuthenticationToken(principal, null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException | IllegalArgumentException invalid) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido ou expirado.");
            return;
        }
        chain.doFilter(request, response);
    }
}
