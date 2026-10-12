package cl.duoc.productos_service.repository;

import cl.duoc.productos_service.model.ComunaCamion;
import cl.duoc.productos_service.model.Flota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FlotaRepository extends JpaRepository<Flota, Long> {
    Optional<Flota> findByComuna(ComunaCamion comuna);
}
