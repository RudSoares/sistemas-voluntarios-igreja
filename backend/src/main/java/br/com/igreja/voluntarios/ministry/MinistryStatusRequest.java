package br.com.igreja.voluntarios.ministry;

import jakarta.validation.constraints.NotNull;

public record MinistryStatusRequest(
        @NotNull(message = "Informe se o ministério está ativo.")
        Boolean ativo) {
}
