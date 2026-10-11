package cl.duoc.productos_service.dto;

import cl.duoc.productos_service.model.TipoBasura;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrdenRequest(
        @NotBlank String comuna,
        @NotBlank String calle,
        @NotNull @Positive @JsonProperty("num_calle") Integer numCalle,
        @NotNull TipoBasura tipoBasura) {
}
