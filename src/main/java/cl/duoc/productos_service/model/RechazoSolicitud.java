package cl.duoc.productos_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "rechazos_solicitud", uniqueConstraints = @UniqueConstraint(
        columnNames = {"solicitud_id", "conductor_id"}))
public class RechazoSolicitud {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conductor_id", nullable = false)
    private UsuConductor conductor;

    protected RechazoSolicitud() {
    }

    public RechazoSolicitud(Solicitud solicitud, UsuConductor conductor) {
        this.solicitud = solicitud;
        this.conductor = conductor;
    }

    public Solicitud getSolicitud() {
        return solicitud;
    }
}
