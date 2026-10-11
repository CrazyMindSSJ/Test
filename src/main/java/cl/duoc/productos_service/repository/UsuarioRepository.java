package cl.duoc.productos_service.repository;

import cl.duoc.productos_service.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    long countByRol(String rol);

    List<Usuario> findAllByOrderByIdAsc();

    List<Usuario> findAllByRolOrderByUsernameAsc(String rol);
}
