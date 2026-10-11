package cl.duoc.productos_service.service;

import cl.duoc.productos_service.dto.OrdenRequest;
import cl.duoc.productos_service.model.EstadoOrden;
import cl.duoc.productos_service.model.Orden;
import cl.duoc.productos_service.repository.OrdenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OrdenService {
    private final OrdenRepository ordenRepository;

    public OrdenService(OrdenRepository ordenRepository) {
        this.ordenRepository = ordenRepository;
    }

    public Orden crear(String nombreUsuario, OrdenRequest solicitud) {
        Orden orden = new Orden();
        orden.setNombreUsuario(nombreUsuario);
        orden.setComuna(solicitud.comuna());
        orden.setCalle(solicitud.calle());
        orden.setNumCalle(solicitud.numCalle());
        orden.setTipoBasura(solicitud.tipoBasura());
        orden.setEstado(EstadoOrden.EN_PROCESO);
        return ordenRepository.save(orden);
    }

    public List<Orden> listarPropias(String nombreUsuario) {
        return ordenRepository.findAllByNombreUsuarioOrderByIdAsc(nombreUsuario);
    }

    public Orden modificar(Long id, String nombreUsuario, OrdenRequest solicitud) {
        Orden orden = buscarPropia(id, nombreUsuario);
        if (orden.getEstado() != EstadoOrden.EN_PROCESO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Solo se pueden modificar órdenes en proceso");
        }
        orden.setComuna(solicitud.comuna());
        orden.setCalle(solicitud.calle());
        orden.setNumCalle(solicitud.numCalle());
        orden.setTipoBasura(solicitud.tipoBasura());
        return ordenRepository.save(orden);
    }

    public void eliminar(Long id, String nombreUsuario) {
        Orden orden = buscarPropia(id, nombreUsuario);
        if (orden.getEstado() != EstadoOrden.EN_PROCESO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Solo se pueden eliminar órdenes en proceso");
        }
        ordenRepository.delete(orden);
    }

    private Orden buscarPropia(Long id, String nombreUsuario) {
        return ordenRepository.findByIdAndNombreUsuario(id, nombreUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));
    }
}
