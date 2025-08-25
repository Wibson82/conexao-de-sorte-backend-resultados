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

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=" +
        "org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration," +
        "org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration," +
        "org.springframework.boot.autoconfigure.data.r2dbc.R2dbcRepositoriesAutoConfiguration," +
        "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration," +
        "org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration," +
        "org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration," +
        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
        "org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration",
    "features.statistics-cache=false"
})
@ActiveProfiles("test")
class ResultadosApplicationTests {

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityWebFilterChain testSpringSecurityFilterChain() {
            ServerHttpSecurity http = ServerHttpSecurity.http();
            return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .build();
        }
    }

    @TestConfiguration
    static class TestDataConfig {
        @Bean
        ConnectionFactory connectionFactory() {
            ConnectionFactory cf = mock(ConnectionFactory.class);
            ConnectionFactoryMetadata meta = new ConnectionFactoryMetadata() {
                @Override
                public String getName() { return "H2"; }
            };
            when(cf.getMetadata()).thenReturn(meta);
            return cf;
        }

        @Bean
        R2dbcMappingContext r2dbcMappingContext() {
            return new R2dbcMappingContext();
        }
    }

    @Test
    void contextLoads() {
        // Teste básico para verificar se o contexto da aplicação carrega corretamente
    }
}