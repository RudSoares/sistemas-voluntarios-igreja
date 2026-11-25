package br.com.igreja.voluntarios.security;

import br.com.igreja.voluntarios.identity.UserRole;

public record TenantPrincipal(Long userId, String email, UserRole role, Long churchId, Long branchId, Long volunteerId) { }
