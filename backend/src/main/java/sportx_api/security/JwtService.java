
package sportx_api.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import sportx_api.model.Usuario;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long expirationMinutes;

    // CONSTRUCTOR SIN LOMBOK
    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${sportx.jwt.expiration-minutes:60}")
            long expirationMinutes) {

        this.jwtEncoder = jwtEncoder;
        this.expirationMinutes = expirationMinutes;
    }

    // GENERAR TOKEN JWT
    public String generarToken(Usuario usuario) {

        Instant ahora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("sportx-chile")
            .issuedAt(ahora)
            .expiresAt(
                ahora.plus(
                    expirationMinutes,
                    ChronoUnit.MINUTES
                )
            )
            .subject(usuario.getId().toString())
            .claim("email", usuario.getEmail())
            .claim("rol", usuario.getRol().getNombre())
            .build();

        JwsHeader header = JwsHeader
            .with(MacAlgorithm.HS256)
            .build();

        return jwtEncoder.encode(
            JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}
