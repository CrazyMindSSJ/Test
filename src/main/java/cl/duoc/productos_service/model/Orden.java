package cl.duoc.productos_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "ordenes")
public class Orden {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreUsuario;

    @Column(nullable = false)
    private String comuna;

    @Column(nullable = false)
    private String calle;

    @Column(name = "num_calle", nullable = false)
    private Integer numCalle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoBasura tipoBasura;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOrden estado;

    @PrePersist
    void asignarEstadoInicial() {
        if (estado == null) {
            estado = EstadoOrden.EN_PROCESO;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getComuna() {
        return comuna;
    }

    public void setComuna(String comuna) {
        this.comuna = comuna;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public Integer getNumCalle() {
        return numCalle;
    }

    public void setNumCalle(Integer numCalle) {
        this.numCalle = numCalle;
    }

    public TipoBasura getTipoBasura() {
        return tipoBasura;
    }

    public void setTipoBasura(TipoBasura tipoBasura) {
        this.tipoBasura = tipoBasura;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public void setEstado(EstadoOrden estado) {
        this.estado = estado;
    }
}
