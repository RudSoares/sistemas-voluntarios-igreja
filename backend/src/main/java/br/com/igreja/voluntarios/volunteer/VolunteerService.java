package br.com.igreja.voluntarios.volunteer;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import br.com.igreja.voluntarios.identity.AppUser;
import br.com.igreja.voluntarios.identity.AppUserRepository;
import br.com.igreja.voluntarios.identity.UserRole;
import br.com.igreja.voluntarios.organization.Branch;
import br.com.igreja.voluntarios.organization.BranchRepository;
import br.com.igreja.voluntarios.organization.Church;
import br.com.igreja.voluntarios.organization.ChurchRepository;
import br.com.igreja.voluntarios.security.SecurityUser;
import br.com.igreja.voluntarios.security.TenantPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class VolunteerService {

    private final VolunteerRepository volunteerRepository;
    private final ChurchRepository churchRepository;
    private final BranchRepository branchRepository;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public VolunteerService(VolunteerRepository volunteerRepository, ChurchRepository churchRepository,
            BranchRepository branchRepository, AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.volunteerRepository = volunteerRepository;
        this.churchRepository = churchRepository;
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<VolunteerResponse> listAll() {
        return volunteerRepository.findAllByChurch_IdOrderByNomeCompletoAsc(SecurityUser.churchId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VolunteerResponse findById(Long id) {
        return toResponse(findEntity(id));
    }

    public VolunteerResponse create(VolunteerRequest request) {
        TenantPrincipal principal = SecurityUser.current();
        Church church = churchRepository.findById(SecurityUser.churchId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Igreja do usuário não encontrada."));
        Branch branch = resolveBranch(request.branchId(), principal, church);
        Volunteer volunteer = new Volunteer(
                request.nomeCompleto(),
                request.telefone(),
                request.email(),
                normalizeAvailability(request.disponibilidadeDias()),
                request.observacoes(), church, branch);
        Volunteer saved = volunteerRepository.save(volunteer);
        if (request.senhaInicial() != null && !request.senhaInicial().isBlank()) {
            if (request.senhaInicial().length() < 8) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha inicial deve ter pelo menos 8 caracteres.");
            }
            if (saved.getEmail() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o e-mail para criar o acesso do voluntário.");
            }
            if (userRepository.existsByEmailIgnoreCase(saved.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta com este e-mail.");
            }
            userRepository.save(new AppUser(saved.getNomeCompleto(), saved.getEmail(),
                    passwordEncoder.encode(request.senhaInicial()), UserRole.VOLUNTEER, church, branch, saved));
        }
        return toResponse(saved);
    }

    public VolunteerResponse update(Long id, VolunteerRequest request) {
        Volunteer volunteer = findEntity(id);
        userRepository.findByVolunteer_Id(volunteer.getId()).ifPresent(account -> {
            if (request.email() == null || request.email().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "A conta deste voluntário exige um e-mail. Informe um endereço para salvar as alterações.");
            }
            if (userRepository.existsByEmailIgnoreCaseAndIdNot(request.email().trim(), account.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta com este e-mail.");
            }
            account.updateIdentity(request.nomeCompleto(), request.email());
        });
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
        userRepository.findByVolunteer_Id(volunteer.getId()).ifPresent(account -> account.setActive(ativo));
        return toResponse(volunteerRepository.save(volunteer));
    }

    private Volunteer findEntity(Long id) {
        return volunteerRepository.findByIdAndChurch_Id(id, SecurityUser.churchId())
                .orElseThrow(() -> new VolunteerNotFoundException(id));
    }

    public VolunteerResponse updateMyAvailability(Set<String> availability) {
        TenantPrincipal principal = SecurityUser.current();
        if (principal.volunteerId() == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário não possui cadastro de voluntário.");
        Volunteer volunteer = findEntity(principal.volunteerId());
        volunteer.updateDetails(volunteer.getNomeCompleto(), volunteer.getTelefone(), volunteer.getEmail(),
                normalizeAvailability(availability), volunteer.getObservacoes());
        return toResponse(volunteerRepository.save(volunteer));
    }

    @Transactional(readOnly = true)
    public VolunteerResponse findMe() {
        TenantPrincipal principal = SecurityUser.current();
        if (principal.volunteerId() == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário não possui cadastro de voluntário.");
        return toResponse(findEntity(principal.volunteerId()));
    }

    private Branch resolveBranch(Long requestedBranchId, TenantPrincipal principal, Church church) {
        Long branchId = requestedBranchId != null ? requestedBranchId : principal.branchId();
        if (branchId == null) return null;
        return branchRepository.findByIdAndChurch_Id(branchId, church.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filial inválida para esta igreja."));
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
