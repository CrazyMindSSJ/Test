package cl.duoc.productos_service.console;

import cl.duoc.productos_service.model.Usuario;
import cl.duoc.productos_service.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.Console;
import java.util.Optional;
import java.util.Scanner;

@Component
@Profile("login-console")
@Order(Ordered.LOWEST_PRECEDENCE)
public class ConsolaInicioSesion implements CommandLineRunner {
    private final AuthService authService;

    public ConsolaInicioSesion(AuthService authService) {
        this.authService = authService;
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

            System.out.printf(
                    "%nSesión iniciada: %s (rol: %s).%n",
                    usuario.get().getUsername(),
                    usuario.get().getRol());
            while (true) {
                String accion = leerLinea(
                        "1. Cerrar sesión   0. Salir\nSelecciona una opción: ",
                        console,
                        scanner);
                if (accion == null || accion.equals("0")) {
                    return;
                }
                if (accion.equals("1")) {
                    System.out.println("Sesión cerrada.");
                    break;
                }
                System.out.println("Opción no válida.");
            }
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
}
