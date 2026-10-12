package cl.duoc.productos_service;

import cl.duoc.productos_service.console.ConsolaInicioSesion;
import cl.duoc.productos_service.dto.NuevaSolicitudRequest;
import cl.duoc.productos_service.model.Camion;
import cl.duoc.productos_service.model.ComunaCamion;
import cl.duoc.productos_service.model.EstadoSolicitud;
import cl.duoc.productos_service.model.Flota;
import cl.duoc.productos_service.model.Solicitud;
import cl.duoc.productos_service.model.TipoBasura;
import cl.duoc.productos_service.model.Usuario;
import cl.duoc.productos_service.repository.CamionRepository;
import cl.duoc.productos_service.repository.FlotaRepository;
import cl.duoc.productos_service.repository.SolicitudRepository;
import cl.duoc.productos_service.repository.UsuClienteRepository;
import cl.duoc.productos_service.service.AuthService;
import cl.duoc.productos_service.service.SolicitudService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "spring.datasource.url=jdbc:h2:mem:flujo-cliente;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class FlujoClienteTests {
    @Autowired
    private UsuClienteRepository clienteRepository;

    @Autowired
    private FlotaRepository flotaRepository;

    @Autowired
    private CamionRepository camionRepository;

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private SolicitudService solicitudService;

    @Test
    void creaTresClientesTresFlotasYUnCamionPorFlota() {
        List<Flota> flotas = flotaRepository.findAll();
        List<Camion> camiones = camionRepository.findAllByOrderByPatenteAsc();

        assertThat(clienteRepository.count()).isEqualTo(3);
        assertThat(flotas)
                .extracting(Flota::getComuna)
                .containsExactlyInAnyOrder(ComunaCamion.values());
        assertThat(camiones).hasSize(3);

        for (Flota flota : flotas) {
            assertThat(camiones)
                    .filteredOn(camion -> camion.getFlotaEntidad().getId().equals(flota.getId()))
                    .singleElement()
                    .satisfies(camion ->
                            assertThat(camion.getComunaAsignada()).isEqualTo(flota.getComuna()));
        }
    }

    @Test
    void nuevaSolicitudSeGuardaEnLaFlotaDeLaComunaDelCliente() {
        Solicitud creada = solicitudService.crear(
                "cliente1", new NuevaSolicitudRequest(TipoBasura.RECICLABLE, 300));

        Solicitud guardada = solicitudRepository.findById(creada.getId()).orElseThrow();
        assertThat(guardada.getEstado()).isEqualTo(EstadoSolicitud.PENDIENTE);
        assertThat(guardada.getPesoKg()).isEqualTo(300);
        assertThat(guardada.getFlota().getComuna()).isEqualTo(ComunaCamion.LA_FLORIDA);
    }

    @Test
    void conductorPuedeRechazarDejarPendienteAceptarYFinalizarSolicitud() {
        Solicitud disponible = solicitudService.listarDisponibles("conductor1").get(0);
        solicitudService.rechazar(disponible.getId(), "conductor1");

        Solicitud rechazada = solicitudRepository.findById(disponible.getId()).orElseThrow();
        assertThat(rechazada.getEstado()).isEqualTo(EstadoSolicitud.PENDIENTE);
        assertThat(solicitudService.listarDisponibles("conductor1"))
                .extracting(Solicitud::getId)
                .doesNotContain(disponible.getId());

        Solicitud nueva = solicitudService.crear(
                "cliente1", new NuevaSolicitudRequest(TipoBasura.RECICLABLE, 200));
        Solicitud asignada = solicitudService.aceptar(nueva.getId(), "conductor1");
        assertThat(asignada.getEstado()).isEqualTo(EstadoSolicitud.ASIGNADA);

        Solicitud completada = solicitudService.completar(nueva.getId(), "conductor1");
        assertThat(completada.getEstado()).isEqualTo(EstadoSolicitud.COMPLETADA);
    }

    @Test
    void clientePuedeVerSusSolicitudesYCrearUnaDesdeElFormulario() {
        AuthService authService = mock(AuthService.class);
        SolicitudService solicitudService = mock(SolicitudService.class);
        Usuario usuario = mock(Usuario.class);
        when(usuario.getUsername()).thenReturn("cliente1");
        when(usuario.getRol()).thenReturn("CLIENTE");
        when(authService.autenticar("cliente1", "Cliente1234")).thenReturn(Optional.of(usuario));

        Flota flota = mock(Flota.class);
        when(flota.getComuna()).thenReturn(ComunaCamion.LA_FLORIDA);
        Solicitud solicitud = mock(Solicitud.class);
        when(solicitud.getId()).thenReturn(501L);
        when(solicitud.getFlota()).thenReturn(flota);
        when(solicitudService.listarPropias("cliente1")).thenReturn(List.of());
        when(solicitudService.crear(eq("cliente1"), any(NuevaSolicitudRequest.class)))
                .thenReturn(solicitud);

        String respuestas = String.join("\n",
                "1",
                "cliente1",
                "Cliente1234",
                "1",
                "",
                "2",
                "2",
                "250",
                "",
                "1",
                "",
                "3",
                "0",
                "");
        ByteArrayInputStream entradaAnterior = new ByteArrayInputStream(respuestas.getBytes(StandardCharsets.UTF_8));
        var entradaOriginal = System.in;
        var salidaOriginal = System.out;
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try {
            System.setIn(entradaAnterior);
            System.setOut(new PrintStream(salida, true, StandardCharsets.UTF_8));
            new ConsolaInicioSesion(authService, solicitudService).run();
        } finally {
            System.setIn(entradaOriginal);
            System.setOut(salidaOriginal);
        }

        assertThat(salida.toString(StandardCharsets.UTF_8))
                .contains("Ver mis solicitudes")
                .contains("FORMULARIO DE NUEVA SOLICITUD")
                .contains("Solicitud #501 creada correctamente");
        verify(solicitudService, times(2)).listarPropias("cliente1");
        ArgumentCaptor<NuevaSolicitudRequest> requestCaptor =
                ArgumentCaptor.forClass(NuevaSolicitudRequest.class);
        verify(solicitudService).crear(eq("cliente1"), requestCaptor.capture());
        assertThat(requestCaptor.getValue().tipoBasura()).isEqualTo(TipoBasura.BASURA);
        assertThat(requestCaptor.getValue().pesoKg()).isEqualTo(250);
    }

    @Test
    void menuConductorPermiteListarAceptarRechazarYFinalizar() {
        AuthService authService = mock(AuthService.class);
        SolicitudService solicitudService = mock(SolicitudService.class);
        Usuario usuario = mock(Usuario.class);
        when(usuario.getUsername()).thenReturn("conductor1");
        when(usuario.getRol()).thenReturn("CONDUCTOR");
        when(authService.autenticar("conductor1", "Conductor1234"))
                .thenReturn(Optional.of(usuario));
        when(solicitudService.listarDisponibles("conductor1")).thenReturn(List.of());
        when(solicitudService.listarAsignadas("conductor1")).thenReturn(List.of());
        Solicitud solicitud = mock(Solicitud.class);
        when(solicitud.getId()).thenReturn(10L, 11L, 12L);
        when(solicitudService.aceptar(10L, "conductor1")).thenReturn(solicitud);
        when(solicitudService.rechazar(11L, "conductor1")).thenReturn(solicitud);
        when(solicitudService.completar(12L, "conductor1")).thenReturn(solicitud);

        String respuestas = String.join("\n",
                "1",
                "conductor1",
                "Conductor1234",
                "1",
                "",
                "2",
                "10",
                "",
                "3",
                "11",
                "",
                "4",
                "",
                "5",
                "12",
                "",
                "6",
                "0",
                "");
        ByteArrayInputStream entrada = new ByteArrayInputStream(respuestas.getBytes(StandardCharsets.UTF_8));
        var entradaOriginal = System.in;
        var salidaOriginal = System.out;
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try {
            System.setIn(entrada);
            System.setOut(new PrintStream(salida, true, StandardCharsets.UTF_8));
            new ConsolaInicioSesion(authService, solicitudService).run();
        } finally {
            System.setIn(entradaOriginal);
            System.setOut(salidaOriginal);
        }

        assertThat(salida.toString(StandardCharsets.UTF_8))
                .contains("MENÚ CONDUCTOR")
                .contains("Seguirá pendiente para otros conductores");
        verify(solicitudService).listarDisponibles("conductor1");
        verify(solicitudService).listarAsignadas("conductor1");
        verify(solicitudService).aceptar(10L, "conductor1");
        verify(solicitudService).rechazar(11L, "conductor1");
        verify(solicitudService).completar(12L, "conductor1");
    }
}
