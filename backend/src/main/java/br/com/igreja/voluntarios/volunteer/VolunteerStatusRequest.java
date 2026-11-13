package br.com.igreja.voluntarios.volunteer;

import jakarta.validation.constraints.NotNull;

public record VolunteerStatusRequest(
        @NotNull(message = "Informe se o voluntário está ativo.")
        Boolean ativo) {
}
