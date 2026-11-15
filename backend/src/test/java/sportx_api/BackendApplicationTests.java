package sportx_api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba de integración: levanta el contexto completo contra MariaDB.
 * Solo se ejecuta con la base disponible: SPORTX_DB_TESTS=true mvnw test
 * (las pruebas unitarias y de seguridad no necesitan base de datos).
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "SPORTX_DB_TESTS", matches = "true")
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
