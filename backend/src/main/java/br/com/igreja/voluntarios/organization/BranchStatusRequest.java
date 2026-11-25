package br.com.igreja.voluntarios.organization;
import jakarta.validation.constraints.NotNull;
public record BranchStatusRequest(@NotNull Boolean active) { }
