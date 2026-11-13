package br.com.igreja.voluntarios.ministry;

import java.util.Comparator;
import java.util.List;

import br.com.igreja.voluntarios.volunteer.Volunteer;
import br.com.igreja.voluntarios.volunteer.VolunteerRepository;
import br.com.igreja.voluntarios.volunteer.VolunteerNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class MinistryService {

    private final MinistryRepository ministryRepository;
    private final MinistryRoleRepository ministryRoleRepository;
    private final VolunteerMinistryRepository volunteerMinistryRepository;
    private final VolunteerRepository volunteerRepository;

    public MinistryService(
            MinistryRepository ministryRepository,
            MinistryRoleRepository ministryRoleRepository,
            VolunteerMinistryRepository volunteerMinistryRepository,
            VolunteerRepository volunteerRepository) {
        this.ministryRepository = ministryRepository;
        this.ministryRoleRepository = ministryRoleRepository;
        this.volunteerMinistryRepository = volunteerMinistryRepository;
        this.volunteerRepository = volunteerRepository;
    }

    @Transactional(readOnly = true)
    public List<MinistryResponse> listMinistries() {
        return ministryRepository.findAllByOrderByNomeAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MinistryResponse findMinistry(Long id) {
        return toResponse(requireMinistry(id));
    }

    public MinistryResponse createMinistry(MinistryRequest request) {
        String nome = request.nome().trim();
        ensureNameIsAvailable(nome, null);
        return toResponse(ministryRepository.save(new Ministry(nome, request.descricao())));
    }

    public MinistryResponse updateMinistry(Long id, MinistryRequest request) {
        Ministry ministry = requireMinistry(id);
        String nome = request.nome().trim();
        ensureNameIsAvailable(nome, id);
        ministry.updateDetails(nome, request.descricao());
        return toResponse(ministryRepository.save(ministry));
    }

    public MinistryResponse updateMinistryStatus(Long id, boolean ativo) {
        Ministry ministry = requireMinistry(id);
        ministry.setAtivo(ativo);
        return toResponse(ministryRepository.save(ministry));
    }

    @Transactional(readOnly = true)
    public List<MinistryRoleResponse> listRoles(Long ministryId) {
        requireMinistry(ministryId);
        return ministryRoleRepository.findAllByMinistry_IdOrderByNomeAsc(ministryId).stream()
                .map(this::toResponse)
                .toList();
    }

    public MinistryRoleResponse createRole(Long ministryId, MinistryRoleRequest request) {
        Ministry ministry = requireMinistry(ministryId);
        MinistryRole role = new MinistryRole(request.nome().trim(), ministry);
        return toResponse(ministryRoleRepository.save(role));
    }

    public MinistryRoleResponse updateRole(Long ministryId, Long roleId, MinistryRoleRequest request) {
        MinistryRole role = requireRole(ministryId, roleId);
        role.rename(request.nome().trim());
        return toResponse(ministryRoleRepository.save(role));
    }

    public void deleteRole(Long ministryId, Long roleId) {
        MinistryRole role = requireRole(ministryId, roleId);
        if (volunteerMinistryRepository.existsByRole_Id(roleId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Desvincule os voluntários desta função antes de removê-la.");
        }
        ministryRoleRepository.delete(role);
    }

    @Transactional(readOnly = true)
    public List<VolunteerMinistryResponse> listMemberships(Long ministryId) {
        requireMinistry(ministryId);
        return volunteerMinistryRepository.findAllByMinistry_Id(ministryId).stream()
                .sorted(Comparator
                        .comparing((VolunteerMinistry membership) -> membership.getVolunteer().getNomeCompleto(),
                                String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(membership -> membership.getRole().getNome(),
                                String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VolunteerMinistryResponse findMembership(Long ministryId, Long membershipId) {
        requireMinistry(ministryId);
        return toResponse(requireMembership(ministryId, membershipId));
    }

    public VolunteerMinistryResponse linkVolunteer(Long ministryId, VolunteerMinistryRequest request) {
        Ministry ministry = requireMinistry(ministryId);
        Volunteer volunteer = volunteerRepository.findById(request.volunteerId())
                .orElseThrow(() -> new VolunteerNotFoundException(request.volunteerId()));
        MinistryRole role = requireRole(ministryId, request.ministryRoleId());

        if (volunteerMinistryRepository.existsByVolunteer_IdAndMinistry_IdAndRole_Id(
                volunteer.getId(), ministry.getId(), role.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este voluntário já está vinculado a esta função neste ministério.");
        }

        VolunteerMinistry membership = new VolunteerMinistry(volunteer, ministry, role);
        return toResponse(volunteerMinistryRepository.save(membership));
    }

    public void unlinkVolunteer(Long ministryId, Long membershipId) {
        volunteerMinistryRepository.delete(requireMembership(ministryId, membershipId));
    }

    private Ministry requireMinistry(Long id) {
        return ministryRepository.findById(id)
                .orElseThrow(() -> notFound("Ministério", id));
    }

    private MinistryRole requireRole(Long ministryId, Long roleId) {
        return ministryRoleRepository.findByIdAndMinistry_Id(roleId, ministryId)
                .orElseThrow(() -> notFound("Função", roleId));
    }

    private VolunteerMinistry requireMembership(Long ministryId, Long membershipId) {
        return volunteerMinistryRepository.findByIdAndMinistry_Id(membershipId, ministryId)
                .orElseThrow(() -> notFound("Vínculo", membershipId));
    }

    private void ensureNameIsAvailable(String nome, Long currentId) {
        boolean alreadyExists = currentId == null
                ? ministryRepository.existsByNomeIgnoreCase(nome)
                : ministryRepository.existsByNomeIgnoreCaseAndIdNot(nome, currentId);
        if (alreadyExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um ministério com esse nome.");
        }
    }

    private ResponseStatusException notFound(String tipo, Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, tipo + " não encontrado: " + id);
    }

    private MinistryResponse toResponse(Ministry ministry) {
        return new MinistryResponse(ministry.getId(), ministry.getNome(), ministry.getDescricao(), ministry.isAtivo());
    }

    private MinistryRoleResponse toResponse(MinistryRole role) {
        return new MinistryRoleResponse(role.getId(), role.getMinistry().getId(), role.getNome());
    }

    private VolunteerMinistryResponse toResponse(VolunteerMinistry membership) {
        return new VolunteerMinistryResponse(
                membership.getId(),
                membership.getVolunteer().getId(),
                membership.getVolunteer().getNomeCompleto(),
                membership.getMinistry().getId(),
                membership.getRole().getId(),
                membership.getRole().getNome());
    }
}
