package cl.duoc.productos_service.dto;

import cl.duoc.productos_service.model.TipoBasura;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record NuevaSolicitudRequest(
        @NotNull TipoBasura tipoBasura,
        @NotNull @Positive Integer pesoKg) {
}
