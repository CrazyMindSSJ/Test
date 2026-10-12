package cl.duoc.productos_service.console;

import cl.duoc.productos_service.dto.CamionRequest;
import cl.duoc.productos_service.dto.OrdenRequest;
import cl.duoc.productos_service.model.EstadoOrden;
import cl.duoc.productos_service.model.Camion;
import cl.duoc.productos_service.model.ComunaCamion;
import cl.duoc.productos_service.model.FlotaCamion;
import cl.duoc.productos_service.model.Orden;
import cl.duoc.productos_service.model.TipoBasura;
import cl.duoc.productos_service.model.Usuario;
import cl.duoc.productos_service.service.AuthService;
import cl.duoc.productos_service.service.CamionService;
import cl.duoc.productos_service.service.OrdenService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
@Profile("console")
@Order(Ordered.LOWEST_PRECEDENCE)
public class ConsolaOrdenes implements CommandLineRunner {
    private final AuthService authService;
    private final OrdenService ordenService;
    private final CamionService camionService;

    public ConsolaOrdenes(AuthService authService, OrdenService ordenService, CamionService camionService) {
        this.authService = authService;
        this.ordenService = ordenService;
        this.camionService = camionService;
    }

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            Optional<Usuario> usuario = iniciarSesion(scanner);
            if (usuario.isEmpty()) {
                return;
            }

