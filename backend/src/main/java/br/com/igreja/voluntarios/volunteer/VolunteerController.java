package br.com.igreja.voluntarios.volunteer;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/volunteers")
public class VolunteerController {

    private final VolunteerService volunteerService;

    public VolunteerController(VolunteerService volunteerService) {
        this.volunteerService = volunteerService;
    }

    @GetMapping
    @PreAuthorize("hasRole('CHURCH_ADMIN')")
    public List<VolunteerResponse> list() {
        return volunteerService.listAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CHURCH_ADMIN')")
    public VolunteerResponse findById(@PathVariable Long id) {
        return volunteerService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('CHURCH_ADMIN')")
    public ResponseEntity<VolunteerResponse> create(@Valid @RequestBody VolunteerRequest request) {
        VolunteerResponse created = volunteerService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CHURCH_ADMIN')")
    public VolunteerResponse update(
            @PathVariable Long id,
            @Valid @RequestBody VolunteerRequest request) {
        return volunteerService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('CHURCH_ADMIN')")
    public VolunteerResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody VolunteerStatusRequest request) {
        return volunteerService.updateStatus(id, request.ativo());
    }

    @PatchMapping("/me/availability")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public VolunteerResponse updateMyAvailability(@Valid @RequestBody VolunteerAvailabilityRequest request) {
        return volunteerService.updateMyAvailability(request.disponibilidadeDias());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public VolunteerResponse me() { return volunteerService.findMe(); }
}
