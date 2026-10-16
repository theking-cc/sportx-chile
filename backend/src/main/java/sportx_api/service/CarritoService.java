
package sportx_api.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import sportx_api.dto.AgregarCarritoRequest;
import sportx_api.dto.CarritoItemResponse;
import sportx_api.dto.CarritoResponse;
import sportx_api.model.Carrito;
import sportx_api.model.DetalleCarrito;
import sportx_api.model.Producto;
import sportx_api.repository.CarritoRepository;
import sportx_api.repository.DetalleCarritoRepository;
import sportx_api.repository.ProductoRepository;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final DetalleCarritoRepository detalleRepository;
    private final ProductoRepository productoRepository;

    public CarritoService(
            CarritoRepository carritoRepository,
            DetalleCarritoRepository detalleRepository,
            ProductoRepository productoRepository) {

        this.carritoRepository = carritoRepository;
        this.detalleRepository = detalleRepository;
        this.productoRepository = productoRepository;
    }

    // ==========================================
    // OBTENER CARRITO DEL CLIENTE
    // ==========================================

    @Transactional
    public CarritoResponse obtener(Long usuarioId) {

        System.out.println("=== INICIO CARRITO ===");
        System.out.println("Usuario ID: " + usuarioId);

        Carrito carrito = obtenerOCrear(usuarioId);

        System.out.println(
            "Carrito encontrado: " + carrito.getId()
        );

        CarritoResponse respuesta = convertir(carrito);

        System.out.println("Conversión completada");
        System.out.println("=== FIN CARRITO ===");

        return respuesta;
    }

    // ==========================================
    // AGREGAR PRODUCTOS AL CARRITO
    // ==========================================

    @Transactional
    public CarritoResponse agregar(
            Long usuarioId,
            AgregarCarritoRequest datos) {

        if (datos == null
                || datos.idProducto() == null
                || datos.cantidad() == null
                || datos.cantidad() < 1) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Producto o cantidad inválida"
            );
        }

        Carrito carrito = obtenerOCrear(usuarioId);

        Producto producto = buscarProducto(
            datos.idProducto()
        );

        DetalleCarrito detalle = detalleRepository
            .findByIdCarritoAndIdProducto(
                carrito.getId(),
                producto.getId()
            )
            .orElse(null);

        int cantidadActual = detalle == null
            ? 0
            : detalle.getCantidad();

        long nuevaCantidad =
            (long) cantidadActual + datos.cantidad();

        validarStock(producto, nuevaCantidad);

        if (detalle == null) {

            detalle = new DetalleCarrito();

            detalle.setIdCarrito(carrito.getId());
            detalle.setIdProducto(producto.getId());
        }

        detalle.setCantidad((int) nuevaCantidad);

        detalleRepository.saveAndFlush(detalle);

        return convertir(carrito);
    }

    // ==========================================
    // ACTUALIZAR CANTIDAD
    // ==========================================

    @Transactional
    public CarritoResponse actualizar(
            Long usuarioId,
            Long detalleId,
            Integer cantidad) {

        if (cantidad == null || cantidad < 1) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La cantidad debe ser mayor que cero"
            );
        }

        Carrito carrito = obtenerExistente(usuarioId);

        DetalleCarrito detalle = detalleRepository
            .findByIdAndIdCarrito(
                detalleId,
                carrito.getId()
            )
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Producto no encontrado en el carrito"
                )
            );

        Producto producto = buscarProducto(
            detalle.getIdProducto()
        );

        validarStock(producto, cantidad);

        detalle.setCantidad(cantidad);

        detalleRepository.saveAndFlush(detalle);

        return convertir(carrito);
    }

    // ==========================================
    // ELIMINAR PRODUCTO DEL CARRITO
    // ==========================================

    @Transactional
    public CarritoResponse eliminar(
            Long usuarioId,
            Long detalleId) {

        Carrito carrito = obtenerExistente(usuarioId);

        DetalleCarrito detalle = detalleRepository
            .findByIdAndIdCarrito(
                detalleId,
                carrito.getId()
            )
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Producto no encontrado en el carrito"
                )
            );

        detalleRepository.delete(detalle);

        detalleRepository.flush();

        return convertir(carrito);
    }

    // ==========================================
    // OBTENER O CREAR CARRITO
    // ==========================================

    private Carrito obtenerOCrear(Long usuarioId) {

        if (usuarioId == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Usuario no identificado"
            );
        }

        System.out.println(
            "Buscando carrito del usuario: " + usuarioId
        );

        return carritoRepository
            .findByIdUsuario(usuarioId)
            .orElseGet(() -> {

                System.out.println(
                    "Creando carrito para usuario: " + usuarioId
                );

                Carrito carrito = new Carrito();

                carrito.setIdUsuario(usuarioId);

                return carritoRepository.saveAndFlush(
                    carrito
                );
            });
    }

    // ==========================================
    // OBTENER CARRITO EXISTENTE
    // ==========================================

    private Carrito obtenerExistente(Long usuarioId) {

        return carritoRepository
            .findByIdUsuario(usuarioId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El carrito no existe"
                )
            );
    }

    // ==========================================
    // BUSCAR PRODUCTO ACTIVO
    // ==========================================

    private Producto buscarProducto(Long productoId) {

        Producto producto = productoRepository
            .findById(productoId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Producto no encontrado"
                )
            );

        if (!Boolean.TRUE.equals(producto.getActivo())
                || producto.getCategoria() == null
                || !Boolean.TRUE.equals(
                    producto.getCategoria().getActivo()
                )) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Producto no disponible"
            );
        }

        return producto;
    }

    // ==========================================
    // VALIDAR STOCK
    // ==========================================

    private void validarStock(
            Producto producto,
            long cantidad) {

        if (producto.getStock() == null
                || cantidad > producto.getStock()
                || cantidad > Integer.MAX_VALUE) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Stock insuficiente"
            );
        }
    }

    // ==========================================
    // CONVERTIR CARRITO A DTO
    // ==========================================

    private CarritoResponse convertir(Carrito carrito) {

        System.out.println(
            "Consultando detalles del carrito: "
                + carrito.getId()
        );

        List<DetalleCarrito> detalles =
            detalleRepository
                .findByIdCarritoOrderByIdAsc(
                    carrito.getId()
                );

        System.out.println(
            "Cantidad de registros encontrados: "
                + detalles.size()
        );

        List<CarritoItemResponse> items = detalles.stream()
            .map(detalle -> {

                Producto producto = buscarProducto(
                    detalle.getIdProducto()
                );

                BigDecimal subtotal =
                    producto.getPrecio().multiply(
                        BigDecimal.valueOf(
                            detalle.getCantidad()
                        )
                    );

                return new CarritoItemResponse(
                    detalle.getId(),
                    producto.getId(),
                    producto.getCodigo(),
                    producto.getNombre(),
                    producto.getPrecio(),
                    detalle.getCantidad(),
                    subtotal
                );
            })
            .toList();

        BigDecimal total = items.stream()
            .map(CarritoItemResponse::subtotal)
            .reduce(
                BigDecimal.ZERO,
                BigDecimal::add
            );

        System.out.println(
            "Total calculado: " + total
        );

        return new CarritoResponse(
            carrito.getId(),
            carrito.getIdUsuario(),
            items,
            total
        );
    }
}
