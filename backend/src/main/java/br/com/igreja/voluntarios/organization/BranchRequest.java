package br.com.igreja.voluntarios.organization;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record BranchRequest(@NotBlank @Size(max = 150) String name,
        @Size(max = 255) String address, @Size(max = 100) String city) { }
