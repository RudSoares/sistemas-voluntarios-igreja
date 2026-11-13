package br.com.igreja.voluntarios.schedule;

import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleAssignmentResponse(Long id, Long eventId, String eventTitulo, LocalDate dataEvento,
        LocalTime horario, String turnoOuDiaSemana, Long ministryId, String ministryNome,
        Long ministryRoleId, String ministryRoleNome, Long volunteerId, String volunteerNomeCompleto,
        ConfirmationStatus statusConfirmacao, boolean alertaDisponibilidade, String mensagemDisponibilidade) {
    static ScheduleAssignmentResponse from(ScheduleAssignment assignment, boolean alert, String message) {
        ServiceEvent event = assignment.getServiceEvent();
        return new ScheduleAssignmentResponse(assignment.getId(), event.getId(), event.getTitulo(),
                event.getDataEvento(), event.getHorario(), event.getTurnoOuDiaSemana(),
                assignment.getMinistry().getId(), assignment.getMinistry().getNome(),
                assignment.getMinistryRole().getId(), assignment.getMinistryRole().getNome(),
                assignment.getVolunteer().getId(), assignment.getVolunteer().getNomeCompleto(),
                assignment.getStatusConfirmacao(), alert, message);
    }
}
