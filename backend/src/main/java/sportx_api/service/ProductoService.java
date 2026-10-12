package sportx_api.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sportx_api.model.Producto;
import sportx_api.dto.ProductoResponse;
import sportx_api.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productoRepository = null;

    public List<ProductoResponse> listar() {
        return productoRepository
            .findByActivoTrueAndCategoria_ActivoTrueOrderByIdAsc()
            .stream()
            .map(this::convertir)
            .toList();
    }

    public ProductoResponse buscarPorId(Long id) {
        Producto producto = productoRepository
            .findByIdAndActivoTrueAndCategoria_ActivoTrue(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Producto no encontrado"
            ));

        return convertir(producto);
    }

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