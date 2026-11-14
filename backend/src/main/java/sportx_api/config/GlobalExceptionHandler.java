package sportx_api.config;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

// Devuelve los errores como {"message": "..."} para que el frontend los muestre.
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> estado(ResponseStatusException ex) {

        String mensaje = ex.getReason() != null
            ? ex.getReason()
            : "No se pudo completar la solicitud";

        return ResponseEntity
            .status(ex.getStatusCode())
            .body(Map.of("message", mensaje));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacion(
            MethodArgumentNotValidException ex) {

        String mensaje = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(e -> e.getDefaultMessage())
            .orElse("Datos inválidos");

        return ResponseEntity
            .badRequest()
            .body(Map.of("message", mensaje));
    }
}
