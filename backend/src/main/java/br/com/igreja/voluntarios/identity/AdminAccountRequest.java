package br.com.igreja.voluntarios.identity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record AdminAccountRequest(@NotBlank @Size(max = 150) String nome, @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 8, max = 72) String senhaInicial, Long branchId) { }
