package br.com.igreja.voluntarios.schedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceEventRepository extends JpaRepository<ServiceEvent, Long> {
    List<ServiceEvent> findAllByOrderByDataEventoAscHorarioAsc();
    List<ServiceEvent> findAllByStatusAndDataEventoGreaterThanEqualOrderByDataEventoAscHorarioAsc(EventStatus status, LocalDate from);
    Optional<ServiceEvent> findFirstByStatusAndDataEventoGreaterThanEqualOrderByDataEventoAscHorarioAsc(EventStatus status, LocalDate from);
}
