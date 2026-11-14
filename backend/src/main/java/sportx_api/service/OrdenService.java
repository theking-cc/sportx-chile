package sportx_api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import sportx_api.dto.ComunaResponse;
import sportx_api.dto.CrearOrdenRequest;
import sportx_api.dto.OrdenItemResponse;
import sportx_api.dto.OrdenResponse;
import sportx_api.model.Carrito;
import sportx_api.model.DetalleCarrito;
import sportx_api.model.DetalleOrden;
import sportx_api.model.Orden;
import sportx_api.model.Producto;
import sportx_api.model.Usuario;
import sportx_api.repository.CarritoRepository;
import sportx_api.repository.DetalleCarritoRepository;
import sportx_api.repository.OrdenRepository;
import sportx_api.repository.ProductoRepository;
import sportx_api.repository.UsuarioRepository;

@Service
public class OrdenService {

    // Cupones vigentes (los mismos que muestra el frontend).
    private static final BigDecimal DUOC10_PORCENTAJE = new BigDecimal("0.10");
    private static final BigDecimal SPORTX2026_MONTO = new BigDecimal("5000");

    // Cambios de estado permitidos.
    private static final Map<String, Set<String>> TRANSICIONES = Map.of(
        "Pendiente", Set.of("Preparando", "Cancelado"),
        "Preparando", Set.of("Enviado", "Cancelado"),
        "Enviado", Set.of("Entregado"),
        "Entregado", Set.of(),
        "Cancelado", Set.of()
    );

    private final OrdenRepository ordenRepository;
    private final CarritoRepository carritoRepository;
    private final DetalleCarritoRepository detalleCarritoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UbicacionService ubicacionService;

    public OrdenService(
            OrdenRepository ordenRepository,
            CarritoRepository carritoRepository,
            DetalleCarritoRepository detalleCarritoRepository,
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository,
            UbicacionService ubicacionService) {

        this.ordenRepository = ordenRepository;
        this.carritoRepository = carritoRepository;
        this.detalleCarritoRepository = detalleCarritoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.ubicacionService = ubicacionService;
    }

    // =========================================
    // 1. CONFIRMAR COMPRA DESDE EL CARRITO
    // =========================================
    @Transactional
    public OrdenResponse crear(Long usuarioId, CrearOrdenRequest datos) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Usuario no identificado"));

