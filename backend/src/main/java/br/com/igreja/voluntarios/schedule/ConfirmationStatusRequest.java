package br.com.igreja.voluntarios.schedule;

import jakarta.validation.constraints.NotNull;

public record ConfirmationStatusRequest(@NotNull ConfirmationStatus statusConfirmacao) { }
