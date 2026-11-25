package br.com.igreja.voluntarios.identity;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    @EntityGraph(attributePaths = {"church", "branch", "volunteer"})
    Optional<AppUser> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    Optional<AppUser> findByVolunteer_Id(Long volunteerId);
    List<AppUser> findAllByRoleOrderByNameAsc(UserRole role);
}
