package br.tec.facilitaservicos.resultados.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext;
import org.springframework.data.relational.core.mapping.NamingStrategy;

/**
 * Configuração de teste para resolver dependências R2DBC em testes @WebFluxTest
 * 
 * Esta configuração fornece beans mockados para evitar que o Spring tente
 * carregar o contexto completo do R2DBC durante testes de slice web.
 */
@TestConfiguration
public class WebFluxTestConfig {

    /**
     * Bean mock do R2dbcMappingContext para evitar erro:
     * "No bean named 'r2dbcMappingContext' available"
     */
    @Bean
    @Primary
    public R2dbcMappingContext r2dbcMappingContext() {
        return new R2dbcMappingContext(NamingStrategy.INSTANCE);
    }
}