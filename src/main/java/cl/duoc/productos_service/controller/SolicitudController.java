package cl.duoc.productos_service.controller;

import cl.duoc.productos_service.dto.NuevaSolicitudRequest;
import cl.duoc.productos_service.model.Solicitud;
import cl.duoc.productos_service.service.SolicitudService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {
    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Solicitud crear(
            @Valid @RequestBody NuevaSolicitudRequest request,
            Authentication authentication) {
        return solicitudService.crear(authentication.getName(), request);
    }

    @GetMapping("/mias")
    public List<Solicitud> listarPropias(Authentication authentication) {
        return solicitudService.listarPropias(authentication.getName());
    }

    @GetMapping("/disponibles")
    public List<Solicitud> listarDisponibles(Authentication authentication) {
        return solicitudService.listarDisponibles(authentication.getName());
    }

    @GetMapping("/asignadas")
    public List<Solicitud> listarAsignadas(Authentication authentication) {
        return solicitudService.listarAsignadas(authentication.getName());
    }

    @PostMapping("/{id}/aceptar")
    public Solicitud aceptar(@PathVariable Long id, Authentication authentication) {
        return solicitudService.aceptar(id, authentication.getName());
    }

    @PostMapping("/{id}/rechazar")
    public Solicitud rechazar(@PathVariable Long id, Authentication authentication) {
        return solicitudService.rechazar(id, authentication.getName());
    }

    @PostMapping("/{id}/completar")
    public Solicitud completar(@PathVariable Long id, Authentication authentication) {
        return solicitudService.completar(id, authentication.getName());
    }
}
