package cl.duoc.productos_service.service;

import cl.duoc.productos_service.model.Camion;
import cl.duoc.productos_service.model.ComunaCamion;
import cl.duoc.productos_service.model.EstadoSolicitud;
import cl.duoc.productos_service.model.Flota;
import cl.duoc.productos_service.model.FlotaCamion;
import cl.duoc.productos_service.model.Solicitud;
import cl.duoc.productos_service.model.TipoBasura;
import cl.duoc.productos_service.model.Usuario;
import cl.duoc.productos_service.model.UsuCliente;
import cl.duoc.productos_service.model.UsuConductor;
import cl.duoc.productos_service.repository.CamionRepository;
import cl.duoc.productos_service.repository.FlotaRepository;
import cl.duoc.productos_service.repository.SolicitudRepository;
import cl.duoc.productos_service.repository.UsuarioRepository;
import cl.duoc.productos_service.repository.UsuClienteRepository;
import cl.duoc.productos_service.repository.UsuConductorRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

@Configuration
public class DemoDataInitializer {
    @Bean
    ApplicationRunner initializeDemoData(
            UsuarioRepository usuarioRepository,
            UsuClienteRepository clienteRepository,
            UsuConductorRepository conductorRepository,
            FlotaRepository flotaRepository,
            CamionRepository camionRepository,
            SolicitudRepository solicitudRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepository.count() > 0) {
                return;
            }

            Map<ComunaCamion, Flota> flotas = new EnumMap<>(ComunaCamion.class);
            for (ComunaCamion comuna : ComunaCamion.values()) {
                flotas.put(comuna, flotaRepository.save(new Flota(comuna)));
            }

            crearUsuario(usuarioRepository, passwordEncoder, "admin", "Admin1234",
                    "22.222.222-2", "Administradora", "Demo", 35, ComunaCamion.LA_FLORIDA,
                    "Av. Central", 100, "ADMIN");

            UsuCliente[] clientes = new UsuCliente[3];
            ComunaCamion[] comunas = ComunaCamion.values();
            for (int index = 0; index < 3; index++) {
                Usuario usuario = crearUsuario(
                        usuarioRepository, passwordEncoder,
                        "cliente" + (index + 1), "Cliente1234",
                        "11.111.111-" + (index + 1),
                        new String[]{"Ana", "Bruno", "Carla"}[index],
                        new String[]{"Rojas", "Pérez", "Soto"}[index],
                        25 + index, comunas[index],
                        "Calle " + (index + 1), 120 + index, "CLIENTE");
                clientes[index] = clienteRepository.save(new UsuCliente(usuario));
            }

            UsuConductor[] conductores = new UsuConductor[3];
            for (int index = 0; index < 3; index++) {
                Usuario usuario = crearUsuario(
                        usuarioRepository, passwordEncoder,
                        "conductor" + (index + 1), "Conductor1234",
                        "33.333.333-" + (index + 1),
                        new String[]{"Diego", "Elena", "Felipe"}[index],
                        "Conductor", 30 + index, comunas[index],
                        "Ruta " + (index + 1), 20 + index, "CONDUCTOR");
                conductores[index] = conductorRepository.save(new UsuConductor(
                        usuario, "A4", LocalDate.of(2030, 12, 31)));
            }

            Camion[] camiones = new Camion[3];
            for (int index = 0; index < 3; index++) {
                Camion camion = new Camion();
                camion.setPatente("DEMO-0" + (index + 1));
                camion.setCapacidadKg(1000);
                camion.setTipoBasura(TipoBasura.values()[index]);
                camion.setComunaAsignada(comunas[index]);
                camion.setFlota(FlotaCamion.values()[index]);
                camion.setFlotaEntidad(flotas.get(comunas[index]));
                camion.setConductor(conductores[index]);
                camion.setFechaRevisionTecnica(LocalDate.of(2030, 6, 30));
                camion.setRevisionTecnicaVigente(true);
                camion.setFechaEmisionGases(LocalDate.of(2030, 6, 30));
                camion.setEmisionGasesVigente(true);
                camion.setFechaPermisoCirculacion(LocalDate.of(2030, 6, 30));
                camion.setPermisoCirculacionVigente(true);
                camiones[index] = camionRepository.save(camion);
            }

            for (int index = 0; index < 3; index++) {
                Usuario cliente = clientes[index].getUsuario();
                Solicitud solicitud = new Solicitud();
                solicitud.setNombreCliente(cliente.getNombre() + " " + cliente.getApellido());
                solicitud.setDireccionCliente(
                        cliente.getCalle() + " " + cliente.getNumCalle() + ", " + cliente.getComuna().name());
                solicitud.setTipoBasura(TipoBasura.values()[index]);
                solicitud.setPesoKg(100 + (index * 50));
                solicitud.setEstado(EstadoSolicitud.PENDIENTE);
                solicitud.setCliente(clientes[index]);
                solicitud.setFlota(flotas.get(comunas[index]));
                solicitud.setTiempoEsperaMinutos(0);
                solicitudRepository.save(solicitud);
            }
        };
    }

    private Usuario crearUsuario(
            UsuarioRepository repository,
            PasswordEncoder passwordEncoder,
            String username,
            String password,
            String rut,
            String nombre,
            String apellido,
            int edad,
            ComunaCamion comuna,
            String calle,
            int numCalle,
            String rol) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRut(rut);
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEdad(edad);
        usuario.setComuna(comuna);
        usuario.setCalle(calle);
        usuario.setNumCalle(numCalle);
        usuario.setRol(rol);
        return repository.save(usuario);
    }
}
