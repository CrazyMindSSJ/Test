package cl.duoc.productos_service.service;

import cl.duoc.productos_service.dto.CamionRequest;
import cl.duoc.productos_service.model.Camion;
import cl.duoc.productos_service.model.FlotaCamion;
import cl.duoc.productos_service.model.Flota;
import cl.duoc.productos_service.model.TipoBasura;
import cl.duoc.productos_service.model.UsuConductor;
import cl.duoc.productos_service.repository.CamionRepository;
import cl.duoc.productos_service.repository.FlotaRepository;
import cl.duoc.productos_service.repository.UsuConductorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CamionService {
    private final CamionRepository camionRepository;
    private final FlotaRepository flotaRepository;
    private final UsuConductorRepository conductorRepository;

    public CamionService(
            CamionRepository camionRepository,
            FlotaRepository flotaRepository,
            UsuConductorRepository conductorRepository) {
        this.camionRepository = camionRepository;
        this.flotaRepository = flotaRepository;
        this.conductorRepository = conductorRepository;
    }

    public List<Camion> listar() {
        return camionRepository.findAllByOrderByPatenteAsc();
    }

    @Transactional
    public Camion crear(CamionRequest solicitud) {
        Flota flota = resolverFlota(solicitud);
        String patente = normalizarPatente(solicitud.patente());
        if (camionRepository.existsById(patente)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un camión con esa patente");
        }
        return camionRepository.save(convertir(solicitud, patente, flota, null));
    }

    @Transactional
    public Camion editar(String patente, CamionRequest solicitud) {
        String patenteNormalizada = normalizarPatente(patente);
        Camion existente = buscar(patenteNormalizada);
        String patenteNueva = normalizarPatente(solicitud.patente());
        if (!patenteNormalizada.equals(patenteNueva) && camionRepository.existsById(patenteNueva)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un camión con esa patente");
        }
        if (!patenteNormalizada.equals(patenteNueva)
                && !existente.getSolicitudesAsignadas().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "No se puede cambiar la patente de un camión con solicitudes asignadas");
        }
        Flota flota = resolverFlota(solicitud);
        Camion actualizado = convertir(solicitud, patenteNueva, flota, patenteNormalizada);
        if (patenteNueva.equals(patenteNormalizada)) {
            existente.setCapacidadKg(actualizado.getCapacidadKg());
            existente.setTipoBasura(actualizado.getTipoBasura());
            existente.setComunaAsignada(actualizado.getComunaAsignada());
            existente.setFlota(actualizado.getFlota());
            existente.setFlotaEntidad(actualizado.getFlotaEntidad());
            existente.setConductor(actualizado.getConductor());
            existente.setFechaRevisionTecnica(actualizado.getFechaRevisionTecnica());
            existente.setRevisionTecnicaVigente(actualizado.getRevisionTecnicaVigente());
            existente.setFechaEmisionGases(actualizado.getFechaEmisionGases());
            existente.setEmisionGasesVigente(actualizado.getEmisionGasesVigente());
            existente.setFechaPermisoCirculacion(actualizado.getFechaPermisoCirculacion());
            existente.setPermisoCirculacionVigente(actualizado.getPermisoCirculacionVigente());
            return camionRepository.save(existente);
        }
        camionRepository.delete(existente);
        camionRepository.flush();
        return camionRepository.save(actualizado);
    }

    @Transactional
    public void eliminar(String patente) {
        Camion camion = buscar(normalizarPatente(patente));
        if (!camion.getSolicitudesAsignadas().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "No se puede eliminar un camión con solicitudes asignadas");
        }
        camionRepository.delete(camion);
    }

    private Camion buscar(String patente) {
        return camionRepository.findById(patente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Camión no encontrado"));
    }

    private Camion convertir(CamionRequest solicitud, String patente, Flota flota, String patenteActual) {
        Camion camion = new Camion();
        camion.setPatente(patente);
        camion.setCapacidadKg(solicitud.capacidadKg());
        camion.setTipoBasura(solicitud.tipoBasura() == null
                ? TipoBasura.BASURA : solicitud.tipoBasura());
        camion.setComunaAsignada(flota.getComuna());
        camion.setFlota(FlotaCamion.valueOf(flota.getComuna().name()));
        camion.setFlotaEntidad(flota);
        camion.setConductor(resolverConductor(solicitud.conductorId(), patenteActual));
        camion.setFechaRevisionTecnica(solicitud.fechaRevisionTecnica());
        camion.setRevisionTecnicaVigente(solicitud.revisionTecnicaVigente());
        camion.setFechaEmisionGases(solicitud.fechaEmisionGases());
        camion.setEmisionGasesVigente(solicitud.emisionGasesVigente());
        camion.setFechaPermisoCirculacion(solicitud.fechaPermisoCirculacion());
        camion.setPermisoCirculacionVigente(solicitud.permisoCirculacionVigente());
        return camion;
    }

    private String normalizarPatente(String patente) {
        return patente.trim().toUpperCase();
    }

    private Flota resolverFlota(CamionRequest solicitud) {
        Flota flota = solicitud.flotaId() != null
                ? flotaRepository.findById(solicitud.flotaId()).orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flota no encontrada"))
                : solicitud.comunaAsignada() == null ? null
                : flotaRepository.findByComuna(solicitud.comunaAsignada()).orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flota no encontrada"));
        if (flota == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Se debe indicar id_flota o comuna");
        }
        if (solicitud.comunaAsignada() != null && solicitud.comunaAsignada() != flota.getComuna()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La flota indicada no corresponde a la comuna");
        }
        if (solicitud.flota() != null
                && solicitud.flota() != FlotaCamion.valueOf(flota.getComuna().name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La flota indicada no corresponde a la comuna");
        }
        return flota;
    }

    private UsuConductor resolverConductor(Long conductorId, String patenteActual) {
        if (conductorId == null) {
            return null;
        }
        UsuConductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conductor no encontrado"));
        boolean asignadoAOtroCamion = patenteActual == null
                ? camionRepository.existsByConductorId(conductorId)
                : camionRepository.existsByConductor_IdAndPatenteNot(conductorId, patenteActual);
        if (asignadoAOtroCamion) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El conductor ya tiene un camión asignado");
        }
        return conductor;
    }
}
