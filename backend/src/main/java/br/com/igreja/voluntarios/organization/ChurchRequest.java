package br.com.igreja.voluntarios.organization;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record ChurchRequest(@NotBlank @Size(max = 150) String name,
        @Size(max = 18) String cnpj, @Size(max = 100) String city, @Size(max = 2) String state) { }
