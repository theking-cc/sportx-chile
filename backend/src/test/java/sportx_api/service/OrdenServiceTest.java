package sportx_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import sportx_api.dto.ComunaResponse;
import sportx_api.dto.CrearOrdenRequest;
import sportx_api.model.Carrito;
import sportx_api.model.Categoria;
import sportx_api.model.DetalleCarrito;
import sportx_api.model.DetalleOrden;
import sportx_api.model.Orden;
import sportx_api.model.Pago;
import sportx_api.model.Producto;
import sportx_api.model.Usuario;
import sportx_api.repository.CarritoRepository;
import sportx_api.repository.DetalleCarritoRepository;
import sportx_api.repository.OrdenRepository;
import sportx_api.repository.PagoRepository;
import sportx_api.repository.ProductoRepository;
import sportx_api.repository.UsuarioRepository;

/** Lógica de órdenes (BOLETA): reserva de stock, cupones y estados. Sin base de datos. */
@ExtendWith(MockitoExtension.class)
class OrdenServiceTest {

    @Mock OrdenRepository ordenRepository;
    @Mock CarritoRepository carritoRepository;
    @Mock DetalleCarritoRepository detalleCarritoRepository;
    @Mock ProductoRepository productoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock UbicacionService ubicacionService;
    @Mock PagoRepository pagoRepository;

    OrdenService service;
    Producto zapatillas;

    @BeforeEach
    void preparar() {
        service = new OrdenService(ordenRepository, carritoRepository, detalleCarritoRepository,
            productoRepository, usuarioRepository, ubicacionService, pagoRepository);

        Categoria calzado = new Categoria();
        calzado.setActivo(true);
        zapatillas = new Producto();
        zapatillas.setId(1L);
        zapatillas.setCodigo("RUN-001");
        zapatillas.setNombre("Zapatillas Running Pro");
        zapatillas.setPrecio(new BigDecimal("79990"));
        zapatillas.setStock(15);
        zapatillas.setActivo(true);
        zapatillas.setCategoria(calzado);
    }

