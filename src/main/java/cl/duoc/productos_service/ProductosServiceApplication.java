package cl.duoc.productos_service;

import cl.duoc.productos_service.model.Usuario;
import cl.duoc.productos_service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Optional;

@SpringBootApplication
public class ProductosServiceApplication implements CommandLineRunner {

    @Autowired
    private AuthService authService;

    public static void main(String[] args) {
        SpringApplication.run(ProductosServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========================================");
        System.out.println("   INICIANDO PRUEBAS DE AUTENTICACIÓN   ");
        System.out.println("========================================");

        // Prueba 1: Credenciales correctas
        probarLogin("sebastian", "password567");

        // Prueba 2: Contraseña incorrecta
        probarLogin("sebastian", "clave_incorrecta");

        // Prueba 3: Usuario que no existe
        probarLogin("carlos", "1234");

        System.out.println("========================================\n");
    }

    private void probarLogin(String username, String password) {
        Optional<Usuario> resultado = authService.autenticar(username, password);
        
        System.out.print("Probando usuario: '" + username + "' ... ");
        if (resultado.isPresent()) {
            System.out.println("[ÉXITO] Autenticado con rol: " + resultado.get().getRol());
        } else {
            System.out.println("[FALLO] Acceso denegado (Credenciales inválidas)");
        }
    }
}