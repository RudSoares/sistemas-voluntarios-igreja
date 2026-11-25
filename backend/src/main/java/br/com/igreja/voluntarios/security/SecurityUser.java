package br.com.igreja.voluntarios.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

public final class SecurityUser {
    private SecurityUser() { }
    public static TenantPrincipal current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof TenantPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login para continuar.");
        }
        return principal;
    }
    public static Long churchId() {
        Long id = current().churchId();
        if (id == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário sem igreja vinculada.");
        return id;
    }
}
