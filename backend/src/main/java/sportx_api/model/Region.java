package sportx_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "regiones")
public class Region {

    @Id
    @Column(name = "id_region")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    public Region() {
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
