package br.tec.facilitaservicos.resultados.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Configuração de segurança para testes - desabilita autenticação
 * 
 * Esta configuração permite que todos os endpoints sejam acessados
 * sem autenticação durante os testes, evitando os erros 401 no WebTestClient.
 */
@TestConfiguration
public class SegurancaTesteConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(reg -> reg.anyExchange().permitAll())
            .build();
    }
}