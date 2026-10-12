package cl.duoc.productos_service.console;

import cl.duoc.productos_service.dto.NuevaSolicitudRequest;
import cl.duoc.productos_service.model.Solicitud;
import cl.duoc.productos_service.model.TipoBasura;
import cl.duoc.productos_service.model.Usuario;
import cl.duoc.productos_service.service.AuthService;
import cl.duoc.productos_service.service.SolicitudService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.Console;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
@Profile("login-console")
@Order(Ordered.LOWEST_PRECEDENCE)
public class ConsolaInicioSesion implements CommandLineRunner {
    private final AuthService authService;
    private final SolicitudService solicitudService;

    public ConsolaInicioSesion(AuthService authService, SolicitudService solicitudService) {
        this.authService = authService;
        this.solicitudService = solicitudService;
    }

    @Override
    public void run(String... args) {
        Console console = System.console();
        Scanner scanner = console == null ? new Scanner(System.in) : null;

        while (true) {
            System.out.println("\n======================================");
            System.out.println("       SISTEMA DE GESTIÓN");
            System.out.println("======================================");
            System.out.println("1. Iniciar sesión");
            System.out.println("0. Salir");
            String opcion = leerLinea("Selecciona una opción: ", console, scanner);
            if (opcion == null || opcion.equals("0")) {
                return;
            }
            if (!opcion.equals("1")) {
                System.out.println("Opción no válida.");
                continue;
            }

            Optional<Usuario> usuario = iniciarSesion(console, scanner);
            if (usuario.isEmpty()) {
                if (console == null && !scanner.hasNextLine()) {
                    return;
                }
                continue;
            }

            if (!menuUsuario(usuario.get(), console, scanner)) {
                return;
            }
        }
    }

    private boolean menuUsuario(Usuario usuario, Console console, Scanner scanner) {
        boolean esCliente = "CLIENTE".equalsIgnoreCase(usuario.getRol())
                || "USER".equalsIgnoreCase(usuario.getRol());
        System.out.printf(
                "%nSesión iniciada: %s (rol: %s).%n",
                usuario.getUsername(),
                usuario.getRol());

        if ("CONDUCTOR".equalsIgnoreCase(usuario.getRol())) {
            return menuConductor(usuario.getUsername(), console, scanner);
        }

        while (true) {
            System.out.println("\n======================================");
            System.out.println("             MENÚ DE USUARIO");
            System.out.println("======================================");
            if (esCliente) {
                System.out.println("1. Ver mis solicitudes");
                System.out.println("2. Crear una solicitud");
                System.out.println("3. Cerrar sesión");
            } else {
                System.out.println("1. Cerrar sesión");
            }
            System.out.println("0. Salir");
            String opcion = leerLinea("Selecciona una opción: ", console, scanner);
            if (opcion == null || opcion.equals("0")) {
                return false;
            }

            if (esCliente && opcion.equals("1")) {
                mostrarSolicitudes(usuario.getUsername());
                esperarEnter(console, scanner);
            } else if (esCliente && opcion.equals("2")) {
                crearSolicitud(usuario.getUsername(), console, scanner);
                esperarEnter(console, scanner);
            } else if ((esCliente && opcion.equals("3")) || (!esCliente && opcion.equals("1"))) {
                System.out.println("Sesión cerrada.");
                return true;
            } else {
                System.out.println("Opción no válida.");
            }
        }
    }

