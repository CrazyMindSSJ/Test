package cl.duoc.productos_service.repository;

import cl.duoc.productos_service.model.RechazoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RechazoSolicitudRepository extends JpaRepository<RechazoSolicitud, Long> {
    List<RechazoSolicitud> findAllByConductorUsuarioUsernameIgnoreCase(String username);

    boolean existsBySolicitudIdAndConductorId(Long solicitudId, Long conductorId);

    void deleteAllByConductorUsuarioId(Long usuarioId);
}
