package br.com.igreja.voluntarios.organization;
import java.util.List;
import br.com.igreja.voluntarios.identity.AdminAccountRequest;
import br.com.igreja.voluntarios.identity.UserSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/churches")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class ChurchController {
    private final OrganizationService service;
    public ChurchController(OrganizationService service) { this.service = service; }
    @GetMapping public List<ChurchResponse> list() { return service.listChurches(); }
    @PostMapping public ChurchResponse create(@Valid @RequestBody ChurchRequest request) { return service.createChurch(request); }
    @PutMapping("/{id}") public ChurchResponse update(@PathVariable Long id, @Valid @RequestBody ChurchRequest request) { return service.updateChurch(id, request); }
    @PatchMapping("/{id}/status") public ChurchResponse status(@PathVariable Long id, @Valid @RequestBody ChurchStatusRequest request) { return service.updateChurchStatus(id, request.active()); }
    @PostMapping("/{churchId}/admins") public UserSummaryResponse addAdmin(@PathVariable Long churchId, @Valid @RequestBody AdminAccountRequest request) { return service.createChurchAdmin(churchId, request); }
}
