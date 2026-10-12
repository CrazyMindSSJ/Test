package cl.duoc.productos_service.repository;

import cl.duoc.productos_service.model.EstadoSolicitud;
import cl.duoc.productos_service.model.Solicitud;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    List<Solicitud> findAllByClienteUsuarioUsernameIgnoreCaseOrderByIdAsc(String username);

    List<Solicitud> findAllByFlotaIdAndEstadoOrderByIdAsc(Long flotaId, EstadoSolicitud estado);

    List<Solicitud> findAllByCamionPatenteOrderByIdAsc(String patente);

    boolean existsByCliente_Usuario_Id(Long usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Solicitud s where s.id = :id")
    Optional<Solicitud> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select coalesce(sum(s.pesoKg), 0)
            from Solicitud s
            where s.camion.patente = :patente and s.estado = :estado
            """)
    long sumarPesoAsignado(@Param("patente") String patente, @Param("estado") EstadoSolicitud estado);
}
