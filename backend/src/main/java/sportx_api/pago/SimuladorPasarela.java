package sportx_api.pago;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simulador local de Webpay Plus (sportx.webpay.ambiente=simulado).
 * Sirve para demostrar y probar el flujo sin conexión a Transbank: la "página
 * de pago" la entrega el propio backend en /api/pagos/simulador.
 */
public class SimuladorPasarela implements PasarelaPago {

    private final String urlBase;
    private final Map<String, Pendiente> transacciones = new ConcurrentHashMap<>();

    public SimuladorPasarela(String urlBase) {
        this.urlBase = urlBase;
    }

    public Pendiente buscar(String token) {
        return transacciones.get(token);
    }

    /** El usuario eligió "aprobar" o "rechazar" en la página simulada. */
    public void decidir(String token, boolean aprobar) {
        Pendiente p = transacciones.get(token);
        if (p != null) {
            transacciones.put(token, new Pendiente(p.ordenCompra(), p.monto(), p.urlRetorno(), aprobar));
        }
    }

    @Override
    public Inicio crear(String ordenCompra, String sesion, BigDecimal monto, String urlRetorno) {
        String token = "sim" + UUID.randomUUID().toString().replace("-", "");
        transacciones.put(token, new Pendiente(ordenCompra, monto, urlRetorno, null));
        return new Inicio(urlBase + "/api/pagos/simulador", token);
    }

    @Override
    public Resultado confirmar(String token) {
        Pendiente p = transacciones.remove(token);
        if (p == null || p.aprobar() == null) {
            return new Resultado(false, -1, "FAILED", p == null ? null : p.ordenCompra(),
                p == null ? null : p.monto(), null, null, null, null);
        }
        return p.aprobar()
            ? new Resultado(true, 0, "AUTHORIZED", p.ordenCompra(), p.monto(),
                "1213", "VD", 0, "6623")
            : new Resultado(false, -1, "FAILED", p.ordenCompra(), p.monto(),
                null, "VD", 0, "6623");
    }

    public record Pendiente(String ordenCompra, BigDecimal monto, String urlRetorno, Boolean aprobar) {
    }
}
