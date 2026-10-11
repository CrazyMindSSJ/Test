package cl.duoc.productos_service.repository;

import cl.duoc.productos_service.model.MarcaInicializacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarcaInicializacionRepository extends JpaRepository<MarcaInicializacion, String> {
}
