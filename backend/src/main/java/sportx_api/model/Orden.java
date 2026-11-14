package sportx_api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "ordenes")
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden")
    private Long id;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "fecha_orden", nullable = false)
    private LocalDateTime fechaOrden;

    // Pendiente, Preparando, Enviado, Entregado, Cancelado
    @Column(name = "estado", nullable = false)
    private String estado = "Pendiente";

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 0)
    private BigDecimal subtotal;

    @Column(name = "descuento", nullable = false, precision = 12, scale = 0)
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(name = "monto_total", nullable = false, precision = 12, scale = 0)
    private BigDecimal montoTotal;

    @Column(name = "codigo_cupon", length = 50)
    private String codigoCupon;

    @Column(name = "destinatario", nullable = false, length = 150)
    private String destinatario;

    @Column(name = "email_contacto", nullable = false, length = 100)
    private String emailContacto;

    @Column(name = "direccion_entrega", nullable = false, length = 300)
    private String direccionEntrega;

    @Column(name = "region_entrega", nullable = false, length = 100)
    private String regionEntrega;

    @Column(name = "comuna_entrega", nullable = false, length = 100)
    private String comunaEntrega;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @Column(name = "stock_repuesto", nullable = false)
    private Boolean stockRepuesto = false;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<DetalleOrden> detalles = new ArrayList<>();

    public Orden() {
    }

    public void agregarDetalle(DetalleOrden detalle) {
        detalle.setOrden(this);
        detalles.add(detalle);
    }

    public Long getId() { return id; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public LocalDateTime getFechaOrden() { return fechaOrden; }
    public void setFechaOrden(LocalDateTime fechaOrden) { this.fechaOrden = fechaOrden; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }
    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }
    public String getCodigoCupon() { return codigoCupon; }
    public void setCodigoCupon(String codigoCupon) { this.codigoCupon = codigoCupon; }
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public String getEmailContacto() { return emailContacto; }
    public void setEmailContacto(String emailContacto) { this.emailContacto = emailContacto; }
    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }
    public String getRegionEntrega() { return regionEntrega; }
    public void setRegionEntrega(String regionEntrega) { this.regionEntrega = regionEntrega; }
    public String getComunaEntrega() { return comunaEntrega; }
    public void setComunaEntrega(String comunaEntrega) { this.comunaEntrega = comunaEntrega; }
    public LocalDateTime getFechaCancelacion() { return fechaCancelacion; }
    public void setFechaCancelacion(LocalDateTime fechaCancelacion) { this.fechaCancelacion = fechaCancelacion; }
    public Boolean getStockRepuesto() { return stockRepuesto; }
    public void setStockRepuesto(Boolean stockRepuesto) { this.stockRepuesto = stockRepuesto; }
    public List<DetalleOrden> getDetalles() { return detalles; }
}