    /** Cliente 5 con N unidades de las zapatillas en su carrito. */
    private void carritoCon(int cantidad) {
        Usuario u = new Usuario();
        u.setNombre("Ana");
        u.setApellidos("Pérez");
        u.setEmail("ana@gmail.com");
        Carrito carrito = new Carrito();
        carrito.setId(1L);
        DetalleCarrito item = new DetalleCarrito();
        item.setIdCarrito(1L);
        item.setIdProducto(1L);
        item.setCantidad(cantidad);
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(u));
        when(carritoRepository.findByIdUsuario(5L)).thenReturn(Optional.of(carrito));
        when(detalleCarritoRepository.findByIdCarritoOrderByIdAsc(1L)).thenReturn(List.of(item));
        when(ubicacionService.buscarComuna(1L))
            .thenReturn(new ComunaResponse(1L, 1L, "Región Metropolitana de Santiago", "Santiago"));
        when(productoRepository.findByIdParaActualizar(1L)).thenReturn(Optional.of(zapatillas));
        lenient().when(ordenRepository.save(any(Orden.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    @DisplayName("Reserva: descuenta stock y calcula el cupón DUOC10 (10 %)")
    void reservaConCuponDuoc() {
        carritoCon(2);

        Orden orden = service.reservar(5L, new CrearOrdenRequest("Av. Siempre Viva 742", 1L, "duoc10"));

        assertThat(zapatillas.getStock()).isEqualTo(13);
        assertThat(orden.getSubtotal()).isEqualByComparingTo("159980");
        assertThat(orden.getDescuento()).isEqualByComparingTo("15998");
        assertThat(orden.getMontoTotal()).isEqualByComparingTo("143982");
        assertThat(orden.getCodigoCupon()).isEqualTo("DUOC10");
        assertThat(orden.getEstado()).isEqualTo("Pendiente");
        assertThat(orden.getDetalles()).hasSize(1);
        assertThat(orden.getComunaEntrega()).isEqualTo("Santiago");
    }

    @Test
    @DisplayName("Cupón SPORTX2026 descuenta $5.000 fijos")
    void cuponMontoFijo() {
        carritoCon(1);

        Orden orden = service.reservar(5L, new CrearOrdenRequest("Calle 1", 1L, "SPORTX2026"));

        assertThat(orden.getDescuento()).isEqualByComparingTo("5000");
        assertThat(orden.getMontoTotal()).isEqualByComparingTo("74990");
    }

    @Test
    @DisplayName("Cupón inválido: 400 y no se guarda la orden")
    void cuponInvalido() {
        carritoCon(1);

        assertThatThrownBy(() -> service.reservar(5L, new CrearOrdenRequest("Calle 1", 1L, "FALSO")))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST));
        verify(ordenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Stock insuficiente: 409 y el stock no cambia")
    void stockInsuficiente() {
        carritoCon(20);

        assertThatThrownBy(() -> service.reservar(5L, new CrearOrdenRequest("Calle 1", 1L, null)))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Stock insuficiente");
        assertThat(zapatillas.getStock()).isEqualTo(15);
    }

    @Test
    @DisplayName("Carrito vacío: 400")
    void carritoVacio() {
        Carrito carrito = new Carrito();
        carrito.setId(1L);
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(new Usuario()));
        when(carritoRepository.findByIdUsuario(5L)).thenReturn(Optional.of(carrito));
        when(detalleCarritoRepository.findByIdCarritoOrderByIdAsc(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.reservar(5L, new CrearOrdenRequest("Calle 1", 1L, null)))
            .hasMessageContaining("vacío");
    }

    /** Orden 10 en estado dado, con 2 zapatillas. */
    private Orden orden(String estado) {
        Orden o = new Orden();
        ReflectionTestUtils.setField(o, "id", 10L);
        o.setEstado(estado);
        o.setMontoTotal(new BigDecimal("159980"));
        DetalleOrden d = new DetalleOrden();
        d.setIdProducto(1L);
        d.setCantidad(2);
        o.agregarDetalle(d);
        when(ordenRepository.findWithDetallesById(10L)).thenReturn(Optional.of(o));
        lenient().when(ordenRepository.save(any(Orden.class))).thenAnswer(i -> i.getArgument(0));
        return o;
    }

    @Test
    @DisplayName("Transición no permitida (Pendiente → Entregado): 400")
    void transicionInvalida() {
        orden("Pendiente");

        assertThatThrownBy(() -> service.cambiarEstado(10L, "Entregado"))
            .hasMessageContaining("no está permitido");
    }

    @Test
    @DisplayName("No se prepara un pedido cuyo pago no está aprobado: 409")
    void prepararSinPago() {
        orden("Pendiente");
        Pago pago = new Pago();
        pago.setEstado("INICIADO");
        when(pagoRepository.findFirstByIdOrdenOrderByIdDesc(10L)).thenReturn(Optional.of(pago));

        assertThatThrownBy(() -> service.cambiarEstado(10L, "Preparando"))
            .hasMessageContaining("pago aprobado");
    }

    @Test
    @DisplayName("Pedido con pago aprobado pasa a Preparando")
    void prepararPagado() {
        Orden o = orden("Pendiente");
        Pago pago = new Pago();
        pago.setEstado("APROBADO");
        when(pagoRepository.findFirstByIdOrdenOrderByIdDesc(10L)).thenReturn(Optional.of(pago));

        assertThat(service.cambiarEstado(10L, "Preparando").estado()).isEqualTo("Preparando");
        assertThat(o.getEstado()).isEqualTo("Preparando");
    }

    @Test
    @DisplayName("Cancelar repone el stock una sola vez")
    void cancelarReponeStock() {
        Orden o = orden("Pendiente");
        zapatillas.setStock(13);
        when(productoRepository.findByIdParaActualizar(1L)).thenReturn(Optional.of(zapatillas));

        service.cambiarEstado(10L, "Cancelado");

        assertThat(zapatillas.getStock()).isEqualTo(15);
        assertThat(o.getStockRepuesto()).isTrue();
    }

    @Test
    @DisplayName("Pago fallido: cancela la orden Pendiente y repone stock")
    void pagoFallido() {
        Orden o = orden("Pendiente");
        zapatillas.setStock(13);
        when(productoRepository.findByIdParaActualizar(1L)).thenReturn(Optional.of(zapatillas));

        service.cancelarPorPagoFallido(10L);

        assertThat(o.getEstado()).isEqualTo("Cancelado");
        assertThat(zapatillas.getStock()).isEqualTo(15);
    }
}
