package cl.duoc.productos_service.service;

import cl.duoc.productos_service.dto.CamionRequest;
import cl.duoc.productos_service.model.Camion;
import cl.duoc.productos_service.model.ComunaCamion;
import cl.duoc.productos_service.model.FlotaCamion;
import cl.duoc.productos_service.repository.CamionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CamionService {
    private final CamionRepository camionRepository;

    public CamionService(CamionRepository camionRepository) {
        this.camionRepository = camionRepository;
    }

    public List<Camion> listar() {
        return camionRepository.findAllByOrderByPatenteAsc();
    }

    @Transactional
    public Camion crear(CamionRequest solicitud) {
        validarFlotaComuna(solicitud.comunaAsignada(), solicitud.flota());
        String patente = normalizarPatente(solicitud.patente());
        if (camionRepository.existsById(patente)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un camión con esa patente");
        }
        return camionRepository.save(convertir(solicitud, patente));
    }

    @Transactional
    public Camion editar(String patente, CamionRequest solicitud) {
        String patenteNormalizada = normalizarPatente(patente);
        buscar(patenteNormalizada);
        String patenteNueva = normalizarPatente(solicitud.patente());
        if (!patenteNormalizada.equals(patenteNueva) && camionRepository.existsById(patenteNueva)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un camión con esa patente");
        }
        validarFlotaComuna(solicitud.comunaAsignada(), solicitud.flota());
        camionRepository.deleteById(patenteNormalizada);
        camionRepository.flush();
        return camionRepository.save(convertir(solicitud, patenteNueva));
    }

    public void eliminar(String patente) {
        camionRepository.delete(buscar(normalizarPatente(patente)));
    }

    private Camion buscar(String patente) {
        return camionRepository.findById(patente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Camión no encontrado"));
    }

    private Camion convertir(CamionRequest solicitud, String patente) {
        Camion camion = new Camion();
        camion.setPatente(patente);
        camion.setCapacidadKg(solicitud.capacidadKg());
        camion.setTipoBasura(solicitud.tipoBasura());
        camion.setComunaAsignada(solicitud.comunaAsignada());
        camion.setFlota(solicitud.flota());
        return camion;
    }

    private String normalizarPatente(String patente) {
        return patente.trim().toUpperCase();
    }

    private void validarFlotaComuna(ComunaCamion comuna, FlotaCamion flota) {
        boolean coincide = switch (comuna) {
            case LA_FLORIDA -> flota == FlotaCamion.LA_FLORIDA;
            case PUENTE_ALTO -> flota == FlotaCamion.PUENTE_ALTO;
            case LA_PINTANA -> flota == FlotaCamion.LA_PINTANA;
        };
        if (!coincide) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La flota debe corresponder a la comuna asignada");
        }
    }
}
