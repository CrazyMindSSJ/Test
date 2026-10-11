package cl.duoc.productos_service.repository;

import cl.duoc.productos_service.model.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrdenRepository extends JpaRepository<Orden, Long> {
    List<Orden> findAllByNombreUsuarioOrderByIdAsc(String nombreUsuario);

    Optional<Orden> findByIdAndNombreUsuario(Long id, String nombreUsuario);

    boolean existsByNombreUsuario(String nombreUsuario);

    @Modifying
    @Query("update Orden o set o.nombreUsuario = :nuevo where o.nombreUsuario = :anterior")
    int actualizarPropietario(
            @Param("anterior") String anterior,
            @Param("nuevo") String nuevo);
}
