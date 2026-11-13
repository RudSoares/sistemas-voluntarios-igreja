package br.com.igreja.voluntarios.schedule;

import jakarta.validation.constraints.NotNull;

public record ScheduleAssignmentRequest(@NotNull Long ministryId, @NotNull Long ministryRoleId, @NotNull Long volunteerId) { }
