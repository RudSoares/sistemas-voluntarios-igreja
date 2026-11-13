package br.com.igreja.voluntarios.ministry;

import jakarta.validation.constraints.NotNull;

public record VolunteerMinistryRequest(
        @NotNull(message = "Selecione um voluntário.")
        Long volunteerId,

        @NotNull(message = "Selecione uma função do ministério.")
        Long ministryRoleId) {
}
