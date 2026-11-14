package sportx_api.pago;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PasarelaConfig {

    /**
     * sportx.webpay.ambiente:
     *  - integracion (por defecto): Transbank de prueba, credenciales públicas.
     *  - produccion: requiere código de comercio y API key reales.
     *  - simulado: sin conexión a Transbank (demostraciones y pruebas).
     */
    @Bean
    public PasarelaPago pasarelaPago(
            @Value("${sportx.webpay.ambiente:integracion}") String ambiente,
            @Value("${sportx.webpay.codigo-comercio}") String codigoComercio,
            @Value("${sportx.webpay.api-key}") String apiKey,
            @Value("${sportx.frontend-url}") String frontendUrl) {

        return switch (ambiente.trim().toLowerCase()) {
            case "simulado" -> new SimuladorPasarela(frontendUrl);
            case "produccion" -> new WebpayPlusPasarela(codigoComercio, apiKey, true);
            default -> new WebpayPlusPasarela(codigoComercio, apiKey, false);
        };
    }
}
