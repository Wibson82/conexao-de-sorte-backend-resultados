package br.tec.facilitaservicos.resultados.apresentacao.controlador;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import br.tec.facilitaservicos.resultados.apresentacao.dto.EstatisticasDto;
import br.tec.facilitaservicos.resultados.apresentacao.dto.PaginacaoDto;
import br.tec.facilitaservicos.resultados.apresentacao.dto.RankingDto;
import br.tec.facilitaservicos.resultados.apresentacao.dto.ResultadoDto;
import br.tec.facilitaservicos.resultados.aplicacao.servico.ResultadoService;
import br.tec.facilitaservicos.resultados.config.WebFluxTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = ResultadoController.class)
@AutoConfigureWebTestClient
@Import({WebFluxTestConfig.class, ResultadoControllerContractTest.TestConfig.class})
class ResultadoControllerContractTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ResultadoService resultadoService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public ResultadoService resultadoService() {
            return Mockito.mock(ResultadoService.class);
        }
    }

    @Test
    @DisplayName("GET /rest/v1/resultados deve retornar 200 com paginação")
    void deveListarResultadosPaginados() {
        var dto = ResultadoDto.completo(
                1L,
                "14:00",
                List.of("01","02","03","04","05","06","07"),
                "28",
                LocalDate.now(),
                LocalDateTime.now(),
                LocalDateTime.now());
        var paginacao = PaginacaoDto.criar(List.of(dto), 0, 20, 1);

        given(resultadoService.buscarResultados(anyInt(), anyInt(), any(), any()))
                .willReturn(Mono.just(paginacao));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/rest/v1/resultados").queryParam("pagina", 0).queryParam("tamanho", 20).build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.conteudo[0].horario").isEqualTo("14:00")
                .jsonPath("$.pagina").isEqualTo(0)
                .jsonPath("$.tamanho").isEqualTo(20)
                .jsonPath("$.total").isEqualTo(1);
    }

    @Test
    @DisplayName("GET /rest/v1/resultados/{id} deve retornar 200 quando encontrado")
    void deveBuscarResultadoPorId() {
        var dto = ResultadoDto.criar("10:00", List.of("01","02","03","04","05","06","07"), LocalDate.now());
        given(resultadoService.buscarPorId(eq(123L))).willReturn(Mono.just(dto));

        webTestClient.get()
                .uri("/rest/v1/resultados/{id}", 123L)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.horario").isEqualTo("10:00")
                .jsonPath("$.numeros.length()").isEqualTo(7);
    }

    @Test
    @DisplayName("GET /rest/v1/resultados/{id} deve retornar 404 quando não encontrado")
    void deveRetornar404QuandoIdNaoEncontrado() {
        given(resultadoService.buscarPorId(eq(999L))).willReturn(Mono.empty());

        webTestClient.get()
                .uri("/rest/v1/resultados/{id}", 999L)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("GET /rest/v1/resultados/ranking deve retornar 200 com itens")
    void deveListarRanking() {
        var r1 = RankingDto.completo("07", 10L, 50.0, 1);
        var r2 = RankingDto.completo("13", 8L, 40.0, 2);
        given(resultadoService.buscarRanking(any(), any())).willReturn(Flux.just(r1, r2));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/rest/v1/resultados/ranking").queryParam("limite", 2).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].numero").isEqualTo("07")
                .jsonPath("$[1].numero").isEqualTo("13");
    }

    @Test
    @DisplayName("GET /rest/v1/resultados/estatisticas deve retornar 200 com métricas")
    void deveRetornarEstatisticas() {
        var est = EstatisticasDto.basicas(100L, 700L);
        given(resultadoService.buscarEstatisticas()).willReturn(Mono.just(est));

        webTestClient.get()
                .uri("/rest/v1/resultados/estatisticas")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalResultados").isEqualTo(100)
                .jsonPath("$.totalSorteios").isEqualTo(700);
    }

    @Test
    @DisplayName("GET /rest/v1/resultados/hoje deve retornar 204 quando vazio")
    void deveRetornarNoContentParaHojeQuandoVazio() {
        given(resultadoService.buscarResultadosHoje(anyInt(), anyInt()))
                .willReturn(Mono.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/rest/v1/resultados/hoje").queryParam("pagina", 0).queryParam("tamanho", 20).build())
                .exchange()
                .expectStatus().isNoContent();
    }
}
