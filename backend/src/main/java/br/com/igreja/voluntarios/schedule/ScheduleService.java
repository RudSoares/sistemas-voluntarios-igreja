package br.com.igreja.voluntarios.schedule;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import br.com.igreja.voluntarios.ministry.Ministry;
import br.com.igreja.voluntarios.ministry.MinistryRepository;
import br.com.igreja.voluntarios.ministry.MinistryRole;
import br.com.igreja.voluntarios.ministry.MinistryRoleRepository;
import br.com.igreja.voluntarios.ministry.VolunteerMinistryRepository;
import br.com.igreja.voluntarios.organization.Branch;
import br.com.igreja.voluntarios.organization.BranchRepository;
import br.com.igreja.voluntarios.organization.Church;
import br.com.igreja.voluntarios.organization.ChurchRepository;
import br.com.igreja.voluntarios.security.SecurityUser;
import br.com.igreja.voluntarios.security.TenantPrincipal;
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
    private final BranchRepository branchRepository;
    private final ChurchRepository churchRepository;

    public ScheduleService(ServiceEventRepository eventRepository, ScheduleAssignmentRepository assignmentRepository,
            MinistryRepository ministryRepository, MinistryRoleRepository roleRepository,
            VolunteerRepository volunteerRepository, VolunteerMinistryRepository membershipRepository,
            BranchRepository branchRepository, ChurchRepository churchRepository) {
        this.eventRepository = eventRepository;
        this.assignmentRepository = assignmentRepository;
        this.ministryRepository = ministryRepository;
        this.roleRepository = roleRepository;
        this.volunteerRepository = volunteerRepository;
        this.membershipRepository = membershipRepository;
        this.branchRepository = branchRepository;
        this.churchRepository = churchRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceEventResponse> listEvents() {
        return eventRepository.findAllByChurch_IdOrderByDataEventoAscHorarioAsc(SecurityUser.churchId()).stream().map(ServiceEventResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ServiceEventResponse findEvent(Long id) { return ServiceEventResponse.from(requireEvent(id)); }

    public ServiceEventResponse createEvent(ServiceEventRequest request) {
        TenantPrincipal principal = SecurityUser.current();
        var church = requireEventChurch();
        return ServiceEventResponse.from(eventRepository.save(new ServiceEvent(request.titulo(), request.dataEvento(),
                request.horario(), request.turnoOuDiaSemana(), request.observacoes(), church,
                currentBranch(request.branchId(), principal, church.getId()))));
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
        return eventRepository.findAllByChurch_IdAndStatusAndDataEventoGreaterThanEqualOrderByDataEventoAscHorarioAsc(
                SecurityUser.churchId(), EventStatus.AGENDADO, LocalDate.now()).stream().map(ServiceEventResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleAssignmentResponse> listAssignments(Long eventId) {
        TenantPrincipal principal = SecurityUser.current();
        List<ScheduleAssignment> assignments;
        if (principal.role() == br.com.igreja.voluntarios.identity.UserRole.VOLUNTEER) {
            assignments = assignmentRepository.findAllByVolunteer_IdAndChurch_Id(principal.volunteerId(), SecurityUser.churchId());
        } else {
            assignments = eventId == null
                    ? assignmentRepository.findAllByChurch_Id(SecurityUser.churchId())
                    : assignmentRepository.findAllByServiceEvent_IdAndChurch_Id(eventId, SecurityUser.churchId());
        }
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
        Volunteer volunteer = volunteerRepository.findByIdAndChurch_Id(request.volunteerId(), SecurityUser.churchId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voluntário não encontrado."));
        if (!volunteer.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este voluntário está inativo e não pode ser escalado.");
        }
        Ministry ministry = ministryRepository.findByIdAndChurch_Id(request.ministryId(), SecurityUser.churchId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ministério não encontrado."));
        if (!ministry.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este ministério está inativo e não pode receber escalas.");
        }
        MinistryRole role = roleRepository.findByIdAndMinistry_Id(request.ministryRoleId(), ministry.getId())
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
        return toResponse(assignmentRepository.save(new ScheduleAssignment(event, ministry, role, volunteer,
                event.getChurch(), event.getBranch())));
    }

    public ScheduleAssignmentResponse updateConfirmation(Long assignmentId, ConfirmationStatus status) {
        TenantPrincipal principal = SecurityUser.current();
        ScheduleAssignment assignment = assignmentRepository.findByIdAndChurch_Id(assignmentId, SecurityUser.churchId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Escala não encontrada."));
        if (principal.role() == br.com.igreja.voluntarios.identity.UserRole.VOLUNTEER
                && !assignment.getVolunteer().getId().equals(principal.volunteerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode responder às suas próprias escalas.");
        }
        if (principal.role() == br.com.igreja.voluntarios.identity.UserRole.VOLUNTEER
                && status == ConfirmationStatus.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voluntários podem confirmar ou recusar a escala.");
        }
        assignment.setStatusConfirmacao(status);
        return toResponse(assignmentRepository.save(assignment));
    }

    public void deleteAssignment(Long assignmentId) {
        ScheduleAssignment assignment = assignmentRepository.findByIdAndChurch_Id(assignmentId, SecurityUser.churchId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Escala não encontrada."));
        assignmentRepository.delete(assignment);
    }

    private ServiceEvent requireEvent(Long id) {
        return eventRepository.findByIdAndChurch_Id(id, SecurityUser.churchId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Culto/evento não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<ScheduleAssignmentResponse> listMyAssignments() {
        TenantPrincipal principal = SecurityUser.current();
        if (principal.volunteerId() == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário sem cadastro de voluntário.");
        return assignmentRepository.findAllByVolunteer_IdAndChurch_Id(principal.volunteerId(), SecurityUser.churchId())
                .stream().map(this::toResponse).toList();
    }

    private Church requireEventChurch() {
        Long churchId = SecurityUser.churchId();
        return churchRepository.findById(churchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Igreja do usuário não encontrada."));
    }

    private Branch currentBranch(Long requestedBranchId, TenantPrincipal principal, Long churchId) {
        Long branchId = requestedBranchId == null ? principal.branchId() : requestedBranchId;
        if (branchId == null) return null;
        return branchRepository.findByIdAndChurch_Id(branchId, churchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Filial inválida para esta igreja."));
    }

    private ScheduleAssignmentResponse toResponse(ScheduleAssignment assignment) {
        boolean available = assignment.getVolunteer().getDisponibilidadeDias().stream()
                .anyMatch(day -> day.equalsIgnoreCase(assignment.getServiceEvent().getTurnoOuDiaSemana()));
        return ScheduleAssignmentResponse.from(assignment, !available,
                available ? null : "A disponibilidade cadastrada não inclui "
                        + assignment.getServiceEvent().getTurnoOuDiaSemana().replace('_', ' ').toLowerCase() + ".");
    }
}
