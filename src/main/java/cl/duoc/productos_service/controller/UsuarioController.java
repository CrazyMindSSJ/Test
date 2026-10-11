package cl.duoc.productos_service.controller;

import cl.duoc.productos_service.dto.UsuarioAdminRequest;
import cl.duoc.productos_service.model.Usuario;
import cl.duoc.productos_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final AuthService authService;

    public UsuarioController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public List<Usuario> listar() {
        return authService.listarTodos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario crear(@Valid @RequestBody UsuarioAdminRequest solicitud) {
        return authService.crear(solicitud.username(), solicitud.password(), solicitud.rol());
    }

    @PutMapping("/{id}")
    public Usuario actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioAdminRequest solicitud,
            Authentication authentication) {
        return authService.actualizar(
                id, solicitud.username(), solicitud.password(), solicitud.rol(), obtenerActorId(authentication));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, Authentication authentication) {
        authService.eliminar(id, obtenerActorId(authentication));
    }

    private Long obtenerActorId(Authentication authentication) {
        return authService.buscarPorUsername(authentication.getName())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Usuario autenticado no encontrado"))
                .getId();
    }
}
