package br.com.igreja.voluntarios.volunteer;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {

    @EntityGraph(attributePaths = "disponibilidadeDias")
    List<Volunteer> findAllByOrderByNomeCompletoAsc();

    @EntityGraph(attributePaths = "disponibilidadeDias")
    List<Volunteer> findAllByChurch_IdOrderByNomeCompletoAsc(Long churchId);

    @Override
    @EntityGraph(attributePaths = "disponibilidadeDias")
    Optional<Volunteer> findById(Long id);

    @EntityGraph(attributePaths = "disponibilidadeDias")
    Optional<Volunteer> findByIdAndChurch_Id(Long id, Long churchId);
}
