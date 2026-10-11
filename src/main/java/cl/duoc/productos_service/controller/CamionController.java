package cl.duoc.productos_service.controller;

import cl.duoc.productos_service.dto.CamionRequest;
import cl.duoc.productos_service.model.Camion;
import cl.duoc.productos_service.service.CamionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/camiones")
public class CamionController {
    private final CamionService camionService;

    public CamionController(CamionService camionService) {
        this.camionService = camionService;
    }

    @GetMapping
    public List<Camion> listar() {
        return camionService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Camion crear(@Valid @RequestBody CamionRequest solicitud) {
        return camionService.crear(solicitud);
    }

    @PutMapping("/{patente}")
    public Camion editar(
            @PathVariable String patente,
            @Valid @RequestBody CamionRequest solicitud) {
        return camionService.editar(patente, solicitud);
    }

    @DeleteMapping("/{patente}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String patente) {
        camionService.eliminar(patente);
    }
}
