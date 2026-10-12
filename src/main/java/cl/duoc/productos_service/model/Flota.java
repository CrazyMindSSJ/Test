package cl.duoc.productos_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flotas", uniqueConstraints = @UniqueConstraint(columnNames = "comuna"))
public class Flota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_flota")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComunaCamion comuna;

    @JsonIgnore
    @OneToMany(mappedBy = "flota")
    private List<Camion> camiones = new ArrayList<>();

    protected Flota() {
    }

    public Flota(ComunaCamion comuna) {
        this.comuna = comuna;
    }

    public Long getId() {
        return id;
    }

    public ComunaCamion getComuna() {
        return comuna;
    }

    public void setComuna(ComunaCamion comuna) {
        this.comuna = comuna;
    }

    public List<Camion> getCamiones() {
        return camiones;
    }
}
