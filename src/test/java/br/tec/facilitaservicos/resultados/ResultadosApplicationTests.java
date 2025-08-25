package br.tec.facilitaservicos.resultados;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryMetadata;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.r2dbc.url=r2dbc:h2:mem:///testdb?options=DB_CLOSE_DELAY=-1",
        "spring.r2dbc.username=sa",
        "spring.r2dbc.password=",
        "spring.flyway.enabled=false",
        "features.statistics-cache=false",
        "app.cors.allowed-origins=http://localhost:3000",
        "management.endpoints.web.exposure.include=health,info"
    }
)
@ActiveProfiles("test")
class ResultadosApplicationTests {

    @Test
    void contextLoads() {
        // Teste básico para verificar se o contexto da aplicação carrega corretamente
        // Este teste verifica apenas se a aplicação inicializa sem erros, 
        // não testa integração com outros microserviços
    }
}