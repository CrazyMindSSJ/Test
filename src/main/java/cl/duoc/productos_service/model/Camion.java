package cl.duoc.productos_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "camiones")
public class Camion {
    @Id
    @Column(nullable = false, length = 20)
    private String patente;

    @Column(nullable = false)
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

    public String getPatente() {
        return patente;
    }

    public void setPatente(String patente) {
        this.patente = patente;
    }

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
}
