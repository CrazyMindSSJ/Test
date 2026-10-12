package cl.duoc.productos_service.dto;

import cl.duoc.productos_service.model.ComunaCamion;
import cl.duoc.productos_service.model.FlotaCamion;
import cl.duoc.productos_service.model.TipoBasura;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CamionRequest(
        @NotBlank @Size(max = 20) String patente,
        @JsonProperty("peso_carga") @JsonAlias("capacidadKg") @NotNull @Positive Integer capacidadKg,
        TipoBasura tipoBasura,
        ComunaCamion comunaAsignada,
        FlotaCamion flota,
        @JsonProperty("id_flota") Long flotaId,
        @JsonProperty("id_conductor") Long conductorId,
        LocalDate fechaRevisionTecnica,
        Boolean revisionTecnicaVigente,
        LocalDate fechaEmisionGases,
        Boolean emisionGasesVigente,
        LocalDate fechaPermisoCirculacion,
        Boolean permisoCirculacionVigente) {
    public CamionRequest(
            String patente,
            Integer capacidadKg,
            TipoBasura tipoBasura,
            ComunaCamion comunaAsignada,
            FlotaCamion flota) {
        this(patente, capacidadKg, tipoBasura, comunaAsignada, flota,
                null, null, null, null, null, null, null, null);
    }
}
