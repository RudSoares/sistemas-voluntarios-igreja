package br.com.igreja.voluntarios.identity;
public record LoginResponse(String token, String nome, String email, UserRole role, Long churchId, Long branchId) { }
