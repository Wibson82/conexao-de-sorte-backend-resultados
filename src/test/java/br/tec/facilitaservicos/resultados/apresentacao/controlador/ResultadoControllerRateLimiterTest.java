package br.tec.facilitaservicos.resultados.apresentacao.controlador;

import br.tec.facilitaservicos.resultados.aplicacao.servico.ResultadoService;
import br.tec.facilitaservicos.resultados.apresentacao.dto.EstatisticasDto;
import br.tec.facilitaservicos.resultados.config.WebFluxTestConfig;
import io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@WebFluxTest(controllers = ResultadoController.class)
@ImportAutoConfiguration(RateLimiterAutoConfiguration.class)
@Import(WebFluxTestConfig.class)
@TestPropertySource(locations = "classpath:application-webflux-test.yml")
class ResultadoControllerRateLimiterTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ResultadoService service;

    @Test
    void rateLimiterBlocksExcessiveRequests() {
        when(service.buscarEstatisticas()).thenReturn(Mono.just(EstatisticasDto.basicas(1L, 1L)));

        for (int i = 0; i < 2; i++) {
            webTestClient.get().uri("/api/resultados/estatisticas")
                .exchange()
                .expectStatus().isOk();
        }

        webTestClient.get().uri("/api/resultados/estatisticas")
            .exchange()
            .expectStatus().isEqualTo(429);
    }
}
