package br.com.igreja.voluntarios.ministry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MinistryRoleRequest(
        @NotBlank(message = "Informe o nome da função.")
        @Size(max = 120, message = "O nome da função deve ter no máximo 120 caracteres.")
        String nome) {
}
