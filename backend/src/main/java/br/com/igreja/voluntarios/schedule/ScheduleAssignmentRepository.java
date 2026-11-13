package br.com.igreja.voluntarios.schedule;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleAssignmentRepository extends JpaRepository<ScheduleAssignment, Long> {
    @EntityGraph(attributePaths = {"serviceEvent", "ministry", "ministryRole", "volunteer"})
    List<ScheduleAssignment> findAll();

    @EntityGraph(attributePaths = {"serviceEvent", "ministry", "ministryRole", "volunteer"})
    List<ScheduleAssignment> findAllByServiceEvent_Id(Long eventId);

    boolean existsByServiceEvent_IdAndVolunteer_Id(Long eventId, Long volunteerId);
    boolean existsByServiceEvent_Id(Long eventId);
    long countByServiceEvent_Status(EventStatus status);
}
