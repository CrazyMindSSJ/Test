package cl.duoc.productos_service.dto;

import cl.duoc.productos_service.model.ComunaCamion;
import cl.duoc.productos_service.model.FlotaCamion;
import cl.duoc.productos_service.model.TipoBasura;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CamionRequest(
        @NotBlank @Size(max = 20) String patente,
        @NotNull @Positive Integer capacidadKg,
        @NotNull TipoBasura tipoBasura,
        @NotNull ComunaCamion comunaAsignada,
        @NotNull FlotaCamion flota) {
}