        Carrito carrito = carritoRepository.findByIdUsuario(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "El carrito está vacío"));

        List<DetalleCarrito> items =
            detalleCarritoRepository.findByIdCarritoOrderByIdAsc(carrito.getId());

        if (items.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "El carrito está vacío");
        }

        ComunaResponse comuna = ubicacionService.buscarComuna(datos.idComuna());

        Orden orden = new Orden();
        orden.setIdUsuario(usuarioId);
        orden.setFechaOrden(LocalDateTime.now());
        orden.setEstado("Pendiente");
        orden.setDestinatario(
            (usuario.getNombre() + " " + usuario.getApellidos()).trim());
        orden.setEmailContacto(usuario.getEmail());
        orden.setDireccionEntrega(datos.direccion().trim());
        orden.setRegionEntrega(comuna.region());
        orden.setComunaEntrega(comuna.nombre());

        BigDecimal subtotal = BigDecimal.ZERO;

        for (DetalleCarrito item : items) {

            // Bloquea el producto para que dos compras no vendan el mismo stock.
            Producto producto = productoRepository
                .findByIdParaActualizar(item.getIdProducto())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.CONFLICT, "Un producto del carrito ya no existe"));

            if (!Boolean.TRUE.equals(producto.getActivo())
                    || !Boolean.TRUE.equals(producto.getCategoria().getActivo())) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El producto " + producto.getNombre() + " ya no está disponible");
            }

            if (item.getCantidad() > producto.getStock()) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Stock insuficiente para " + producto.getNombre());
            }

            producto.setStock(producto.getStock() - item.getCantidad());

            BigDecimal precio = producto.getPrecio();
            BigDecimal linea = precio.multiply(BigDecimal.valueOf(item.getCantidad()));

            DetalleOrden detalle = new DetalleOrden();
            detalle.setIdProducto(producto.getId());
            detalle.setCodigoProducto(producto.getCodigo());
            detalle.setNombreProducto(producto.getNombre());
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(precio);
            detalle.setSubtotal(linea);
            orden.agregarDetalle(detalle);

            subtotal = subtotal.add(linea);
        }

        String cupon = normalizarCupon(datos.codigoCupon());
        BigDecimal descuento = calcularDescuento(cupon, subtotal);

        orden.setSubtotal(subtotal);
        orden.setDescuento(descuento);
        orden.setMontoTotal(subtotal.subtract(descuento));
        orden.setCodigoCupon(cupon);

        Orden guardada = ordenRepository.save(orden);

        // Vacía el carrito una vez registrada la orden.
        detalleCarritoRepository.deleteAll(items);

        return convertir(guardada);
    }

    // =========================================
    // 2. PEDIDOS DEL CLIENTE
    // =========================================
    @Transactional(readOnly = true)
    public List<OrdenResponse> listarPorUsuario(Long usuarioId) {
        return ordenRepository.findByIdUsuarioOrderByFechaOrdenDesc(usuarioId)
            .stream().map(this::convertir).toList();
    }

    // =========================================
    // 3. TODOS LOS PEDIDOS (ADMIN / VENDEDOR)
    // =========================================
    @Transactional(readOnly = true)
    public List<OrdenResponse> listarTodas() {
        return ordenRepository.findAllByOrderByFechaOrdenDesc()
            .stream().map(this::convertir).toList();
    }

    // =========================================
    // 4. CAMBIAR ESTADO (repone stock si se cancela)
    // =========================================
    @Transactional
    public OrdenResponse cambiarEstado(Long ordenId, String nuevoEstado) {

        Orden orden = ordenRepository.findWithDetallesById(ordenId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Pedido no encontrado"));

        Set<String> permitidos = TRANSICIONES.getOrDefault(orden.getEstado(), Set.of());

        if (!permitidos.contains(nuevoEstado)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "El cambio de estado no está permitido");
        }

        if ("Cancelado".equals(nuevoEstado)
                && !Boolean.TRUE.equals(orden.getStockRepuesto())) {

            for (DetalleOrden detalle : orden.getDetalles()) {
                productoRepository.findByIdParaActualizar(detalle.getIdProducto())
                    .ifPresent(p -> p.setStock(p.getStock() + detalle.getCantidad()));
            }

            orden.setStockRepuesto(true);
            orden.setFechaCancelacion(LocalDateTime.now());
        }

        orden.setEstado(nuevoEstado);

        return convertir(ordenRepository.save(orden));
    }

    // =========================================
    // AUXILIARES
    // =========================================
    private String normalizarCupon(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    private BigDecimal calcularDescuento(String cupon, BigDecimal subtotal) {

        if (cupon == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal descuento = switch (cupon) {
            case "DUOC10" -> subtotal.multiply(DUOC10_PORCENTAJE)
                .setScale(0, RoundingMode.HALF_UP);
            case "SPORTX2026" -> SPORTX2026_MONTO;
            default -> throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Cupón inválido");
        };

        return descuento.min(subtotal);
    }

    private OrdenResponse convertir(Orden o) {

        List<OrdenItemResponse> productos = o.getDetalles().stream()
            .map(d -> new OrdenItemResponse(
                d.getIdProducto(),
                d.getCodigoProducto(),
                d.getNombreProducto(),
                d.getCantidad(),
                d.getPrecioUnitario(),
                d.getSubtotal()))
            .toList();

        return new OrdenResponse(
            o.getId(),
            String.format("SX-%06d", o.getId()),
            o.getIdUsuario(),
            o.getDestinatario(),
            o.getEmailContacto(),
            o.getFechaOrden(),
            o.getEstado(),
            o.getSubtotal(),
            o.getDescuento(),
            o.getMontoTotal(),
            o.getCodigoCupon(),
            o.getDireccionEntrega(),
            o.getRegionEntrega(),
            o.getComunaEntrega(),
            productos
        );
    }
}
