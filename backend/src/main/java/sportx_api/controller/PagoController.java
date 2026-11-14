package sportx_api.controller;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import sportx_api.dto.CrearOrdenRequest;
import sportx_api.dto.IniciarPagoResponse;
import sportx_api.pago.PasarelaPago;
import sportx_api.pago.SimuladorPasarela;
import sportx_api.service.PagoService;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;
    private final PasarelaPago pasarela;

    public PagoController(PagoService pagoService, PasarelaPago pasarela) {
        this.pagoService = pagoService;
        this.pasarela = pasarela;
    }

    // CLIENTE: crea la orden desde su carrito y abre la transacción Webpay
    @PostMapping("/webpay/iniciar")
    public ResponseEntity<IniciarPagoResponse> iniciar(
            JwtAuthenticationToken auth,
            @Valid @RequestBody CrearOrdenRequest datos) {

        Long usuarioId = Long.valueOf(auth.getToken().getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.iniciar(usuarioId, datos));
    }

    // PÚBLICO: Transbank devuelve aquí al comprador (GET o POST, según el caso)
    @RequestMapping(path = "/webpay/retorno", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Void> retorno(
            @RequestParam(name = "token_ws", required = false) String tokenWs,
            @RequestParam(name = "TBK_TOKEN", required = false) String tbkToken,
            @RequestParam(name = "TBK_ORDEN_COMPRA", required = false) String tbkOrdenCompra) {

        String destino = pagoService.retorno(tokenWs, tbkToken, tbkOrdenCompra);
        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create(destino)).build();
    }

    // Solo en ambiente "simulado": página que imita el formulario de Webpay.
    @RequestMapping(path = "/simulador", method = {RequestMethod.GET, RequestMethod.POST},
            produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> simulador(
            @RequestParam(name = "token_ws", required = false) String token,
            @RequestParam(name = "decision", required = false) String decision) {

        if (!(pasarela instanceof SimuladorPasarela sim) || token == null || sim.buscar(token) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("<p>Transacción simulada no encontrada.</p>");
        }
        SimuladorPasarela.Pendiente p = sim.buscar(token);
        String t = HtmlUtils.htmlEscape(token);

        if (decision != null) {
            String retorno = p.urlRetorno();
            if ("anular".equals(decision)) {
                return redirigir(retorno + "?TBK_TOKEN=" + t + "&TBK_ORDEN_COMPRA=" + p.ordenCompra());
            }
            sim.decidir(token, "aprobar".equals(decision));
            return redirigir(retorno + "?token_ws=" + t);
        }

        String html = """
            <!doctype html><html lang="es"><head><meta charset="utf-8">
            <meta name="viewport" content="width=device-width,initial-scale=1">
            <title>Webpay simulado</title><link rel="icon" href="data:,">
            <style>body{font-family:system-ui,sans-serif;max-width:420px;margin:48px auto;padding:0 16px;color:#14213D}
            .c{border:1px solid #d7dbe3;border-radius:12px;padding:24px}button{width:100%%;padding:12px;margin-top:10px;border-radius:8px;border:1px solid #14213D;font-size:16px;cursor:pointer;background:#fff}
            .ok{background:#14213D;color:#fff}.small{color:#5b6475;font-size:14px}</style></head><body>
            <div class="c"><h1 style="font-size:22px">Webpay (simulado)</h1>
            <p class="small">Ambiente de demostración: no se realiza ningún cobro.</p>
            <p>Orden de compra <b>%s</b><br>Monto <b>$%s</b></p>
            <form method="post"><input type="hidden" name="token_ws" value="%s">
            <button class="ok" name="decision" value="aprobar">Aprobar pago</button>
            <button name="decision" value="rechazar">Rechazar pago</button>
            <button name="decision" value="anular">Anular y volver al comercio</button></form></div></body></html>
            """.formatted(HtmlUtils.htmlEscape(p.ordenCompra()),
                String.format("%,d", p.monto().longValue()).replace(',', '.'), t);
        return ResponseEntity.ok(html);
    }

    private ResponseEntity<String> redirigir(String url) {
        HttpHeaders h = new HttpHeaders();
        h.setLocation(URI.create(url));
        return new ResponseEntity<>(h, HttpStatus.SEE_OTHER);
    }
}
