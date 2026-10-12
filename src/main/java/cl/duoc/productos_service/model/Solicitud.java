package cl.duoc.productos_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "solicitudes")
public class Solicitud {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solid")
    private Long id;

    @Column(name = "nombre_cliente", nullable = false, length = 161)
    private String nombreCliente;

    @Column(name = "direccion_cliente", nullable = false, length = 250)
    private String direccionCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_basura", nullable = false, length = 30)
    private TipoBasura tipoBasura;

    @Column(name = "peso_kg", nullable = false)
    private Integer pesoKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_solid", nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private UsuCliente cliente;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_flota", nullable = false)
    private Flota flota;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camion_id")
    private Camion camion;

    @Column(name = "tiempo_espera", nullable = false)
    private Integer tiempoEsperaMinutos = 0;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    public Solicitud() {
    }

    public Long getId() {
        return id;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getDireccionCliente() {
        return direccionCliente;
    }

    public void setDireccionCliente(String direccionCliente) {
        this.direccionCliente = direccionCliente;
    }

    public TipoBasura getTipoBasura() {
        return tipoBasura;
    }

    public void setTipoBasura(TipoBasura tipoBasura) {
        this.tipoBasura = tipoBasura;
    }

    public Integer getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(Integer pesoKg) {
        this.pesoKg = pesoKg;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public UsuCliente getCliente() {
        return cliente;
    }

    public void setCliente(UsuCliente cliente) {
        this.cliente = cliente;
    }

    public Flota getFlota() {
        return flota;
    }

    @JsonProperty("id_flota")
    public Long getIdFlota() {
        return flota == null ? null : flota.getId();
    }

    public void setFlota(Flota flota) {
        this.flota = flota;
    }

    public Camion getCamion() {
        return camion;
    }

    @JsonProperty("camion_id")
    public String getCamionId() {
        return camion == null ? null : camion.getPatente();
    }

    public void setCamion(Camion camion) {
        this.camion = camion;
    }

    public Integer getTiempoEsperaMinutos() {
        return tiempoEsperaMinutos;
    }

    public void setTiempoEsperaMinutos(Integer tiempoEsperaMinutos) {
        this.tiempoEsperaMinutos = tiempoEsperaMinutos;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    @jakarta.persistence.PrePersist
    void asignarFechaCreacion() {
        if (fechaCreacion == null) {
            fechaCreacion = Instant.now();
        }
    }
}
