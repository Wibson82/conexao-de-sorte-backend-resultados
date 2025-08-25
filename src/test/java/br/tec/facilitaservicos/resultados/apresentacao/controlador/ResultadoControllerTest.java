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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration;

/**
 * Testes de integração para ResultadoController
 * 
 * @author Sistema de Migração R2DBC
 * @version 1.0
 * @since 2024
 */
@WebFluxTest(
    controllers = ResultadoController.class,
    excludeAutoConfiguration = {
        R2dbcAutoConfiguration.class,
        R2dbcDataAutoConfiguration.class,
        FlywayAutoConfiguration.class,
        ReactiveSecurityAutoConfiguration.class,
        ReactiveOAuth2ResourceServerAutoConfiguration.class
    }
)
@ActiveProfiles("test")
@Import({ResultadoController.class, ResultadoControllerTest.TestSecurityConfig.class})
class ResultadoControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ResultadoService service;

    private ResultadoDto resultadoDto;
    private PaginacaoDto<ResultadoDto> paginacaoDto;

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
        when(service.buscarResultados(
                anyInt(),
                anyInt(),
                org.mockito.ArgumentMatchers.isNull(String.class),
                org.mockito.ArgumentMatchers.isNull(Integer.class)))
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
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(3);
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