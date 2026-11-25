package br.com.igreja.voluntarios.ministry;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/ministries")
@PreAuthorize("hasRole('CHURCH_ADMIN')")
public class MinistryController {

    private final MinistryService ministryService;

    public MinistryController(MinistryService ministryService) {
        this.ministryService = ministryService;
    }

    @GetMapping
    public List<MinistryResponse> listMinistries() {
        return ministryService.listMinistries();
    }

    @GetMapping("/{ministryId}")
    public MinistryResponse findMinistry(@PathVariable Long ministryId) {
        return ministryService.findMinistry(ministryId);
    }

    @PostMapping
    public ResponseEntity<MinistryResponse> createMinistry(@Valid @RequestBody MinistryRequest request) {
        MinistryResponse created = ministryService.createMinistry(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{ministryId}")
    public MinistryResponse updateMinistry(
            @PathVariable Long ministryId,
            @Valid @RequestBody MinistryRequest request) {
        return ministryService.updateMinistry(ministryId, request);
    }

    @PatchMapping("/{ministryId}/status")
    public MinistryResponse updateMinistryStatus(
            @PathVariable Long ministryId,
            @Valid @RequestBody MinistryStatusRequest request) {
        return ministryService.updateMinistryStatus(ministryId, request.ativo());
    }

    @GetMapping("/{ministryId}/roles")
    public List<MinistryRoleResponse> listRoles(@PathVariable Long ministryId) {
        return ministryService.listRoles(ministryId);
    }

    @PostMapping("/{ministryId}/roles")
    public ResponseEntity<MinistryRoleResponse> createRole(
            @PathVariable Long ministryId,
            @Valid @RequestBody MinistryRoleRequest request) {
        MinistryRoleResponse created = ministryService.createRole(ministryId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{ministryId}/roles/{roleId}")
    public MinistryRoleResponse updateRole(
            @PathVariable Long ministryId,
            @PathVariable Long roleId,
            @Valid @RequestBody MinistryRoleRequest request) {
        return ministryService.updateRole(ministryId, roleId, request);
    }

    @DeleteMapping("/{ministryId}/roles/{roleId}")
    public ResponseEntity<Void> deleteRole(
            @PathVariable Long ministryId,
            @PathVariable Long roleId) {
        ministryService.deleteRole(ministryId, roleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{ministryId}/memberships")
    public List<VolunteerMinistryResponse> listMemberships(@PathVariable Long ministryId) {
        return ministryService.listMemberships(ministryId);
    }

    @GetMapping("/{ministryId}/memberships/{membershipId}")
    public VolunteerMinistryResponse findMembership(
            @PathVariable Long ministryId,
            @PathVariable Long membershipId) {
        return ministryService.findMembership(ministryId, membershipId);
    }

    @PostMapping("/{ministryId}/memberships")
    public ResponseEntity<VolunteerMinistryResponse> linkVolunteer(
            @PathVariable Long ministryId,
            @Valid @RequestBody VolunteerMinistryRequest request) {
        VolunteerMinistryResponse created = ministryService.linkVolunteer(ministryId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @DeleteMapping("/{ministryId}/memberships/{membershipId}")
    public ResponseEntity<Void> unlinkVolunteer(
            @PathVariable Long ministryId,
            @PathVariable Long membershipId) {
        ministryService.unlinkVolunteer(ministryId, membershipId);
        return ResponseEntity.noContent().build();
    }
}
