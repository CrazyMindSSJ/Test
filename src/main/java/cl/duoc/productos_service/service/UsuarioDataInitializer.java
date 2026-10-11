package cl.duoc.productos_service.service;

import cl.duoc.productos_service.model.Usuario;
import cl.duoc.productos_service.repository.UsuarioRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class UsuarioDataInitializer {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    ApplicationRunner inicializarUsuarios(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepository.count() > 0) {
                return;
            }
            List<UsuarioInicial> usuariosIniciales = List.of(
                    new UsuarioInicial("admin", "1234", "ADMIN"),
                    new UsuarioInicial("sebastian", "password567", "USER"),
                    new UsuarioInicial("maria", "abcd987", "USER"));

            List<Usuario> usuarios = new java.util.ArrayList<>();
            for (UsuarioInicial inicial : usuariosIniciales) {
                Usuario usuario = new Usuario();
                usuario.setUsername(inicial.username());
                usuario.setPassword(passwordEncoder.encode(inicial.password()));
                usuario.setRol(inicial.rol());
                usuarios.add(usuario);
            }
            usuarioRepository.saveAll(usuarios);
        };
    }

    private record UsuarioInicial(String username, String password, String rol) {
    }
}
