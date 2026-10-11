package cl.duoc.productos_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioAdminRequest(
        @NotBlank @Size(max = 80) String username,
        String password,
        @NotBlank @Pattern(regexp = "(?i)USER|ADMIN") String rol) {
}
