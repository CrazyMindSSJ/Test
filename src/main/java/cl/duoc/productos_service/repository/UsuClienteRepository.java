package cl.duoc.productos_service.repository;

import cl.duoc.productos_service.model.UsuCliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuClienteRepository extends JpaRepository<UsuCliente, Long> {
    Optional<UsuCliente> findByUsuarioUsernameIgnoreCase(String username);

    boolean existsByUsuario_Id(Long usuarioId);

    void deleteByUsuario_Id(Long usuarioId);
}
