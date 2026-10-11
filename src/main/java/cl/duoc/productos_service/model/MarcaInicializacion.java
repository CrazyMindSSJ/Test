package cl.duoc.productos_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "marcas_inicializacion")
public class MarcaInicializacion {
    @Id
    private String nombre;

    protected MarcaInicializacion() {
    }

    public MarcaInicializacion(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
