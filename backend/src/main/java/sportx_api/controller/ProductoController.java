package sportx_api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import sportx_api.dto.ProductoResponse;
import sportx_api.service.ProductoService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService = null;

    @GetMapping
    public List<ProductoResponse> listar() {
        return productoService.listar();
    }

    @GetMapping("/{id}")
    public ProductoResponse buscarPorId(@PathVariable Long id) {
        return productoService.buscarPorId(id);
    }
}
