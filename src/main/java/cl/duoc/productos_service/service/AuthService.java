package cl.duoc.productos_service.service;

import cl.duoc.productos_service.dto.RegistroRequest;
import cl.duoc.productos_service.model.ComunaCamion;
import cl.duoc.productos_service.model.Usuario;
import cl.duoc.productos_service.model.UsuCliente;
import cl.duoc.productos_service.repository.CamionRepository;
import cl.duoc.productos_service.repository.OrdenRepository;
import cl.duoc.productos_service.repository.SolicitudRepository;
import cl.duoc.productos_service.repository.UsuarioRepository;
import cl.duoc.productos_service.repository.UsuClienteRepository;
import cl.duoc.productos_service.repository.UsuConductorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final OrdenRepository ordenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuClienteRepository clienteRepository;
    private final UsuConductorRepository conductorRepository;
    private final SolicitudRepository solicitudRepository;
    private final CamionRepository camionRepository;

    public AuthService(
            UsuarioRepository usuarioRepository,
            OrdenRepository ordenRepository,
            PasswordEncoder passwordEncoder,
            UsuClienteRepository clienteRepository,
            UsuConductorRepository conductorRepository,
            SolicitudRepository solicitudRepository,
            CamionRepository camionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.ordenRepository = ordenRepository;
        this.passwordEncoder = passwordEncoder;
        this.clienteRepository = clienteRepository;
        this.conductorRepository = conductorRepository;
        this.solicitudRepository = solicitudRepository;
        this.camionRepository = camionRepository;
    }

    public Optional<Usuario> autenticar(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        return usuarioRepository.findByUsernameIgnoreCase(normalizarUsername(username))
                .filter(usuario -> passwordEncoder.matches(password, usuario.getPassword()));
    }

    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsernameIgnoreCase(username);
    }

    @Transactional
    public Usuario registrar(String username, String password) {
        return crear(username, password, "USER");
    }

    @Transactional
    public Usuario registrar(RegistroRequest request) {
        String username = normalizarUsername(request.username());
        validarNombreYClave(username, request.password());
        String rut = normalizarRut(request.rut());
        validarRutDisponible(rut);
        if (usuarioRepository.existsByUsernameIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese nombre de usuario ya está registrado");
        }

        Usuario usuario = crearEntidad(username, request.password(), "CLIENTE");
        usuario.setRut(rut);
        usuario.setNombre(request.nombre().trim());
        usuario.setApellido(request.apellido().trim());
        usuario.setEdad(request.edad());
        usuario.setComuna(request.comuna());
        usuario.setCalle(request.calle().trim());
        usuario.setNumCalle(request.numCalle());
        usuario = usuarioRepository.save(usuario);
        clienteRepository.save(new UsuCliente(usuario));
        return usuario;
    }

    @Transactional
    public Usuario crear(String username, String password, String rol) {
        String nombreNormalizado = normalizarUsername(username);
        validarNombreYClave(nombreNormalizado, password);
        String rolNormalizado = normalizarRol(rol);
        if (usuarioRepository.existsByUsernameIgnoreCase(nombreNormalizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese nombre de usuario ya está registrado");
        }
        Usuario usuario = crearEntidad(nombreNormalizado, password, rolNormalizado);
        usuario.setRut("AUTO-" + UUID.randomUUID().toString().replace("-", "").substring(0, 15));
        usuario.setNombre(nombreNormalizado);
        usuario.setApellido("Usuario");
        usuario.setEdad(18);
        usuario.setComuna(ComunaCamion.LA_FLORIDA);
        usuario.setCalle("Sin especificar");
        usuario.setNumCalle(1);
        usuario = usuarioRepository.save(usuario);
        if ("USER".equals(rolNormalizado) || "CLIENTE".equals(rolNormalizado)) {
            clienteRepository.save(new UsuCliente(usuario));
        }
        return usuario;
    }

    @Transactional
    public Usuario actualizar(
            Long id,
            String username,
            String password,
            String rol,
            Long actorId) {
        Usuario usuario = buscar(id);
        String nombreAnterior = usuario.getUsername();
        String nombreNuevo = normalizarUsername(username);
        validarNombre(nombreNuevo);
        usuarioRepository.findByUsernameIgnoreCase(nombreNuevo)
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT, "Ese nombre de usuario ya está registrado");
                });

        String rolNuevo = normalizarRol(rol);
        if (usuario.getId().equals(actorId)
                && !usuario.getRol().equals(rolNuevo)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "No puedes cambiar tu propio rol");
        }
        if ("ADMIN".equals(usuario.getRol())
                && !"ADMIN".equals(rolNuevo)
                && usuarioRepository.countByRol("ADMIN") <= 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "No se puede quitar el rol del último administrador");
        }
        usuario.setUsername(nombreNuevo);
        usuario.setRol(rolNuevo);
        if (("USER".equals(rolNuevo) || "CLIENTE".equals(rolNuevo))
                && !clienteRepository.existsByUsuario_Id(usuario.getId())) {
            clienteRepository.save(new UsuCliente(usuario));
        }
        if (password != null && !password.isBlank()) {
            validarClave(password);
            usuario.setPassword(passwordEncoder.encode(password));
        }
        if (!nombreAnterior.equals(nombreNuevo)) {
            ordenRepository.actualizarPropietario(nombreAnterior, nombreNuevo);
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminar(Long id, Long actorId) {
        Usuario usuario = buscar(id);
        if (usuario.getId().equals(actorId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No puedes eliminar tu propia cuenta");
        }
        if ("ADMIN".equals(usuario.getRol()) && usuarioRepository.countByRol("ADMIN") <= 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "No se puede eliminar el último administrador");
        }
        if (ordenRepository.existsByNombreUsuario(usuario.getUsername())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "No se puede eliminar un usuario que tiene órdenes registradas");
        }
        if (solicitudRepository.existsByCliente_Usuario_Id(usuario.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "No se puede eliminar un usuario que tiene solicitudes registradas");
        }
        if (camionRepository.existsByConductor_Usuario_Id(usuario.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Asigna el camión a otro conductor antes de eliminar la cuenta");
        }
        clienteRepository.deleteByUsuario_Id(usuario.getId());
        conductorRepository.deleteByUsuario_Id(usuario.getId());
        usuarioRepository.delete(usuario);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAllByOrderByIdAsc();
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAllByRolOrderByUsernameAsc("USER");
    }

    public Usuario buscarPorId(Long id) {
        return buscar(id);
    }

    public List<String> listarNombresDeUsuario() {
        return listarUsuarios().stream()
                .map(usuario -> usuario.getUsername())
                .toList();
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private String normalizarUsername(String username) {
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de usuario es obligatorio");
        }
        return username.trim().toLowerCase();
    }

    private String normalizarRol(String rol) {
        if (rol == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol es obligatorio");
        }
        String normalizado = rol.trim().toUpperCase();
        if (!List.of("USER", "CLIENTE", "ADMIN").contains(normalizado)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El rol debe ser USER, CLIENTE o ADMIN");
        }
        return normalizado;
    }

    private void validarNombreYClave(String username, String password) {
        validarNombre(username);
        validarClave(password);
    }

    private void validarNombre(String username) {
        if (username.isBlank() || username.length() > 80) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El nombre de usuario debe tener entre 1 y 80 caracteres");
        }
    }

    private void validarClave(String password) {
        if (password == null || password.length() < 4
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 4 caracteres y máximo 72 bytes UTF-8");
        }
    }

    private Usuario crearEntidad(String username, String password, String rol) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rol);
        return usuario;
    }

    private String normalizarRut(String rut) {
        if (rut == null || rut.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El RUT es obligatorio");
        }
        return rut.trim().toUpperCase();
    }

    private void validarRutDisponible(String rut) {
        if (usuarioRepository.existsByRutIgnoreCase(rut)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese RUT ya está registrado");
        }
    }
}
