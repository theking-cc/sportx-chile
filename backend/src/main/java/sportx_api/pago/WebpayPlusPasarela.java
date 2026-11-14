package sportx_api.pago;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import cl.transbank.webpay.webpayplus.WebpayPlus;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCommitResponse;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCreateResponse;

/** Transbank Webpay Plus con el SDK oficial (transbank-sdk-java 6.x). */
public class WebpayPlusPasarela implements PasarelaPago {

    private final WebpayPlus.Transaction tx;

    public WebpayPlusPasarela(String codigoComercio, String apiKey, boolean produccion) {
        this.tx = produccion
            ? WebpayPlus.Transaction.buildForProduction(codigoComercio, apiKey)
            : WebpayPlus.Transaction.buildForIntegration(codigoComercio, apiKey);
    }

    @Override
    public Inicio crear(String ordenCompra, String sesion, BigDecimal monto, String urlRetorno) {
        try {
            WebpayPlusTransactionCreateResponse r =
                tx.create(ordenCompra, sesion, monto.doubleValue(), urlRetorno);
            return new Inicio(r.getUrl(), r.getToken());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "No se pudo iniciar el pago con Webpay. Intenta nuevamente.", ex);
        }
    }

    @Override
    public Resultado confirmar(String token) {
        try {
            WebpayPlusTransactionCommitResponse r = tx.commit(token);
            int codigo = r.getResponseCode();
            boolean aprobado = codigo == 0 && "AUTHORIZED".equalsIgnoreCase(r.getStatus());
            String digitos = r.getCardDetail() == null ? null : r.getCardDetail().getCardNumber();
            return new Resultado(
                aprobado,
                codigo,
                r.getStatus(),
                r.getBuyOrder(),
                BigDecimal.valueOf(r.getAmount()),
                r.getAuthorizationCode(),
                r.getPaymentTypeCode(),
                (int) r.getInstallmentsNumber(),
                digitos);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "No se pudo confirmar el pago con Webpay.", ex);
        }
    }
}
