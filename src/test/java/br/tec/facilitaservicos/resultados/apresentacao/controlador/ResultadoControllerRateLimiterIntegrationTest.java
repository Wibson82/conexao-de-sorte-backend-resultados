package br.tec.facilitaservicos.resultados.apresentacao.controlador;

import br.tec.facilitaservicos.resultados.aplicacao.servico.ResultadoService;
import br.tec.facilitaservicos.resultados.apresentacao.dto.EstatisticasDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

/**
 * Teste de integração alternativo para Rate Limiting
 * 
 * Esta versão usa @SpringBootTest para carregamento completo do contexto,
 * evitando problemas com @WebFluxTest e dependências R2DBC.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "resilience4j.ratelimiter.instances.resultados-service.limit-for-period=2",
        "resilience4j.ratelimiter.instances.resultados-service.limit-refresh-period=10s",
        "resilience4j.ratelimiter.instances.resultados-service.timeout-duration=0",
        // Configurações de teste para evitar dependências externas
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.r2dbc.url=r2dbc:h2:mem:///testdb",
        "spring.flyway.enabled=false",
        "spring.cloud.azure.keyvault.secret.enabled=false"
})
class ResultadoControllerRateLimiterIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ResultadoService service;

    @Test
    void rateLimiterBlocksExcessiveRequestsIntegration() {
        when(service.buscarEstatisticas()).thenReturn(Mono.just(EstatisticasDto.basicas(1L, 1L)));

        // Fazer 2 requests permitidos
        for (int i = 0; i < 2; i++) {
            webTestClient.get().uri("/api/resultados/estatisticas")
                .exchange()
                .expectStatus().isOk();
        }

        // O 3º request deve ser bloqueado (429 - Too Many Requests)
        webTestClient.get().uri("/api/resultados/estatisticas")
            .exchange()
            .expectStatus().isEqualTo(429);
    }
}