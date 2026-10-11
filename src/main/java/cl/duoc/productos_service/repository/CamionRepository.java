package cl.duoc.productos_service.repository;

import cl.duoc.productos_service.model.Camion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CamionRepository extends JpaRepository<Camion, String> {
    java.util.List<Camion> findAllByOrderByPatenteAsc();
}
