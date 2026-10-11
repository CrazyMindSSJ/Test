package cl.duoc.productos_service.controller;

import cl.duoc.productos_service.dto.OrdenRequest;
import cl.duoc.productos_service.model.Orden;
import cl.duoc.productos_service.service.OrdenService;
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
@RequestMapping("/api/ordenes")
public class OrdenController {
    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Orden crear(@Valid @RequestBody OrdenRequest solicitud, Authentication authentication) {
        return ordenService.crear(authentication.getName(), solicitud);
    }

    @GetMapping
    public List<Orden> listar(Authentication authentication) {
        return ordenService.listarPropias(authentication.getName());
    }

    @PutMapping("/{id}")
    public Orden modificar(
            @PathVariable Long id,
            @Valid @RequestBody OrdenRequest solicitud,
            Authentication authentication) {
        return ordenService.modificar(id, authentication.getName(), solicitud);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, Authentication authentication) {
        ordenService.eliminar(id, authentication.getName());
    }
}
