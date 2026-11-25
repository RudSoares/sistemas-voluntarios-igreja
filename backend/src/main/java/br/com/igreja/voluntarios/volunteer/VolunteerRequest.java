package br.com.igreja.voluntarios.volunteer;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VolunteerRequest(
        @NotBlank(message = "Informe o nome completo.")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
        String nomeCompleto,

        @Size(max = 40, message = "O telefone deve ter no máximo 40 caracteres.")
        String telefone,

        @Email(message = "Informe um e-mail válido.")
        @Size(max = 254, message = "O e-mail deve ter no máximo 254 caracteres.")
        String email,

        Set<@NotBlank @Size(max = 40) String> disponibilidadeDias,

        String observacoes,
        Long branchId,
        @Size(max = 72, message = "A senha inicial deve ter no máximo 72 caracteres.")
        String senhaInicial) {
}
