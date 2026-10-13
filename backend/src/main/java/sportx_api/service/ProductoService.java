
package sportx_api.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import sportx_api.dto.ProductoRequest;
import sportx_api.dto.ProductoResponse;
import sportx_api.model.Categoria;
import sportx_api.model.Producto;
import sportx_api.repository.CategoriaRepository;
import sportx_api.repository.ProductoRepository;

@Service
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    // Constructor sin Lombok
    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository) {

        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // 1. LISTAR PRODUCTOS ACTIVOS
    public List<ProductoResponse> listar() {

        return productoRepository
            .findByActivoTrueAndCategoria_ActivoTrueOrderByIdAsc()
            .stream()
            .map(this::convertir)
            .toList();
    }

    // 2. BUSCAR PRODUCTO POR ID
    public ProductoResponse buscarPorId(Long id) {

        Producto producto = productoRepository
            .findByIdAndActivoTrueAndCategoria_ActivoTrue(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Producto no encontrado"
            ));

        return convertir(producto);
    }

    // 3. CREAR PRODUCTO
    @Transactional
    public ProductoResponse guardar(ProductoRequest datos) {

        String codigo = datos.codigo().trim();

        if (productoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ya existe un producto con ese código"
            );
        }

        Categoria categoria = buscarCategoria(datos.categoriaId());

        Producto producto = new Producto();

        producto.setCodigo(codigo);
        producto.setNombre(datos.nombre().trim());
        producto.setDescripcion(datos.descripcion());
        producto.setPrecio(datos.precio());
        producto.setStock(datos.stock());
        producto.setStockCritico(datos.stockCritico());
        producto.setImagenUrl(datos.imagenUrl());
        producto.setActivo(true);
        producto.setCategoria(categoria);

        Producto guardado = productoRepository.save(producto);

        return convertir(guardado);
    }

    // 4. ACTUALIZAR PRODUCTO
    @Transactional
    public ProductoResponse actualizar(
            Long id, ProductoRequest datos) {

        Producto producto = productoRepository
            .findByIdAndActivoTrueAndCategoria_ActivoTrue(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Producto no encontrado"
            ));

        String codigo = datos.codigo().trim();

        if (productoRepository
                .existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ya existe otro producto con ese código"
            );
        }

        Categoria categoria = buscarCategoria(datos.categoriaId());

        producto.setCodigo(codigo);
        producto.setNombre(datos.nombre().trim());
        producto.setDescripcion(datos.descripcion());
        producto.setPrecio(datos.precio());
        producto.setStock(datos.stock());
        producto.setStockCritico(datos.stockCritico());
        producto.setImagenUrl(datos.imagenUrl());
        producto.setCategoria(categoria);

        Producto actualizado = productoRepository.save(producto);

        return convertir(actualizado);
    }

    // 5. DESACTIVAR PRODUCTO
    @Transactional
    public void eliminar(Long id) {

        Producto producto = productoRepository
            .findByIdAndActivoTrueAndCategoria_ActivoTrue(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Producto no encontrado"
            ));

        producto.setActivo(false);
        productoRepository.save(producto);
    }

    // 6. VALIDAR CATEGORÍA
    private Categoria buscarCategoria(Long id) {

        return categoriaRepository
            .findByIdAndActivoTrue(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La categoría no existe o está inactiva"
            ));
    }

    // 7. CONVERTIR ENTIDAD A DTO
    private ProductoResponse convertir(Producto producto) {

        return new ProductoResponse(
            producto.getId(),
            producto.getCodigo(),
            producto.getNombre(),
            producto.getDescripcion(),
            producto.getPrecio(),
            producto.getStock(),
            producto.getStockCritico(),
            producto.getImagenUrl(),
            producto.getCategoria().getId(),
            producto.getCategoria().getNombre()
        );
    }
}
