package br.com.igreja.voluntarios.volunteer;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {

    @EntityGraph(attributePaths = "disponibilidadeDias")
    List<Volunteer> findAllByOrderByNomeCompletoAsc();

    @Override
    @EntityGraph(attributePaths = "disponibilidadeDias")
    Optional<Volunteer> findById(Long id);
}
