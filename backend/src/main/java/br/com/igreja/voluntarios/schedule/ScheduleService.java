package br.com.igreja.voluntarios.schedule;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import br.com.igreja.voluntarios.ministry.Ministry;
import br.com.igreja.voluntarios.ministry.MinistryRepository;
import br.com.igreja.voluntarios.ministry.MinistryRole;
import br.com.igreja.voluntarios.ministry.MinistryRoleRepository;
import br.com.igreja.voluntarios.ministry.VolunteerMinistryRepository;
import br.com.igreja.voluntarios.volunteer.Volunteer;
import br.com.igreja.voluntarios.volunteer.VolunteerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ScheduleService {
    private final ServiceEventRepository eventRepository;
    private final ScheduleAssignmentRepository assignmentRepository;
    private final MinistryRepository ministryRepository;
    private final MinistryRoleRepository roleRepository;
    private final VolunteerRepository volunteerRepository;
    private final VolunteerMinistryRepository membershipRepository;

    public ScheduleService(ServiceEventRepository eventRepository, ScheduleAssignmentRepository assignmentRepository,
            MinistryRepository ministryRepository, MinistryRoleRepository roleRepository,
            VolunteerRepository volunteerRepository, VolunteerMinistryRepository membershipRepository) {
        this.eventRepository = eventRepository;
        this.assignmentRepository = assignmentRepository;
        this.ministryRepository = ministryRepository;
        this.roleRepository = roleRepository;
        this.volunteerRepository = volunteerRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceEventResponse> listEvents() {
        return eventRepository.findAllByOrderByDataEventoAscHorarioAsc().stream().map(ServiceEventResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ServiceEventResponse findEvent(Long id) { return ServiceEventResponse.from(requireEvent(id)); }

    public ServiceEventResponse createEvent(ServiceEventRequest request) {
        return ServiceEventResponse.from(eventRepository.save(new ServiceEvent(request.titulo(), request.dataEvento(),
                request.horario(), request.turnoOuDiaSemana(), request.observacoes())));
    }

    public ServiceEventResponse updateEvent(Long id, ServiceEventRequest request) {
        ServiceEvent event = requireEvent(id);
        if (event.getStatus() != EventStatus.AGENDADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Somente eventos agendados podem ser editados.");
        }
        event.update(request.titulo(), request.dataEvento(), request.horario(), request.turnoOuDiaSemana(), request.observacoes());
        return ServiceEventResponse.from(eventRepository.save(event));
    }

    public ServiceEventResponse updateEventStatus(Long id, EventStatus status) {
        ServiceEvent event = requireEvent(id);
        event.setStatus(status);
        return ServiceEventResponse.from(eventRepository.save(event));
    }

    public void deleteEvent(Long id) {
        ServiceEvent event = requireEvent(id);
        if (assignmentRepository.existsByServiceEvent_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este evento possui escalas. Cancele-o ou remova as escalas antes de excluir.");
        }
        eventRepository.delete(event);
    }

    @Transactional(readOnly = true)
    public List<ServiceEventResponse> listUpcomingEvents() {
        return eventRepository.findAllByStatusAndDataEventoGreaterThanEqualOrderByDataEventoAscHorarioAsc(
                EventStatus.AGENDADO, LocalDate.now()).stream().map(ServiceEventResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleAssignmentResponse> listAssignments(Long eventId) {
        List<ScheduleAssignment> assignments = eventId == null
                ? assignmentRepository.findAll()
                : assignmentRepository.findAllByServiceEvent_Id(eventId);
        return assignments.stream()
                .sorted(Comparator.comparing((ScheduleAssignment a) -> a.getServiceEvent().getDataEvento())
                        .thenComparing(a -> a.getServiceEvent().getHorario())
                        .thenComparing(a -> a.getMinistry().getNome(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(a -> a.getMinistryRole().getNome(), String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse).toList();
    }

    public ScheduleAssignmentResponse createAssignment(Long eventId, ScheduleAssignmentRequest request) {
        ServiceEvent event = requireEvent(eventId);
        if (event.getStatus() != EventStatus.AGENDADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Só é possível escalar pessoas em eventos agendados.");
        }
        Volunteer volunteer = volunteerRepository.findById(request.volunteerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voluntário não encontrado."));
        if (!volunteer.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este voluntário está inativo e não pode ser escalado.");
        }
        Ministry ministry = ministryRepository.findById(request.ministryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ministério não encontrado."));
        if (!ministry.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este ministério está inativo e não pode receber escalas.");
        }
        MinistryRole role = roleRepository.findById(request.ministryRoleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Função não encontrada."));
        if (!role.getMinistry().getId().equals(ministry.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A função selecionada não pertence a este ministério.");
        }
        if (!membershipRepository.existsByVolunteer_IdAndMinistry_IdAndRole_Id(
                volunteer.getId(), ministry.getId(), role.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Vincule primeiro este voluntário à função selecionada no ministério.");
        }
        if (assignmentRepository.existsByServiceEvent_IdAndVolunteer_Id(eventId, volunteer.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este voluntário já está escalado neste evento, mesmo que em outra função.");
        }
        return toResponse(assignmentRepository.save(new ScheduleAssignment(event, ministry, role, volunteer)));
    }

    public ScheduleAssignmentResponse updateConfirmation(Long assignmentId, ConfirmationStatus status) {
        ScheduleAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Escala não encontrada."));
        assignment.setStatusConfirmacao(status);
        return toResponse(assignmentRepository.save(assignment));
    }

    public void deleteAssignment(Long assignmentId) {
        ScheduleAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Escala não encontrada."));
        assignmentRepository.delete(assignment);
    }

    private ServiceEvent requireEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Culto/evento não encontrado."));
    }

    private ScheduleAssignmentResponse toResponse(ScheduleAssignment assignment) {
        boolean available = assignment.getVolunteer().getDisponibilidadeDias().stream()
                .anyMatch(day -> day.equalsIgnoreCase(assignment.getServiceEvent().getTurnoOuDiaSemana()));
        return ScheduleAssignmentResponse.from(assignment, !available,
                available ? null : "A disponibilidade cadastrada não inclui "
                        + assignment.getServiceEvent().getTurnoOuDiaSemana().replace('_', ' ').toLowerCase() + ".");
    }
}
