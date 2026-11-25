package br.com.igreja.voluntarios.organization;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/branches")
@PreAuthorize("hasRole('CHURCH_ADMIN')")
public class BranchController {
    private final OrganizationService service;
    public BranchController(OrganizationService service) { this.service = service; }
    @GetMapping public List<BranchResponse> list() { return service.listBranches(); }
    @PostMapping public BranchResponse create(@Valid @RequestBody BranchRequest request) { return service.createBranch(request); }
    @PutMapping("/{id}") public BranchResponse update(@PathVariable Long id, @Valid @RequestBody BranchRequest request) { return service.updateBranch(id, request); }
    @PatchMapping("/{id}/status") public BranchResponse status(@PathVariable Long id, @Valid @RequestBody BranchStatusRequest request) { return service.updateBranchStatus(id, request.active()); }
}
