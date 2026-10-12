package cl.duoc.productos_service.service;

import cl.duoc.productos_service.dto.NuevaSolicitudRequest;
import cl.duoc.productos_service.model.Camion;
import cl.duoc.productos_service.model.ComunaCamion;
import cl.duoc.productos_service.model.EstadoSolicitud;
import cl.duoc.productos_service.model.Flota;
import cl.duoc.productos_service.model.RechazoSolicitud;
import cl.duoc.productos_service.model.Solicitud;
import cl.duoc.productos_service.model.UsuCliente;
import cl.duoc.productos_service.model.UsuConductor;
import cl.duoc.productos_service.repository.CamionRepository;
import cl.duoc.productos_service.repository.FlotaRepository;
import cl.duoc.productos_service.repository.RechazoSolicitudRepository;
import cl.duoc.productos_service.repository.SolicitudRepository;
import cl.duoc.productos_service.repository.UsuClienteRepository;
import cl.duoc.productos_service.repository.UsuConductorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.Duration;
import java.time.Instant;

@Service
public class SolicitudService {
    private final SolicitudRepository solicitudRepository;
    private final UsuClienteRepository clienteRepository;
    private final UsuConductorRepository conductorRepository;
    private final CamionRepository camionRepository;
    private final FlotaRepository flotaRepository;
    private final RechazoSolicitudRepository rechazoRepository;

    public SolicitudService(
            SolicitudRepository solicitudRepository,
            UsuClienteRepository clienteRepository,
            UsuConductorRepository conductorRepository,
            CamionRepository camionRepository,
            FlotaRepository flotaRepository,
            RechazoSolicitudRepository rechazoRepository) {
        this.solicitudRepository = solicitudRepository;
        this.clienteRepository = clienteRepository;
        this.conductorRepository = conductorRepository;
        this.camionRepository = camionRepository;
        this.flotaRepository = flotaRepository;
        this.rechazoRepository = rechazoRepository;
    }

    @Transactional
    public Solicitud crear(String username, NuevaSolicitudRequest request) {
        UsuCliente cliente = clienteRepository.findByUsuarioUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "La cuenta no tiene perfil de cliente"));
        var usuario = cliente.getUsuario();
        Flota flota = flotaRepository.findByComuna(usuario.getComuna())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT, "No hay una flota asignada a la comuna del cliente"));

        Solicitud solicitud = new Solicitud();
        solicitud.setNombreCliente(usuario.getNombre() + " " + usuario.getApellido());
        solicitud.setDireccionCliente(usuario.getCalle() + " " + usuario.getNumCalle()
                + ", " + usuario.getComuna().name());
        solicitud.setTipoBasura(request.tipoBasura());
        solicitud.setPesoKg(request.pesoKg());
        solicitud.setCliente(cliente);
        solicitud.setFlota(flota);
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setTiempoEsperaMinutos(0);
        return solicitudRepository.save(solicitud);
    }

    @Transactional(readOnly = true)
    public List<Solicitud> listarPropias(String username) {
        return solicitudRepository.findAllByClienteUsuarioUsernameIgnoreCaseOrderByIdAsc(username);
    }

    @Transactional(readOnly = true)
    public List<Solicitud> listarDisponibles(String username) {
        UsuConductor conductor = buscarConductor(username);
        Camion camion = camionRepository.findByConductorUsuarioUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT, "El conductor no tiene un camión asignado"));
        ComunaCamion comuna = camion.getFlotaEntidad().getComuna();
        List<Long> rechazadas = rechazoRepository
                .findAllByConductorUsuarioUsernameIgnoreCase(username)
                .stream()
                .map(rechazo -> rechazo.getSolicitud().getId())
                .toList();
        return solicitudRepository.findAllByFlotaIdAndEstadoOrderByIdAsc(
                        camion.getFlotaEntidad().getId(), EstadoSolicitud.PENDIENTE)
                .stream()
                .filter(solicitud -> solicitud.getFlota().getComuna() == comuna)
                .filter(solicitud -> !rechazadas.contains(solicitud.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Solicitud> listarAsignadas(String username) {
        buscarConductor(username);
        Camion camion = camionRepository.findByConductorUsuarioUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT, "El conductor no tiene un camión asignado"));
        return solicitudRepository.findAllByCamionPatenteOrderByIdAsc(camion.getPatente());
    }

    @Transactional
    public Solicitud aceptar(Long id, String username) {
        buscarConductor(username);
        Camion camion = camionRepository.findByConductorUsernameForUpdate(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT, "El conductor no tiene un camión asignado"));
        Solicitud solicitud = solicitudRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE || solicitud.getCamion() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La solicitud ya no está disponible");
        }
        if (!solicitud.getFlota().getId().equals(camion.getFlotaEntidad().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La solicitud no pertenece a la flota del camión");
        }
        long pesoAsignado = solicitudRepository.sumarPesoAsignado(camion.getPatente(), EstadoSolicitud.ASIGNADA);
        if (pesoAsignado + solicitud.getPesoKg() > camion.getCapacidadKg()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "La solicitud supera la capacidad disponible del camión");
        }

        solicitud.setCamion(camion);
        solicitud.setEstado(EstadoSolicitud.ASIGNADA);
        if (solicitud.getFechaCreacion() != null) {
            solicitud.setTiempoEsperaMinutos((int) Duration.between(
                    solicitud.getFechaCreacion(), Instant.now()).toMinutes());
        }
        return solicitudRepository.save(solicitud);
    }

    @Transactional
    public Solicitud rechazar(Long id, String username) {
        UsuConductor conductor = buscarConductor(username);
        Camion camion = camionRepository.findByConductorUsernameForUpdate(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT, "El conductor no tiene un camión asignado"));
        Solicitud solicitud = solicitudRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE || solicitud.getCamion() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La solicitud ya no está disponible");
        }
        if (!solicitud.getFlota().getId().equals(camion.getFlotaEntidad().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La solicitud no pertenece a la flota del camión");
        }
        if (rechazoRepository.existsBySolicitudIdAndConductorId(id, conductor.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya rechazaste esta solicitud");
        }

        rechazoRepository.save(new RechazoSolicitud(solicitud, conductor));
        return solicitud;
    }

    @Transactional
    public Solicitud completar(Long id, String username) {
        Camion camion = camionRepository.findByConductorUsernameForUpdate(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT, "El conductor no tiene un camión asignado"));
        Solicitud solicitud = solicitudRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
        if (solicitud.getEstado() != EstadoSolicitud.ASIGNADA
                || solicitud.getCamion() == null
                || !solicitud.getCamion().getPatente().equals(camion.getPatente())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La solicitud no está asignada a tu camión");
        }
        solicitud.setEstado(EstadoSolicitud.COMPLETADA);
        return solicitudRepository.save(solicitud);
    }

    private UsuConductor buscarConductor(String username) {
        UsuConductor conductor = conductorRepository.findByUsuarioUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "La cuenta no tiene perfil de conductor"));
        if (conductor.getFechaVencimientoLicencia().isBefore(java.time.LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La licencia del conductor está vencida");
        }
        return conductor;
    }
}
