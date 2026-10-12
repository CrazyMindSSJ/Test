package cl.duoc.productos_service.repository;

import cl.duoc.productos_service.model.Camion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CamionRepository extends JpaRepository<Camion, String> {
    @EntityGraph(attributePaths = {"flotaEntidad", "conductor"})
    java.util.List<Camion> findAllByOrderByPatenteAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Camion c where c.conductor.usuario.username = :username")
    Optional<Camion> findByConductorUsernameForUpdate(@Param("username") String username);

    Optional<Camion> findByConductorUsuarioUsernameIgnoreCase(String username);

    boolean existsByConductorId(Long conductorId);

    boolean existsByConductor_IdAndPatenteNot(Long conductorId, String patente);

    boolean existsByConductor_Usuario_Id(Long usuarioId);
}
