package cl.duoc.productos_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank @Size(max = 80) String username,
        @NotBlank @Size(min = 4, max = 72) String password) {
}
