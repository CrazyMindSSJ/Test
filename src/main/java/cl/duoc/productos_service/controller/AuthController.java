package cl.duoc.productos_service.controller;

import cl.duoc.productos_service.model.Usuario;
import cl.duoc.productos_service.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public static class CredencialesDto {
        public String username;
        public String password;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody CredencialesDto credenciales) {
        Optional<Usuario> usuarioAutenticado =
                authService.autenticar(credenciales.username, credenciales.password);

        if (usuarioAutenticado.isPresent()) {
            Usuario usuario = usuarioAutenticado.get();
            usuario.setPassword(null);
            return ResponseEntity.ok(usuario);
        }
        return ResponseEntity.status(401).body("Credenciales inválidas");
    }
}
