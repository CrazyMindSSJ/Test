package cl.duoc.productos_service.controller;

import cl.duoc.productos_service.dto.RegistroRequest;
import cl.duoc.productos_service.model.Usuario;
import cl.duoc.productos_service.service.AuthService;
import cl.duoc.productos_service.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    public static class CredencialesDto {
        public String username;
        public String password;
    }

    public record LoginResponse(
            String token,
            String tokenType,
            long expiresIn,
            Long id,
            String username,
            String rol) {
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody CredencialesDto credenciales) {
        Optional<Usuario> usuarioAutenticado =
                authService.autenticar(credenciales.username, credenciales.password);

        if (usuarioAutenticado.isPresent()) {
            Usuario usuario = usuarioAutenticado.get();
            String token = jwtService.generarToken(usuario);
            return ResponseEntity.ok(new LoginResponse(
                    token,
                    "Bearer",
                    jwtService.getExpirationSeconds(),
                    usuario.getId(),
                    usuario.getUsername(),
                    usuario.getRol()));
        }
        return ResponseEntity.status(401).body("Credenciales inválidas");
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario registrar(@Valid @RequestBody RegistroRequest solicitud) {
        return authService.registrar(solicitud.username(), solicitud.password());
    }
}
