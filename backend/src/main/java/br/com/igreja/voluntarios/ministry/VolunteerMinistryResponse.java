package br.com.igreja.voluntarios.ministry;

public record VolunteerMinistryResponse(
        Long id,
        Long volunteerId,
        String volunteerNomeCompleto,
        Long ministryId,
        Long ministryRoleId,
        String ministryRoleNome) {
}
