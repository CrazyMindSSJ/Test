package cl.duoc.productos_service.service;

import cl.duoc.productos_service.model.Flota;
import cl.duoc.productos_service.repository.FlotaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FlotaService {
    private final FlotaRepository flotaRepository;

    public FlotaService(FlotaRepository flotaRepository) {
        this.flotaRepository = flotaRepository;
    }

    @Transactional(readOnly = true)
    public List<Flota> listar() {
        return flotaRepository.findAll();
    }
}
