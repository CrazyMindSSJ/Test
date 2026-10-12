package cl.duoc.productos_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "camiones")
public class Camion {
    @Id
    @Column(nullable = false, length = 20)
    private String patente;

    @Column(name = "peso_carga", nullable = false)
    private Integer capacidadKg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoBasura tipoBasura;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComunaCamion comunaAsignada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlotaCamion flota;

    @Column(name = "fecha_revtec")
    private LocalDate fechaRevisionTecnica;

    @Column(name = "est_revtec")
    private Boolean revisionTecnicaVigente;

    @Column(name = "fecha_emigas")
    private LocalDate fechaEmisionGases;

    @Column(name = "est_emigas")
    private Boolean emisionGasesVigente;

    @Column(name = "fecha_percir")
    private LocalDate fechaPermisoCirculacion;

    @Column(name = "est_perci")
    private Boolean permisoCirculacionVigente;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_flota")
    private Flota flotaEntidad;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conductor", unique = true)
    private UsuConductor conductor;

    @JsonIgnore
    @OneToMany(mappedBy = "camion")
    private List<Solicitud> solicitudesAsignadas = new ArrayList<>();

    public String getPatente() {
        return patente;
    }

    public void setPatente(String patente) {
        this.patente = patente;
    }

    @JsonProperty("peso_carga")
    public Integer getCapacidadKg() {
        return capacidadKg;
    }

    public void setCapacidadKg(Integer capacidadKg) {
        this.capacidadKg = capacidadKg;
    }

    public TipoBasura getTipoBasura() {
        return tipoBasura;
    }

    public void setTipoBasura(TipoBasura tipoBasura) {
        this.tipoBasura = tipoBasura;
    }

    public ComunaCamion getComunaAsignada() {
        return comunaAsignada;
    }

    public void setComunaAsignada(ComunaCamion comunaAsignada) {
        this.comunaAsignada = comunaAsignada;
    }

    public FlotaCamion getFlota() {
        return flota;
    }

    public void setFlota(FlotaCamion flota) {
        this.flota = flota;
    }

    public LocalDate getFechaRevisionTecnica() {
        return fechaRevisionTecnica;
    }

    public void setFechaRevisionTecnica(LocalDate fechaRevisionTecnica) {
        this.fechaRevisionTecnica = fechaRevisionTecnica;
    }

    public Boolean getRevisionTecnicaVigente() {
        return revisionTecnicaVigente;
    }

    public void setRevisionTecnicaVigente(Boolean revisionTecnicaVigente) {
        this.revisionTecnicaVigente = revisionTecnicaVigente;
    }

    public LocalDate getFechaEmisionGases() {
        return fechaEmisionGases;
    }

    public void setFechaEmisionGases(LocalDate fechaEmisionGases) {
        this.fechaEmisionGases = fechaEmisionGases;
    }

    public Boolean getEmisionGasesVigente() {
        return emisionGasesVigente;
    }

    public void setEmisionGasesVigente(Boolean emisionGasesVigente) {
        this.emisionGasesVigente = emisionGasesVigente;
    }

    public LocalDate getFechaPermisoCirculacion() {
        return fechaPermisoCirculacion;
    }

    public void setFechaPermisoCirculacion(LocalDate fechaPermisoCirculacion) {
        this.fechaPermisoCirculacion = fechaPermisoCirculacion;
    }

    public Boolean getPermisoCirculacionVigente() {
        return permisoCirculacionVigente;
    }

    public void setPermisoCirculacionVigente(Boolean permisoCirculacionVigente) {
        this.permisoCirculacionVigente = permisoCirculacionVigente;
    }

    public Flota getFlotaEntidad() {
        return flotaEntidad;
    }

    @JsonProperty("id_flota")
    public Long getIdFlota() {
        return flotaEntidad == null ? null : flotaEntidad.getId();
    }

    public void setFlotaEntidad(Flota flotaEntidad) {
        this.flotaEntidad = flotaEntidad;
    }

    public UsuConductor getConductor() {
        return conductor;
    }

    @JsonProperty("id_conductor")
    public Long getIdConductor() {
        return conductor == null ? null : conductor.getId();
    }

    public void setConductor(UsuConductor conductor) {
        this.conductor = conductor;
    }

    public List<Solicitud> getSolicitudesAsignadas() {
        return solicitudesAsignadas;
    }
}
