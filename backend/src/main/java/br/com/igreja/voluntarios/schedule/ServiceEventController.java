package br.com.igreja.voluntarios.schedule;

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

@RestController
@RequestMapping("/api/v1/events")
public class ServiceEventController {
    private final ScheduleService scheduleService;
    public ServiceEventController(ScheduleService scheduleService) { this.scheduleService = scheduleService; }

    @GetMapping public List<ServiceEventResponse> list() { return scheduleService.listEvents(); }
    @GetMapping("/upcoming") public List<ServiceEventResponse> upcoming() { return scheduleService.listUpcomingEvents(); }
    @GetMapping("/{id}") public ServiceEventResponse find(@PathVariable Long id) { return scheduleService.findEvent(id); }

    @PostMapping
    public ResponseEntity<ServiceEventResponse> create(@Valid @RequestBody ServiceEventRequest request) {
        ServiceEventResponse response = scheduleService.createEvent(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ServiceEventResponse update(@PathVariable Long id, @Valid @RequestBody ServiceEventRequest request) {
        return scheduleService.updateEvent(id, request);
    }

    @PatchMapping("/{id}/status")
    public ServiceEventResponse updateStatus(@PathVariable Long id, @Valid @RequestBody EventStatusRequest request) {
        return scheduleService.updateEventStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scheduleService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
