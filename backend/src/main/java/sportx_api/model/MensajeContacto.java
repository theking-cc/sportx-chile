package sportx_api.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mensajes_contacto")
public class MensajeContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    private Long id;

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "asunto", nullable = false, length = 150)
    private String asunto;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    // Nuevo, Leido, Respondido
    @Column(name = "estado", nullable = false)
    private String estado = "Nuevo";

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    public MensajeContacto() {
    }

    public Long getId() { return id; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
