package br.com.igreja.voluntarios.ministry;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MinistryRepository extends JpaRepository<Ministry, Long> {

    List<Ministry> findAllByOrderByNomeAsc();
    List<Ministry> findAllByChurch_IdOrderByNomeAsc(Long churchId);
    Optional<Ministry> findByIdAndChurch_Id(Long id, Long churchId);
    boolean existsByNomeIgnoreCaseAndChurch_Id(String nome, Long churchId);
    boolean existsByNomeIgnoreCaseAndIdNotAndChurch_Id(String nome, Long id, Long churchId);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    Optional<Ministry> findByIdAndAtivoTrue(Long id);
}
