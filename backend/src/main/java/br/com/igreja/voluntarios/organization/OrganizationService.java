package br.com.igreja.voluntarios.organization;

import java.util.List;
import br.com.igreja.voluntarios.identity.AppUser;
import br.com.igreja.voluntarios.identity.AppUserRepository;
import br.com.igreja.voluntarios.identity.AdminAccountRequest;
import br.com.igreja.voluntarios.identity.UserRole;
import br.com.igreja.voluntarios.identity.UserSummaryResponse;
import br.com.igreja.voluntarios.security.SecurityUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class OrganizationService {
    private final ChurchRepository churches;
    private final BranchRepository branches;
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    public OrganizationService(ChurchRepository churches, BranchRepository branches, AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.churches = churches; this.branches = branches; this.users = users; this.passwordEncoder = passwordEncoder;
    }
    @Transactional(readOnly = true)
    public List<ChurchResponse> listChurches() { return churches.findAllByOrderByNameAsc().stream().map(ChurchResponse::from).toList(); }
    public ChurchResponse createChurch(ChurchRequest request) {
        return ChurchResponse.from(churches.save(new Church(request.name(), request.cnpj(), request.city(), request.state())));
    }
    public ChurchResponse updateChurch(Long id, ChurchRequest request) {
        Church church = requireChurch(id);
        church.update(request.name(), request.cnpj(), request.city(), request.state());
        return ChurchResponse.from(church);
    }
    public ChurchResponse updateChurchStatus(Long id, boolean active) {
        Church church = requireChurch(id); church.setActive(active); return ChurchResponse.from(church);
    }
    public UserSummaryResponse createChurchAdmin(Long churchId, AdminAccountRequest request) {
        Church church = requireChurch(churchId);
        if (!church.isActive()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Ative a igreja antes de criar administradores.");
        if (users.existsByEmailIgnoreCase(request.email())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta com este e-mail.");
        Branch branch = request.branchId() == null ? null : branches.findByIdAndChurch_Id(request.branchId(), churchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filial inválida para esta igreja."));
        AppUser user = new AppUser(request.nome(), request.email(), passwordEncoder.encode(request.senhaInicial()),
                UserRole.CHURCH_ADMIN, church, branch, null);
        return UserSummaryResponse.from(users.save(user));
    }
    @Transactional(readOnly = true)
    public List<BranchResponse> listBranches() { return branches.findAllByChurch_IdOrderByNameAsc(SecurityUser.churchId()).stream().map(BranchResponse::from).toList(); }
    public BranchResponse createBranch(BranchRequest request) {
        Church church = requireChurch(SecurityUser.churchId());
        if (!church.isActive()) throw new ResponseStatusException(HttpStatus.CONFLICT, "A igreja está inativa.");
        return BranchResponse.from(branches.save(new Branch(request.name(), request.address(), request.city(), church)));
    }
    public BranchResponse updateBranch(Long id, BranchRequest request) {
        Branch branch = requireBranch(id); branch.update(request.name(), request.address(), request.city()); return BranchResponse.from(branch);
    }
    public BranchResponse updateBranchStatus(Long id, boolean active) {
        Branch branch = requireBranch(id); branch.setActive(active); return BranchResponse.from(branch);
    }
    private Church requireChurch(Long id) {
        return churches.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Igreja não encontrada."));
    }
    private Branch requireBranch(Long id) {
        return branches.findByIdAndChurch_Id(id, SecurityUser.churchId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Filial não encontrada nesta igreja."));
    }
}
