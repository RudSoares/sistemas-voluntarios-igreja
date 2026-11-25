package br.com.igreja.voluntarios.ministry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MinistryRequest(
        @NotBlank(message = "Informe o nome do ministério.")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        String nome,

        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres.")
        String descricao,
        Long branchId) {
}
