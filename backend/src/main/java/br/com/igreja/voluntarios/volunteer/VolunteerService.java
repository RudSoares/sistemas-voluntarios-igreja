package br.com.igreja.voluntarios.volunteer;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VolunteerService {

    private final VolunteerRepository volunteerRepository;

    public VolunteerService(VolunteerRepository volunteerRepository) {
        this.volunteerRepository = volunteerRepository;
    }

    @Transactional(readOnly = true)
    public List<VolunteerResponse> listAll() {
        return volunteerRepository.findAllByOrderByNomeCompletoAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VolunteerResponse findById(Long id) {
        return toResponse(findEntity(id));
    }

    public VolunteerResponse create(VolunteerRequest request) {
        Volunteer volunteer = new Volunteer(
                request.nomeCompleto(),
                request.telefone(),
                request.email(),
                normalizeAvailability(request.disponibilidadeDias()),
                request.observacoes());
        return toResponse(volunteerRepository.save(volunteer));
    }

    public VolunteerResponse update(Long id, VolunteerRequest request) {
        Volunteer volunteer = findEntity(id);
        volunteer.updateDetails(
                request.nomeCompleto(),
                request.telefone(),
                request.email(),
                normalizeAvailability(request.disponibilidadeDias()),
                request.observacoes());
        return toResponse(volunteerRepository.save(volunteer));
    }

    public VolunteerResponse updateStatus(Long id, boolean ativo) {
        Volunteer volunteer = findEntity(id);
        volunteer.setAtivo(ativo);
        return toResponse(volunteerRepository.save(volunteer));
    }

    private Volunteer findEntity(Long id) {
        return volunteerRepository.findById(id)
                .orElseThrow(() -> new VolunteerNotFoundException(id));
    }

    private VolunteerResponse toResponse(Volunteer volunteer) {
        List<String> availability = volunteer.getDisponibilidadeDias().stream()
                .sorted(Comparator.naturalOrder())
                .toList();
        return new VolunteerResponse(
                volunteer.getId(),
                volunteer.getNomeCompleto(),
                volunteer.getTelefone(),
                volunteer.getEmail(),
                volunteer.isAtivo(),
                availability,
                volunteer.getObservacoes());
    }

    private Set<String> normalizeAvailability(Set<String> availability) {
        if (availability == null || availability.isEmpty()) {
            return Set.of();
        }
        TreeSet<String> normalized = new TreeSet<>();
        availability.stream()
                .map(String::trim)
                .forEach(normalized::add);
        return normalized;
    }
}
