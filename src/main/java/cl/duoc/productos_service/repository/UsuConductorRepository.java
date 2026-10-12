package cl.duoc.productos_service.repository;

import cl.duoc.productos_service.model.UsuConductor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuConductorRepository extends JpaRepository<UsuConductor, Long> {
    Optional<UsuConductor> findByUsuarioUsernameIgnoreCase(String username);

    void deleteByUsuario_Id(Long usuarioId);
}
