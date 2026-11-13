package br.com.igreja.voluntarios.ministry;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MinistryRoleRepository extends JpaRepository<MinistryRole, Long> {

    @EntityGraph(attributePaths = "ministry")
    List<MinistryRole> findAllByMinistry_IdOrderByNomeAsc(Long ministryId);

    Optional<MinistryRole> findByIdAndMinistry_Id(Long id, Long ministryId);
}
