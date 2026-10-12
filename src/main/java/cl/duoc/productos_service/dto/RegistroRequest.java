package cl.duoc.productos_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import cl.duoc.productos_service.model.ComunaCamion;

public record RegistroRequest(
        @NotBlank @Size(max = 80) String username,
        @NotBlank @Size(min = 4, max = 72) String password,
        @NotBlank @Size(max = 20) String rut,
        @NotBlank @Size(max = 80) String nombre,
        @NotBlank @Size(max = 80) String apellido,
        @NotNull @Min(18) @Max(120) Integer edad,
        @NotNull ComunaCamion comuna,
        @NotBlank @Size(max = 120) String calle,
        @NotNull @Positive Integer numCalle) {
}
