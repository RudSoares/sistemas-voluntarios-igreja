package br.com.igreja.voluntarios.organization;
import jakarta.validation.constraints.NotNull;
public record ChurchStatusRequest(@NotNull Boolean active) { }
