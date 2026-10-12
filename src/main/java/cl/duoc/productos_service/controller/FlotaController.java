package cl.duoc.productos_service.controller;

import cl.duoc.productos_service.model.Flota;
import cl.duoc.productos_service.service.FlotaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/flotas")
public class FlotaController {
    private final FlotaService flotaService;

    public FlotaController(FlotaService flotaService) {
        this.flotaService = flotaService;
    }

    @GetMapping
    public List<Flota> listar() {
        return flotaService.listar();
    }
}
