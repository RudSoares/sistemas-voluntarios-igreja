package br.com.igreja.voluntarios.volunteer;
import java.util.Set;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record VolunteerAvailabilityRequest(@NotNull Set<@NotBlank @Size(max = 40) String> disponibilidadeDias) { }
