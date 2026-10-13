
package sportx_api.dto;

public record CategoriaResponse(
    Long id,
    String nombre,
    String descripcion,
    Boolean activo
) {
}
