package br.com.igreja.voluntarios.ministry;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolunteerMinistryRepository extends JpaRepository<VolunteerMinistry, Long> {

    @EntityGraph(attributePaths = {"volunteer", "role"})
    List<VolunteerMinistry> findAllByMinistry_Id(Long ministryId);

    boolean existsByVolunteer_IdAndMinistry_IdAndRole_Id(Long volunteerId, Long ministryId, Long roleId);

    boolean existsByRole_Id(Long roleId);

    Optional<VolunteerMinistry> findByIdAndMinistry_Id(Long id, Long ministryId);
}
