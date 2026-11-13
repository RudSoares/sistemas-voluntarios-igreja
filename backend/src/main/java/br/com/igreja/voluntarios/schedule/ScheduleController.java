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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/schedules")
public class ScheduleController {
    private final ScheduleService scheduleService;
    public ScheduleController(ScheduleService scheduleService) { this.scheduleService = scheduleService; }

    @GetMapping
    public List<ScheduleAssignmentResponse> list(@RequestParam(required = false) Long eventId) {
        return scheduleService.listAssignments(eventId);
    }

    @PostMapping("/events/{eventId}/assignments")
    public ResponseEntity<ScheduleAssignmentResponse> create(
            @PathVariable Long eventId, @Valid @RequestBody ScheduleAssignmentRequest request) {
        ScheduleAssignmentResponse response = scheduleService.createAssignment(eventId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/assignments/{assignmentId}/confirmation")
    public ScheduleAssignmentResponse updateConfirmation(
            @PathVariable Long assignmentId, @Valid @RequestBody ConfirmationStatusRequest request) {
        return scheduleService.updateConfirmation(assignmentId, request.statusConfirmacao());
    }

    @DeleteMapping("/assignments/{assignmentId}")
    public ResponseEntity<Void> delete(@PathVariable Long assignmentId) {
        scheduleService.deleteAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }
}
