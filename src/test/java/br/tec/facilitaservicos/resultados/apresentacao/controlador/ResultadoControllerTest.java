package br.tec.facilitaservicos.resultados.apresentacao.controlador;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import br.tec.facilitaservicos.resultados.aplicacao.servico.ResultadoService;
import br.tec.facilitaservicos.resultados.apresentacao.dto.EstatisticasDto;
import br.tec.facilitaservicos.resultados.apresentacao.dto.PaginacaoDto;
import br.tec.facilitaservicos.resultados.apresentacao.dto.RankingDto;
import br.tec.facilitaservicos.resultados.apresentacao.dto.ResultadoDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.springframework.context.annotation.Import;

/**
 * Testes de integração para ResultadoController
 * 
 * @author Sistema de Migração R2DBC
 * @version 1.0
 * @since 2024
 */
@WebFluxTest(controllers = ResultadoController.class)
@ActiveProfiles("test")
@Import(ResultadoController.class)
class ResultadoControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ResultadoService service;

    private ResultadoDto resultadoDto;
    private PaginacaoDto<ResultadoDto> paginacaoDto;

    @BeforeEach
    void setUp() {
        resultadoDto = ResultadoDto.criar(
            "14:00",
            List.of("01", "15", "23", "34", "45", "56", "67"),
            LocalDate.now()
        );

        paginacaoDto = PaginacaoDto.criar(List.of(resultadoDto), 0, 20, 1);
    }

    @Test
    void buscarResultados_DeveRetornarPaginacao() {
        // Given
        when(service.buscarResultados(anyInt(), anyInt(), anyString(), any()))
            .thenReturn(Mono.just(paginacaoDto));

        // When & Then
        webTestClient.get()
            .uri("/api/resultados?pagina=0&tamanho=20")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.conteudo").isArray()
            .jsonPath("$.pagina").isEqualTo(0)
            .jsonPath("$.tamanho").isEqualTo(20)
            .jsonPath("$.total").isEqualTo(1);
    }

    @Test
    void buscarPorId_DeveRetornarResultado() {
        // Given
        when(service.buscarPorId(anyLong()))
            .thenReturn(Mono.just(resultadoDto));

        // When & Then
        webTestClient.get()
            .uri("/api/resultados/1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.horario").isEqualTo("14:00")
            .jsonPath("$.numeros").isArray()
            .jsonPath("$.numeros.length()").isEqualTo(7);
    }

    @Test
    void buscarPorId_DeveRetornar404QuandoNaoEncontrado() {
        // Given
        when(service.buscarPorId(anyLong()))
            .thenReturn(Mono.empty());

        // When & Then
        webTestClient.get()
            .uri("/api/resultados/999")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void buscarRanking_DeveRetornarListaRanking() {
        // Given
        RankingDto ranking = RankingDto.criar("01", 10L);
        when(service.buscarRanking(any(), anyInt()))
            .thenReturn(Flux.just(ranking));

        // When & Then
        webTestClient.get()
            .uri("/api/resultados/ranking?limite=50")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBodyList(RankingDto.class)
            .hasSize(1);
    }

    @Test
    void buscarEstatisticas_DeveRetornarEstatisticas() {
        // Given
        EstatisticasDto estatisticas = EstatisticasDto.basicas(100L, 700L);
        when(service.buscarEstatisticas())
            .thenReturn(Mono.just(estatisticas));

        // When & Then
        webTestClient.get()
            .uri("/api/resultados/estatisticas")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.totalResultados").isEqualTo(100)
            .jsonPath("$.totalSorteios").isEqualTo(700);
    }

    @Test
    void buscarResultadosHoje_DeveRetornarPaginacao() {
        // Given
        when(service.buscarResultadosHoje(anyInt(), anyInt()))
            .thenReturn(Mono.just(paginacaoDto));

        // When & Then
        webTestClient.get()
            .uri("/api/resultados/hoje")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.conteudo").isArray();
    }

    @Test
    void buscarUltimoPorHorario_DeveRetornarResultado() {
        // Given
        when(service.buscarUltimoPorHorario(anyString()))
            .thenReturn(Mono.just(resultadoDto));

        // When & Then
        webTestClient.get()
            .uri("/api/resultados/ultimo/14:00")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.horario").isEqualTo("14:00");
    }

    @Test
    void buscarHorariosPorData_DeveRetornarListaHorarios() {
        // Given
        when(service.buscarHorariosPorData(any(LocalDate.class)))
            .thenReturn(Flux.just("14:00", "18:00", "21:00"));

        // When & Then
        webTestClient.get()
            .uri("/api/resultados/horarios?data=2024-01-15")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(String.class)
            .hasSize(3);
    }

    @Test
    void buscarResultados_DeveValidarParametros() {
        // When & Then - Página negativa
        webTestClient.get()
            .uri("/api/resultados?pagina=-1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest();

        // When & Then - Tamanho muito grande
        webTestClient.get()
            .uri("/api/resultados?tamanho=200")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest();
    }
}