            boolean continuar = "ADMIN".equals(usuario.get().getRol())
                    ? mostrarMenuAdmin(scanner, usuario.get())
                    : mostrarMenuUsuario(scanner, usuario.get().getUsername());
            if (!continuar) {
                return;
            }
        }
    }

    private Optional<Usuario> iniciarSesion(Scanner scanner) {
        while (true) {
            System.out.println("\n======================================");
            System.out.println("       SISTEMA DE GESTIÓN");
            System.out.println("======================================");
            System.out.println("1. Iniciar sesión");
            System.out.println("2. Registrarme como USER");
            System.out.println("0. Salir");
            System.out.print("Selecciona una opción: ");
            String opcion = leerLinea(scanner);
            if (opcion == null || opcion.equals("0")) {
                return Optional.empty();
            }

            if (opcion.equals("2")) {
                Optional<Usuario> registrado = registrarUsuario(scanner);
                if (registrado.isPresent()) {
                    return registrado;
                }
                continue;
            }
            if (!opcion.equals("1")) {
                System.out.println("Opción no válida.");
                continue;
            }

            String username = pedirCampoObligatorio(scanner, "Usuario");
            if (username == null) {
                return Optional.empty();
            }
            System.out.print("Contraseña: ");
            String password = leerLinea(scanner);
            if (password == null) {
                return Optional.empty();
            }

            Optional<Usuario> usuario = authService.autenticar(username, password);
            if (usuario.isPresent()) {
                System.out.println("Sesión iniciada como " + usuario.get().getUsername() + ".");
                return usuario;
            }
            System.out.println("Usuario o contraseña incorrectos. Intenta nuevamente.");
        }
    }

    private Optional<Usuario> registrarUsuario(Scanner scanner) {
        System.out.println("\n--- Registro de usuario USER ---");
        String username = pedirCampoObligatorio(scanner, "Nombre de usuario");
        if (username == null) {
            return Optional.empty();
        }
        System.out.print("Contraseña (mínimo 4 caracteres): ");
        String password = leerLinea(scanner);
        if (password == null) {
            return Optional.empty();
        }
        try {
            Usuario usuario = authService.registrar(username, password);
            System.out.println("Cuenta USER creada. Sesión iniciada como " + usuario.getUsername() + ".");
            return Optional.of(usuario);
        } catch (ResponseStatusException exception) {
            System.out.println("No se pudo registrar: " + exception.getReason());
            return Optional.empty();
        }
    }

    private boolean mostrarMenuUsuario(Scanner scanner, String username) {
        while (true) {
            System.out.println("\n----------- ÓRDENES -----------");
            System.out.println("Usuario: " + username);
            System.out.println("1. Crear orden");
            System.out.println("2. Listar mis órdenes");
            System.out.println("3. Modificar orden");
            System.out.println("4. Eliminar orden");
            System.out.println("5. Cerrar sesión");
            System.out.println("0. Salir");
            System.out.print("Selecciona una opción: ");
            String opcion = leerLinea(scanner);
            if (opcion == null || opcion.equals("0")) {
                return false;
            }

            switch (opcion) {
                case "1" -> crearOrden(scanner, username);
                case "2" -> listarOrdenes(username);
                case "3" -> modificarOrden(scanner, username);
                case "4" -> eliminarOrden(scanner, username);
                case "5" -> {
                    System.out.println("Sesión cerrada.");
                    return true;
                }
                default -> System.out.println("Opción no válida. Selecciona una opción del menú.");
            }
        }
    }

    private boolean mostrarMenuAdmin(Scanner scanner, Usuario admin) {
        while (true) {
            System.out.println("\n----------- ADMINISTRACIÓN -----------");
            System.out.println("Administrador: " + admin.getUsername());
            System.out.println("1. Administrar usuarios");
            System.out.println("2. Administrar camiones");
            System.out.println("3. Cerrar sesión");
            System.out.println("0. Salir");
            System.out.print("Selecciona una opción: ");
            String opcion = leerLinea(scanner);
            if (opcion == null || opcion.equals("0")) {
                return false;
            }
            switch (opcion) {
                case "1" -> administrarUsuarios(scanner, admin.getId());
                case "2" -> administrarCamiones(scanner);
                case "3" -> {
                    System.out.println("Sesión cerrada.");
                    return true;
                }
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    private void administrarUsuarios(Scanner scanner, Long adminId) {
        while (true) {
            System.out.println("\n----------- USUARIOS -----------");
            System.out.println("1. Listar usuarios");
            System.out.println("2. Crear usuario");
            System.out.println("3. Editar usuario");
            System.out.println("4. Eliminar usuario");
            System.out.println("0. Volver");
            System.out.print("Selecciona una opción: ");
            String opcion = leerLinea(scanner);
            if (opcion == null || opcion.equals("0")) {
                return;
            }
            switch (opcion) {
                case "1" -> listarUsuarios();
                case "2" -> crearUsuario(scanner);
                case "3" -> editarUsuario(scanner, adminId);
                case "4" -> eliminarUsuario(scanner, adminId);
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    private void listarUsuarios() {
        System.out.println("\n--- Usuarios registrados ---");
        List<Usuario> usuarios = authService.listarTodos();
        if (usuarios.isEmpty()) {
            System.out.println("No hay usuarios registrados.");
            return;
        }
        usuarios.forEach(usuario -> System.out.printf(
                "ID: %d | Usuario: %s | Rol: %s%n",
                usuario.getId(), usuario.getUsername(), usuario.getRol()));
    }

    private void crearUsuario(Scanner scanner) {
        System.out.println("\n--- Crear usuario ---");
        String username = pedirCampoObligatorio(scanner, "Nombre de usuario");
        if (username == null) {
            return;
        }
        String password = pedirClave(scanner, false);
        if (password == null) {
            return;
        }
        String rol = pedirRol(scanner);
        if (rol == null) {
            return;
        }
        try {
            Usuario usuario = authService.crear(username, password, rol);
            System.out.println("Usuario creado con ID " + usuario.getId() + ".");
        } catch (ResponseStatusException exception) {
            System.out.println("No se pudo crear el usuario: " + exception.getReason());
        }
    }

    private void editarUsuario(Scanner scanner, Long adminId) {
        System.out.println("\n--- Editar usuario ---");
        Long id = pedirId(scanner, "ID del usuario");
        if (id == null) {
            return;
        }
        Usuario usuario;
        try {
            usuario = authService.buscarPorId(id);
        } catch (ResponseStatusException exception) {
            System.out.println("No se pudo encontrar el usuario: " + exception.getReason());
            return;
        }
        System.out.println("Usuario actual: " + usuario.getUsername() + " (" + usuario.getRol() + ")");
        String username = pedirCampoObligatorio(scanner, "Nuevo nombre de usuario");
        if (username == null) {
            return;
        }
        String password = pedirClave(scanner, true);
        if (password == null) {
            return;
        }
        String rol = pedirRol(scanner);
        if (rol == null) {
            return;
        }
        try {
            authService.actualizar(id, username, password, rol, adminId);
            System.out.println("Usuario actualizado.");
        } catch (ResponseStatusException exception) {
            System.out.println("No se pudo actualizar el usuario: " + exception.getReason());
        }
    }

    private void eliminarUsuario(Scanner scanner, Long adminId) {
        Long id = pedirId(scanner, "ID del usuario que deseas eliminar");
        if (id == null) {
            return;
        }
        try {
            authService.eliminar(id, adminId);
            System.out.println("Usuario eliminado.");
        } catch (ResponseStatusException exception) {
            System.out.println("No se pudo eliminar el usuario: " + exception.getReason());
        }
    }

    private String pedirClave(Scanner scanner, boolean opcional) {
        while (true) {
            System.out.print(opcional
                    ? "Nueva contraseña (Enter para conservar la actual): "
                    : "Contraseña (mínimo 4 caracteres): ");
            String password = leerLinea(scanner);
            if (password == null) {
                return null;
            }
            if (opcional && password.isEmpty()) {
                return "";
            }
            if (password.length() >= 4 && password.getBytes(StandardCharsets.UTF_8).length <= 72) {
                return password;
            }
            System.out.println("La contraseña debe tener al menos 4 caracteres y máximo 72 bytes UTF-8.");
        }
    }

    private String pedirRol(Scanner scanner) {
        while (true) {
            System.out.println("Rol: 1. USER  2. ADMIN");
            System.out.print("Selecciona el rol: ");
            String opcion = leerLinea(scanner);
            if (opcion == null) {
                return null;
            }
            if (opcion.equals("1")) {
                return "USER";
            }
            if (opcion.equals("2")) {
                return "ADMIN";
            }
            System.out.println("Selecciona 1 o 2.");
        }
    }

    private void administrarCamiones(Scanner scanner) {
        while (true) {
            System.out.println("\n----------- CAMIONES -----------");
            System.out.println("1. Listar camiones");
            System.out.println("2. Crear camión");
            System.out.println("3. Editar camión");
            System.out.println("4. Eliminar camión");
            System.out.println("0. Volver");
            System.out.print("Selecciona una opción: ");
            String opcion = leerLinea(scanner);
            if (opcion == null || opcion.equals("0")) {
                return;
            }
            switch (opcion) {
                case "1" -> listarCamiones();
                case "2" -> guardarCamion(scanner, null);
                case "3" -> {
                    System.out.print("Patente del camión a editar: ");
                    String patente = leerLinea(scanner);
                    if (patente != null && !patente.isBlank()) {
                        guardarCamion(scanner, patente);
                    }
                }
                case "4" -> eliminarCamion(scanner);
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    private void listarCamiones() {
        List<Camion> camiones = camionService.listar();
        System.out.println("\n--- Camiones registrados: " + camiones.size() + " ---");
        if (camiones.isEmpty()) {
            System.out.println("No hay camiones registrados.");
            return;
        }
        camiones.forEach(camion -> System.out.printf(
                "Patente: %s | Capacidad: %d kg | Tipo: %s | Comuna: %s | Flota: %s%n",
                camion.getPatente(),
                camion.getCapacidadKg(),
                camion.getTipoBasura(),
                camion.getComunaAsignada(),
                camion.getFlota()));
    }

    private void guardarCamion(Scanner scanner, String patenteExistente) {
        System.out.println(patenteExistente == null ? "\n--- Crear camión ---" : "\n--- Editar camión ---");
        String patente = pedirCampoObligatorio(scanner, "Patente");
        if (patente == null) {
            return;
        }
        Integer capacidad = pedirEnteroPositivo(scanner, "Capacidad en kilogramos");
        if (capacidad == null) {
            return;
        }
        TipoBasura tipo = pedirTipoBasura(scanner);
        if (tipo == null) {
            return;
        }
        ComunaCamion comuna = pedirComunaCamion(scanner);
        if (comuna == null) {
            return;
        }
        FlotaCamion flota = pedirFlotaCamion(scanner);
        if (flota == null) {
            return;
        }
        CamionRequest solicitud = new CamionRequest(patente, capacidad, tipo, comuna, flota);
        try {
            Camion camion = patenteExistente == null
                    ? camionService.crear(solicitud)
                    : camionService.editar(patenteExistente, solicitud);
            System.out.println("Camión guardado: " + camion.getPatente());
        } catch (ResponseStatusException exception) {
            System.out.println("No se pudo guardar el camión: " + exception.getReason());
        }
    }

    private void eliminarCamion(Scanner scanner) {
        System.out.print("Patente del camión a eliminar: ");
        String patente = leerLinea(scanner);
        if (patente == null || patente.isBlank()) {
            return;
        }
        try {
            camionService.eliminar(patente);
            System.out.println("Camión eliminado.");
        } catch (ResponseStatusException exception) {
            System.out.println("No se pudo eliminar el camión: " + exception.getReason());
        }
    }

    private ComunaCamion pedirComunaCamion(Scanner scanner) {
        while (true) {
            System.out.println("Comuna: 1. La Florida  2. Puente Alto  3. La Pintana");
            System.out.print("Selecciona la comuna: ");
            String opcion = leerLinea(scanner);
            if (opcion == null) {
                return null;
            }
            switch (opcion) {
                case "1":
                    return ComunaCamion.LA_FLORIDA;
                case "2":
                    return ComunaCamion.PUENTE_ALTO;
                case "3":
                    return ComunaCamion.LA_PINTANA;
                default:
                    System.out.println("Selecciona 1, 2 o 3.");
            }
        }
    }

    private FlotaCamion pedirFlotaCamion(Scanner scanner) {
        while (true) {
            System.out.println("Flota: 1. La Florida  2. Puente Alto  3. La Pintana");
            System.out.print("Selecciona la flota: ");
            String opcion = leerLinea(scanner);
            if (opcion == null) {
                return null;
            }
            switch (opcion) {
                case "1":
                    return FlotaCamion.LA_FLORIDA;
                case "2":
                    return FlotaCamion.PUENTE_ALTO;
                case "3":
                    return FlotaCamion.LA_PINTANA;
                default:
                    System.out.println("Selecciona 1, 2 o 3.");
            }
        }
    }

    private void crearOrden(Scanner scanner, String username) {
        System.out.println("\n--- Crear orden ---");
        String comuna = pedirCampoObligatorio(scanner, "Comuna");
        if (comuna == null) {
            return;
        }
        String calle = pedirCampoObligatorio(scanner, "Calle");
        if (calle == null) {
            return;
        }
        Integer numCalle = pedirNumeroCalle(scanner);
        if (numCalle == null) {
            return;
        }
        TipoBasura tipoBasura = pedirTipoBasura(scanner);
        if (tipoBasura == null) {
            return;
        }

        Orden orden = ordenService.crear(
                username, new OrdenRequest(comuna, calle, numCalle, tipoBasura));
        System.out.println("Orden creada correctamente. ID asignado automáticamente: " + orden.getId());
        imprimirOrden(orden);
    }

    private void listarOrdenes(String username) {
        List<Orden> ordenes = ordenService.listarPropias(username);
        System.out.println("\n--- Mis órdenes ---");
        if (ordenes.isEmpty()) {
            System.out.println("No tienes órdenes registradas.");
            return;
        }
        ordenes.forEach(this::imprimirOrden);
    }

    private void modificarOrden(Scanner scanner, String username) {
        System.out.println("\n--- Modificar orden ---");
        Long id = pedirId(scanner);
        if (id == null) {
            return;
        }
        String comuna = pedirCampoObligatorio(scanner, "Comuna");
        if (comuna == null) {
            return;
        }
        String calle = pedirCampoObligatorio(scanner, "Calle");
        if (calle == null) {
            return;
        }
        Integer numCalle = pedirNumeroCalle(scanner);
        if (numCalle == null) {
            return;
        }
        TipoBasura tipoBasura = pedirTipoBasura(scanner);
        if (tipoBasura == null) {
            return;
        }

        try {
            Orden orden = ordenService.modificar(
                    id, username, new OrdenRequest(comuna, calle, numCalle, tipoBasura));
            System.out.println("Orden modificada.");
            imprimirOrden(orden);
        } catch (ResponseStatusException exception) {
            System.out.println("No se pudo modificar la orden: " + exception.getReason());
        }
    }

    private void eliminarOrden(Scanner scanner, String username) {
        System.out.println("\n--- Eliminar orden ---");
        Long id = pedirId(scanner);
        if (id == null) {
            return;
        }
        try {
            ordenService.eliminar(id, username);
            System.out.println("Orden eliminada.");
        } catch (ResponseStatusException exception) {
            System.out.println("No se pudo eliminar la orden: " + exception.getReason());
        }
    }

    private String pedirCampoObligatorio(Scanner scanner, String nombreCampo) {
        while (true) {
            System.out.print(nombreCampo + ": ");
            String valor = leerLinea(scanner);
            if (valor == null) {
                return null;
            }
            if (!valor.isBlank()) {
                return valor;
            }
            System.out.println(nombreCampo + " no puede estar vacío.");
        }
    }

    private Integer pedirNumeroCalle(Scanner scanner) {
        return pedirEnteroPositivo(scanner, "Número de calle");
    }

    private Integer pedirEnteroPositivo(Scanner scanner, String nombreCampo) {
        while (true) {
            System.out.print(nombreCampo + ": ");
            String valor = leerLinea(scanner);
            if (valor == null) {
                return null;
            }
            try {
                int numero = Integer.parseInt(valor);
                if (numero > 0) {
                    return numero;
                }
            } catch (NumberFormatException ignored) {
                // Invalid numeric input is reported and requested again.
            }
            System.out.println("Ingresa un valor entero mayor que cero para " + nombreCampo.toLowerCase() + ".");
        }
    }

    private TipoBasura pedirTipoBasura(Scanner scanner) {
        while (true) {
            System.out.println("Tipo de basura:");
            System.out.println("1. Escombro");
            System.out.println("2. Basura");
            System.out.println("3. Reciclable");
            System.out.print("Selecciona el tipo: ");
            String opcion = leerLinea(scanner);
            if (opcion == null) {
                return null;
            }
            switch (opcion) {
                case "1":
                    return TipoBasura.ESCOMBRO;
                case "2":
                    return TipoBasura.BASURA;
                case "3":
                    return TipoBasura.RECICLABLE;
                default:
                    System.out.println("Tipo no válido. Selecciona 1, 2 o 3.");
            }
        }
    }

    private Long pedirId(Scanner scanner) {
        return pedirId(scanner, "ID de la orden");
    }

    private Long pedirId(Scanner scanner, String nombreCampo) {
        while (true) {
            System.out.print(nombreCampo + ": ");
            String valor = leerLinea(scanner);
            if (valor == null) {
                return null;
            }
            try {
                long id = Long.parseLong(valor);
                if (id > 0) {
                    return id;
                }
            } catch (NumberFormatException ignored) {
                // A non-numeric ID is handled as invalid input and requested again.
            }
            System.out.println("Ingresa un ID numérico mayor que cero.");
        }
    }

    private void imprimirOrden(Orden orden) {
        System.out.println("--------------------------------");
        System.out.println("ID: " + orden.getId());
        System.out.println("Usuario: " + orden.getNombreUsuario());
        System.out.println("Comuna: " + orden.getComuna());
        System.out.println("Calle: " + orden.getCalle());
        System.out.println("Número de calle: " + orden.getNumCalle());
        System.out.println("Tipo: " + textoTipoBasura(orden.getTipoBasura()));
        System.out.println("Estado: " + textoEstado(orden.getEstado()));
        System.out.println("--------------------------------");
    }

    private String textoTipoBasura(TipoBasura tipo) {
        return switch (tipo) {
            case ESCOMBRO -> "Escombro";
            case BASURA -> "Basura";
            case RECICLABLE -> "Reciclable";
        };
    }

    private String textoEstado(EstadoOrden estado) {
        return switch (estado) {
            case EN_PROCESO -> "En proceso";
            case FINALIZADA -> "Finalizada";
            case RECHAZADA -> "Rechazada";
        };
    }

    private String leerLinea(Scanner scanner) {
        return scanner.hasNextLine() ? scanner.nextLine().trim() : null;
    }
}
