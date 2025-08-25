package br.tec.facilitaservicos.resultados.aplicacao.servico;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import br.tec.facilitaservicos.resultados.aplicacao.mapper.ResultadoMapper;
import br.tec.facilitaservicos.resultados.apresentacao.dto.PaginacaoDto;
import br.tec.facilitaservicos.resultados.apresentacao.dto.ResultadoDto;
import br.tec.facilitaservicos.resultados.dominio.entidade.ResultadoR2dbc;
import br.tec.facilitaservicos.resultados.dominio.repositorio.RepositorioResultadoR2dbc;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para ResultadoService
 * 
 * @author Sistema de Migração R2DBC
 * @version 1.0
 * @since 2024
 */
@ExtendWith(MockitoExtension.class)
class ResultadoServiceTest {

    @Mock
    private RepositorioResultadoR2dbc repositorio;

    @Mock
    private ResultadoMapper mapper;

    @InjectMocks
    private ResultadoService service;

    private ResultadoR2dbc entidade;
    private ResultadoDto dto;

    @BeforeEach
    void setUp() {
        entidade = new ResultadoR2dbc("14:00", "01", "15", "23", "34", "45", "56", "67", LocalDate.now());
        entidade.setId(1L);

        dto = ResultadoDto.completo(
            1L,
            "14:00",
            List.of("01", "15", "23", "34", "45", "56", "67"),
            "241",
            LocalDate.now(),
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    @Test
    void buscarResultados_DeveBuscarComSucesso() {
        // Given
        when(repositorio.findAllPaginado(any(Pageable.class)))
            .thenReturn(Flux.just(entidade));
        when(repositorio.countTotal())
            .thenReturn(Mono.just(1L));
        when(mapper.paraDto(entidade))
            .thenReturn(dto);

        // When
        Mono<PaginacaoDto<ResultadoDto>> resultado = service.buscarResultados(0, 20, null, null);

        // Then
        StepVerifier.create(resultado)
            .assertNext(paginacao -> {
                assertEquals(1, paginacao.conteudo().size());
                assertEquals(0, paginacao.pagina());
                assertEquals(20, paginacao.tamanho());
                assertEquals(1L, paginacao.total());
                assertTrue(paginacao.primeiro());
                assertTrue(paginacao.ultimo());
            })
            .verifyComplete();
    }

    @Test
    void buscarPorId_DeveRetornarResultado() {
        // Given
        when(repositorio.findById(anyLong()))
            .thenReturn(Mono.just(entidade));
        when(mapper.paraDto(entidade))
            .thenReturn(dto);

        // When
        Mono<ResultadoDto> resultado = service.buscarPorId(1L);

        // Then
        StepVerifier.create(resultado)
            .assertNext(r -> {
                assertEquals(1L, r.id());
                assertEquals("14:00", r.horario());
                assertEquals(7, r.numeros().size());
            })
            .verifyComplete();
    }

    @Test
    void buscarPorId_DeveRetornarVazioQuandoNaoEncontrado() {
        // Given
        when(repositorio.findById(anyLong()))
            .thenReturn(Mono.empty());

        // When
        Mono<ResultadoDto> resultado = service.buscarPorId(999L);

        // Then
        StepVerifier.create(resultado)
            .verifyComplete();
    }

    @Test
    void buscarRanking_DeveRetornarRankingComSucesso() {
        // Given
        Object[] rankingData = {"01", 10L};
        when(repositorio.findEstatisticasFrequenciaNumeros(50))
            .thenReturn(Flux.<Object[]>just(rankingData));

        // When
        var resultado = service.buscarRanking(null, 50);

        // Then
        StepVerifier.create(resultado)
            .assertNext(ranking -> {
                assertEquals("01", ranking.numero());
                assertEquals(10L, ranking.frequencia());
                assertEquals(1, ranking.posicao());
            })
            .verifyComplete();
    }

    @Test
    void buscarEstatisticas_DeveCalcularEstatisticas() {
        // Given
        when(repositorio.countTotal())
            .thenReturn(Mono.just(100L));
        when(repositorio.findResultadoMaisAntigo())
            .thenReturn(Mono.just(entidade));
        when(repositorio.findResultadoMaisRecente())
            .thenReturn(Mono.just(entidade));
        when(repositorio.findEstatisticasFrequenciaNumeros(10))
            .thenReturn(Flux.empty());
        when(repositorio.findHorariosPorData(any(LocalDate.class)))
            .thenReturn(Flux.just("14:00"));
        when(repositorio.findResultadosRecentes(any(Pageable.class)))
            .thenReturn(Flux.just(entidade));

        // When
        var resultado = service.buscarEstatisticas();

        // Then
        StepVerifier.create(resultado)
            .assertNext(stats -> {
                assertEquals(100L, stats.totalResultados());
                assertEquals(700L, stats.totalSorteios()); // 100 * 7
                assertEquals(7.0, stats.mediaNumerosPorSorteio());
            })
            .verifyComplete();
    }

    @Test
    void buscarResultadosHoje_DeveBuscarResultadosDeHoje() {
        // Given
        when(repositorio.findResultadosDeHoje(any(Pageable.class)))
            .thenReturn(Flux.just(entidade));
        when(repositorio.countByDataResultado(any(LocalDate.class)))
            .thenReturn(Mono.just(1L));
        when(mapper.paraDto(entidade))
            .thenReturn(dto);

        // When
        var resultado = service.buscarResultadosHoje(0, 20);

        // Then
        StepVerifier.create(resultado)
            .assertNext(paginacao -> {
                assertEquals(1, paginacao.conteudo().size());
                assertTrue(paginacao.temConteudo());
            })
            .verifyComplete();
    }

    @Test
    void buscarUltimoPorHorario_DeveRetornarUltimoResultado() {
        // Given
        when(repositorio.findUltimoResultadoPorHorario("14:00"))
            .thenReturn(Mono.just(entidade));
        when(mapper.paraDto(entidade))
            .thenReturn(dto);

        // When
        var resultado = service.buscarUltimoPorHorario("14:00");

        // Then
        StepVerifier.create(resultado)
            .assertNext(r -> assertEquals("14:00", r.horario()))
            .verifyComplete();
    }

    @Test
    void buscarHorariosPorData_DeveRetornarHorarios() {
        // Given
        LocalDate data = LocalDate.of(2024, 1, 15);
        when(repositorio.findHorariosPorData(data))
            .thenReturn(Flux.just("14:00", "18:00", "21:00"));

        // When
        var resultado = service.buscarHorariosPorData(data);

        // Then
        StepVerifier.create(resultado)
            .expectNext("14:00")
            .expectNext("18:00")
            .expectNext("21:00")
            .verifyComplete();
    }
}