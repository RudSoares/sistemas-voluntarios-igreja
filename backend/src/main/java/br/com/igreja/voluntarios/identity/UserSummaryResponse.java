package br.com.igreja.voluntarios.identity;
public record UserSummaryResponse(Long id, String nome, String email, UserRole role, Long churchId, Long branchId, boolean ativo) {
    public static UserSummaryResponse from(AppUser user) {
        return new UserSummaryResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(),
                user.getChurch() == null ? null : user.getChurch().getId(),
                user.getBranch() == null ? null : user.getBranch().getId(), user.isActive());
    }
}
