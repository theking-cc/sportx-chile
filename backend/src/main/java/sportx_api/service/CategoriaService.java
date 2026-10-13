
package sportx_api.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import sportx_api.dto.CategoriaResponse;
import sportx_api.model.Categoria;
import sportx_api.repository.CategoriaRepository;

@Service
@Transactional(readOnly = true)
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    // Constructor sin Lombok
    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // 1. LISTAR CATEGORÍAS ACTIVAS
    public List<CategoriaResponse> listar() {

        return categoriaRepository
            .findByActivoTrueOrderByNombreAsc()
            .stream()
            .map(this::convertir)
            .toList();
    }

    // 2. BUSCAR CATEGORÍA POR ID
    public CategoriaResponse buscarPorId(Long id) {

        Categoria categoria = categoriaRepository
            .findByIdAndActivoTrue(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Categoría no encontrada"
            ));

        return convertir(categoria);
    }

    // 3. CREAR CATEGORÍA
    @Transactional
    public CategoriaResponse guardar(Categoria datos) {

        validarNombre(datos.getNombre());

        String nombre = datos.getNombre().trim();

        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ya existe una categoría con ese nombre"
            );
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion(datos.getDescripcion());
        categoria.setActivo(true);

        return convertir(categoriaRepository.save(categoria));
    }

    // 4. ACTUALIZAR CATEGORÍA
    @Transactional
    public CategoriaResponse actualizar(Long id, Categoria datos) {

        Categoria categoria = categoriaRepository
            .findByIdAndActivoTrue(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Categoría no encontrada"
            ));

        validarNombre(datos.getNombre());

        String nombre = datos.getNombre().trim();

        if (categoriaRepository
                .existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ya existe otra categoría con ese nombre"
            );
        }

        categoria.setNombre(nombre);
        categoria.setDescripcion(datos.getDescripcion());

        return convertir(categoriaRepository.save(categoria));
    }

    // 5. DESACTIVAR CATEGORÍA
    @Transactional
    public void eliminar(Long id) {

        Categoria categoria = categoriaRepository
            .findByIdAndActivoTrue(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Categoría no encontrada"
            ));

        categoria.setActivo(false);
        categoriaRepository.save(categoria);
    }

    // VALIDACIÓN DEL NOMBRE
    private void validarNombre(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El nombre de la categoría es obligatorio"
            );
        }

        if (nombre.trim().length() > 100) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El nombre no puede superar 100 caracteres"
            );
        }
    }

    // CONVERTIR ENTIDAD A DTO
    private CategoriaResponse convertir(Categoria categoria) {

        return new CategoriaResponse(
            categoria.getId(),
            categoria.getNombre(),
            categoria.getDescripcion(),
            categoria.getActivo()
        );
    }
}
