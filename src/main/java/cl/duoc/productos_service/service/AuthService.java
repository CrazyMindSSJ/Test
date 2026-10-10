package cl.duoc.productos_service.service;

import cl.duoc.productos_service.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {
    private final List<Usuario> listaUsuarios = Arrays.asList(
            new Usuario(1L, "admin", "1234", "ADMIN"),
            new Usuario(2L, "sebastian", "password567", "USER"),
            new Usuario(3L, "maria", "abcd987", "USER")
    );

    public Optional<Usuario> autenticar(String username, String password) {
        return listaUsuarios.stream()
                .filter(usuario -> usuario.getUsername().equals(username)
                        && usuario.getPassword().equals(password))
                .findFirst();
    }
}
