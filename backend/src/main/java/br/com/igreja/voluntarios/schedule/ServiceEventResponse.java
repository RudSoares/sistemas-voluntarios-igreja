package br.com.igreja.voluntarios.schedule;

import java.time.LocalDate;
import java.time.LocalTime;

public record ServiceEventResponse(Long id, String titulo, LocalDate dataEvento, LocalTime horario,
        String turnoOuDiaSemana, String observacoes, EventStatus status) {
    static ServiceEventResponse from(ServiceEvent event) {
        return new ServiceEventResponse(event.getId(), event.getTitulo(), event.getDataEvento(),
                event.getHorario(), event.getTurnoOuDiaSemana(), event.getObservacoes(), event.getStatus());
    }
}