    private boolean menuConductor(String username, Console console, Scanner scanner) {
        while (true) {
            System.out.println("\n======================================");
            System.out.println("              MENÚ CONDUCTOR");
            System.out.println("======================================");
            System.out.println("1. Ver solicitudes disponibles de mi flota");
            System.out.println("2. Aceptar una solicitud");
            System.out.println("3. Rechazar una solicitud");
            System.out.println("4. Ver solicitudes asignadas a mi camión");
            System.out.println("5. Finalizar una solicitud");
            System.out.println("6. Cerrar sesión");
            System.out.println("0. Salir");
            String opcion = leerLinea("Selecciona una opción: ", console, scanner);
            if (opcion == null || opcion.equals("0")) {
                return false;
            }

            switch (opcion) {
                case "1" -> mostrarSolicitudesConductor(
                        solicitudService.listarDisponibles(username),
                        "No hay solicitudes disponibles para tu flota.", console, scanner);
                case "2" -> {
                    cambiarEstadoSolicitud(username, console, scanner, true);
                    esperarEnter(console, scanner);
                }
                case "3" -> {
                    cambiarEstadoSolicitud(username, console, scanner, false);
                    esperarEnter(console, scanner);
                }
                case "4" -> mostrarSolicitudesConductor(
                        solicitudService.listarAsignadas(username),
                        "Aún no tienes solicitudes asignadas.", console, scanner);
                case "5" -> {
                    finalizarSolicitud(username, console, scanner);
                    esperarEnter(console, scanner);
                }
                case "6" -> {
                    System.out.println("Sesión cerrada.");
                    return true;
                }
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    private void mostrarSolicitudesConductor(
            List<Solicitud> solicitudes,
            String mensajeVacio,
            Console console,
            Scanner scanner) {
        if (solicitudes.isEmpty()) {
            System.out.println(mensajeVacio);
            esperarEnter(console, scanner);
            return;
        }
        System.out.println("\nSolicitudes de tu flota:");
        for (Solicitud solicitud : solicitudes) {
            System.out.printf(
                    "#%d | Cliente: %s | Dirección: %s | %s | %d kg | Estado: %s | Comuna: %s%n",
                    solicitud.getId(),
                    solicitud.getNombreCliente(),
                    solicitud.getDireccionCliente(),
                    solicitud.getTipoBasura(),
                    solicitud.getPesoKg(),
                    solicitud.getEstado(),
                    solicitud.getFlota().getComuna());
        }
        esperarEnter(console, scanner);
    }

    private void cambiarEstadoSolicitud(
            String username,
            Console console,
            Scanner scanner,
            boolean aceptar) {
        String accion = aceptar ? "aceptar" : "rechazar";
        Long id = leerIdSolicitud("Número de solicitud que deseas " + accion + ": ", console, scanner);
        if (id == null) {
            return;
        }
        try {
            Solicitud solicitud = aceptar
                    ? solicitudService.aceptar(id, username)
                    : solicitudService.rechazar(id, username);
            if (aceptar) {
                System.out.printf("Solicitud #%d aceptada y asignada a tu camión.%n", solicitud.getId());
            } else {
                System.out.printf(
                        "Solicitud #%d rechazada. Seguirá pendiente para otros conductores de la flota.%n",
                        solicitud.getId());
            }
        } catch (ResponseStatusException exception) {
            System.out.printf("No se pudo %s la solicitud: %s%n", accion, exception.getReason());
        }
    }

    private void finalizarSolicitud(String username, Console console, Scanner scanner) {
        Long id = leerIdSolicitud("Número de solicitud que deseas finalizar: ", console, scanner);
        if (id == null) {
            return;
        }
        try {
            Solicitud solicitud = solicitudService.completar(id, username);
            System.out.printf("Solicitud #%d finalizada correctamente.%n", solicitud.getId());
        } catch (ResponseStatusException exception) {
            System.out.printf("No se pudo finalizar la solicitud: %s%n", exception.getReason());
        }
    }

    private Long leerIdSolicitud(String mensaje, Console console, Scanner scanner) {
        while (true) {
            String entrada = leerLinea(mensaje, console, scanner);
            if (entrada == null) {
                return null;
            }
            try {
                long id = Long.parseLong(entrada);
                if (id > 0) {
                    return id;
                }
            } catch (NumberFormatException exception) {
                System.out.println("Ingresa un número de solicitud válido.");
                continue;
            }
            System.out.println("El número de solicitud debe ser mayor que cero.");
        }
    }

    private void mostrarSolicitudes(String username) {
        List<Solicitud> solicitudes = solicitudService.listarPropias(username);
        if (solicitudes.isEmpty()) {
            System.out.println("\nAún no tienes solicitudes creadas.");
            return;
        }

        DateTimeFormatter formatoFecha = DateTimeFormatter
                .ofPattern("dd-MM-yyyy HH:mm")
                .withZone(ZoneId.systemDefault());
        System.out.println("\nTus solicitudes:");
        for (Solicitud solicitud : solicitudes) {
            String fecha = solicitud.getFechaCreacion() == null
                    ? "sin fecha"
                    : formatoFecha.format(solicitud.getFechaCreacion());
            System.out.printf(
                    "#%d | %s | %d kg | %s | Comuna: %s | Creada: %s%n",
                    solicitud.getId(),
                    solicitud.getTipoBasura(),
                    solicitud.getPesoKg(),
                    solicitud.getEstado(),
                    solicitud.getFlota().getComuna(),
                    fecha);
        }
    }

    private void crearSolicitud(String username, Console console, Scanner scanner) {
        System.out.println("\n--- FORMULARIO DE NUEVA SOLICITUD ---");
        TipoBasura[] tipos = TipoBasura.values();
        for (int index = 0; index < tipos.length; index++) {
            System.out.printf("%d. %s%n", index + 1, tipos[index]);
        }

        TipoBasura tipoBasura = leerTipoBasura(tipos, console, scanner);
        if (tipoBasura == null) {
            return;
        }
        Integer pesoKg = leerPeso(console, scanner);
        if (pesoKg == null) {
            return;
        }

        try {
            Solicitud solicitud = solicitudService.crear(
                    username, new NuevaSolicitudRequest(tipoBasura, pesoKg));
            System.out.printf(
                    "Solicitud #%d creada correctamente. Comuna asignada: %s.%n",
                    solicitud.getId(),
                    solicitud.getFlota().getComuna());
        } catch (ResponseStatusException exception) {
            System.out.printf("No se pudo crear la solicitud: %s%n", exception.getReason());
        }
    }

    private TipoBasura leerTipoBasura(TipoBasura[] tipos, Console console, Scanner scanner) {
        while (true) {
            String entrada = leerLinea("Tipo de residuo: ", console, scanner);
            if (entrada == null) {
                return null;
            }
            try {
                int opcion = Integer.parseInt(entrada);
                if (opcion >= 1 && opcion <= tipos.length) {
                    return tipos[opcion - 1];
                }
            } catch (NumberFormatException exception) {
                System.out.println("Ingresa el número de una opción válida.");
                continue;
            }
            System.out.println("Opción no válida.");
        }
    }

    private Integer leerPeso(Console console, Scanner scanner) {
        while (true) {
            String entrada = leerLinea("Peso estimado en kilogramos: ", console, scanner);
            if (entrada == null) {
                return null;
            }
            try {
                int pesoKg = Integer.parseInt(entrada);
                if (pesoKg > 0) {
                    return pesoKg;
                }
            } catch (NumberFormatException exception) {
                System.out.println("Ingresa un peso en kilogramos usando números enteros.");
                continue;
            }
            System.out.println("El peso debe ser mayor que cero.");
        }
    }

    private Optional<Usuario> iniciarSesion(Console console, Scanner scanner) {
        while (true) {
            String username = leerLinea("Usuario: ", console, scanner);
            if (username == null) {
                return Optional.empty();
            }
            String password = leerContrasena(console, scanner);
            if (password == null) {
                return Optional.empty();
            }

            Optional<Usuario> usuario = authService.autenticar(username, password);
            if (usuario.isPresent()) {
                return usuario;
            }
            System.out.println("Usuario o contraseña incorrectos. Intenta nuevamente.");
        }
    }

    private String leerLinea(String mensaje, Console console, Scanner scanner) {
        if (console != null) {
            return console.readLine(mensaje);
        }
        System.out.print(mensaje);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : null;
    }

    private String leerContrasena(Console console, Scanner scanner) {
        if (console != null) {
            char[] password = console.readPassword("Contraseña: ");
            return password == null ? null : new String(password);
        }
        return leerLinea("Contraseña: ", null, scanner);
    }

    private void esperarEnter(Console console, Scanner scanner) {
        leerLinea("\nPresiona Enter para volver al menú...", console, scanner);
    }
}
