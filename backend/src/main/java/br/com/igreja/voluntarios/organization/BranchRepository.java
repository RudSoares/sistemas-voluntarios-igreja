package br.com.igreja.voluntarios.organization;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BranchRepository extends JpaRepository<Branch, Long> {
    List<Branch> findAllByChurch_IdOrderByNameAsc(Long churchId);
    Optional<Branch> findByIdAndChurch_Id(Long id, Long churchId);
}
