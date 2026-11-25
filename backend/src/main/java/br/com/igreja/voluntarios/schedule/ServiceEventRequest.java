package br.com.igreja.voluntarios.schedule;

import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ServiceEventRequest(
        @NotBlank String titulo,
        @NotNull LocalDate dataEvento,
        @NotNull LocalTime horario,
        @NotBlank String turnoOuDiaSemana,
        String observacoes,
        Long branchId) { }
