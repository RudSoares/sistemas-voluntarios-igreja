package br.com.igreja.voluntarios.organization;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ChurchRepository extends JpaRepository<Church, Long> {
    List<Church> findAllByOrderByNameAsc();
    java.util.Optional<Church> findFirstByName(String name);
}
