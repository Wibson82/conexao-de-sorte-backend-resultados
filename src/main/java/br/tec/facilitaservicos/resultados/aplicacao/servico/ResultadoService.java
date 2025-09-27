package br.tec.facilitaservicos.resultados.aplicacao.servico;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import br.tec.facilitaservicos.resultados.aplicacao.mapper.ResultadoMapper;
import br.tec.facilitaservicos.resultados.apresentacao.dto.EstatisticasDto;
import br.tec.facilitaservicos.resultados.apresentacao.dto.PaginacaoDto;
import br.tec.facilitaservicos.resultados.apresentacao.dto.RankingDto;
import br.tec.facilitaservicos.resultados.apresentacao.dto.ResultadoDto;
import br.tec.facilitaservicos.resultados.dominio.repositorio.RepositorioResultadoR2dbc;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;
import java.util.UUID;

/**
 * Serviço reativo para operações de Resultados
 *
 * @author Sistema de Migração R2DBC
 * @version 1.0
 * @since 2024
 */
@Service
public class ResultadoService {

    // Constantes para valores hardcoded
    private static final int PAGINA_MINIMA = 0;
    private static final int TAMANHO_MINIMO = 1;
    private static final int TAMANHO_MAXIMO_DEFAULT = 100;
    private static final int LIMITE_MAXIMO_RANKING = 50;

    private final RepositorioResultadoR2dbc repositorio;
    private final ResultadoMapper mapper;
    private final WebClient webClient;

    @Value("${pagination.default-size:20}")
    private int tamanhoDefault = 20;

    @Value("${pagination.max-size:100}")
    private int tamanhoMaximo = TAMANHO_MAXIMO_DEFAULT;

    public ResultadoService(RepositorioResultadoR2dbc repositorio,
                           ResultadoMapper mapper,
                           WebClient.Builder webClientBuilder) {
        this.repositorio = repositorio;
        this.mapper = mapper;
        this.webClient = webClientBuilder.baseUrl("http://localhost:8084").build(); // Scheduler
    }

    /**
     * Busca resultados paginados com filtros
     * @param pagina Número da página (0-based)
     * @param tamanho Tamanho da página
     * @param ordenacao Ordenação (formato: campo,direção)
     * @param periodo Período em dias (opcional)
     * @return Paginação reativa com resultados
     */
    public Mono<PaginacaoDto<ResultadoDto>> buscarResultados(int pagina, int tamanho, String ordenacao, Integer periodo) {
        // Validar e ajustar parâmetros
        final int paginaFinal = Math.max(pagina, PAGINA_MINIMA);
        final int tamanhoFinal = Math.clamp(tamanho, TAMANHO_MINIMO, Math.max(tamanhoMaximo, TAMANHO_MINIMO));

        Sort sort = criarOrdenacao(ordenacao);
        Pageable pageable = PageRequest.of(paginaFinal, tamanhoFinal, sort);

        Flux<ResultadoDto> resultados;
        Mono<Long> totalElements;

        if (periodo != null && periodo > 0) {
            LocalDate dataInicio = LocalDate.now().minusDays(periodo);
            LocalDate dataFim = LocalDate.now();

            resultados = repositorio.findByPeriodo(dataInicio, dataFim, pageable)
                .map(mapper::paraDto);
            totalElements = repositorio.countByPeriodo(dataInicio, dataFim);
        } else {
            resultados = repositorio.findAllPaginado(pageable)
                .map(mapper::paraDto);
            totalElements = repositorio.countTotal();
        }

        return Mono.zip(resultados.collectList(), totalElements)
            .map(tuple -> PaginacaoDto.criar(tuple.getT1(), paginaFinal, tamanhoFinal, tuple.getT2()));
    }

    /**
     * Busca resultado por ID
     * @param id ID do resultado
     * @return Resultado encontrado
     */
    public Mono<ResultadoDto> buscarPorId(Long id) {
        return repositorio.findById(id)
            .map(mapper::paraDto);
    }

    /**
     * Busca ranking de números mais sorteados
     * @param temporada Temporada em dias (opcional)
     * @param limite Limite de resultados
     * @return Lista com ranking
     */
    public Flux<RankingDto> buscarRanking(Integer temporada, Integer limite) {
        final int limiteRanking = Math.clamp(limite != null ? limite : TAMANHO_MINIMO, TAMANHO_MINIMO, LIMITE_MAXIMO_RANKING);

        Flux<Object[]> estatisticas;

        if (temporada != null && temporada > 0) {
            LocalDate dataInicio = LocalDate.now().minusDays(temporada);
            LocalDate dataFim = LocalDate.now();
            estatisticas = repositorio.findNumerosMaisSorteadosNoPeriodo(dataInicio, dataFim, limiteRanking);
        } else {
            estatisticas = repositorio.findEstatisticasFrequenciaNumeros(limiteRanking);
        }

        AtomicInteger posicao = new AtomicInteger(1);

        return estatisticas
            .map(row -> {
                String numero = (String) row[0];
                Long frequencia = ((Number) row[1]).longValue();
                return RankingDto.criar(numero, frequencia)
                    .comPosicao(posicao.getAndIncrement());
            });
    }

    /**
     * Busca estatísticas gerais
     * @return Estatísticas completas
     */
    public Mono<EstatisticasDto> buscarEstatisticas() {
        Mono<Long> totalResultados = repositorio.countTotal();
        Mono<Long> totalSorteios = totalResultados.map(total -> total * 7); // 7 números por resultado

        Mono<Tuple2<LocalDate, LocalDate>> periodo = Mono.zip(
            repositorio.findResultadoMaisAntigo().map(r -> r.getDataResultado()),
            repositorio.findResultadoMaisRecente().map(r -> r.getDataResultado())
        );

        Flux<RankingDto> numerosMaisFrequentes = repositorio.findEstatisticasFrequenciaNumeros(10)
            .map(row -> RankingDto.criar((String) row[0], ((Number) row[1]).longValue()));

        Flux<String> horariosAtivos = repositorio.findHorariosPorData(LocalDate.now())
            .switchIfEmpty(buscarHorariosRecentes());

        return Mono.zip(totalResultados, totalSorteios, periodo)
            .flatMap(tuple -> {
                Long total = tuple.getT1();
                Long sorteios = tuple.getT2();
                LocalDate inicio = tuple.getT3().getT1();
                LocalDate fim = tuple.getT3().getT2();

                return Mono.zip(
                    numerosMaisFrequentes.collectList(),
                    horariosAtivos.collectList()
                ).map(data ->
                    EstatisticasDto.completas(
                        total, sorteios, inicio, fim,
                        data.getT1(), null, data.getT2()
                    )
                );
            });
    }

    /**
     * Busca resultados de hoje
     * @param pagina Página
     * @param tamanho Tamanho da página
     * @return Resultados paginados de hoje
     */
    public Mono<PaginacaoDto<ResultadoDto>> buscarResultadosHoje(int pagina, int tamanho) {
        final int paginaFinal = Math.max(pagina, PAGINA_MINIMA);
        final int tamanhoFinal = Math.clamp(tamanho, TAMANHO_MINIMO, Math.max(tamanhoMaximo, TAMANHO_MINIMO));

        Pageable pageable = PageRequest.of(paginaFinal, tamanhoFinal);

        return Mono.zip(
            repositorio.findResultadosDeHoje(pageable).map(mapper::paraDto).collectList(),
            repositorio.countByDataResultado(LocalDate.now())
        ).map(tuple -> PaginacaoDto.criar(tuple.getT1(), paginaFinal, tamanhoFinal, tuple.getT2()));
    }

    /**
     * Busca horários disponíveis em uma data
     * @param data Data para buscar
     * @return Lista de horários
     */
    public Flux<String> buscarHorariosPorData(LocalDate data) {
        return repositorio.findHorariosPorData(data);
    }

    // Métodos auxiliares

    private Sort criarOrdenacao(String ordenacao) {
        if (ordenacao == null || ordenacao.trim().isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "dataResultado", "horario");
        }

        String[] partes = ordenacao.split(",");
        String campo = partes[0].trim();
        String direcao = partes.length > 1 ? partes[1].trim() : "desc";

        Sort.Direction direction = "asc".equalsIgnoreCase(direcao)
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;

        return Sort.by(direction, campo);
    }

    private Flux<String> buscarHorariosRecentes() {
        var pageable = PageRequest.of(0, 50);
        var flux = repositorio.findResultadosRecentes(pageable);
        // Evita NPE caso um mock mal configurado retorne null
        return (flux == null ? Flux.<br.tec.facilitaservicos.resultados.dominio.entidade.ResultadoR2dbc>empty() : flux)
            .map(br.tec.facilitaservicos.resultados.dominio.entidade.ResultadoR2dbc::getHorario)
            .distinct();
    }

    // ============================================================================
    // 🌐 MÉTODOS PARA APIS PÚBLICAS E LOTERIAS
    // ============================================================================

    /**
     * Busca resultados públicos com filtros.
     */
    public Mono<PaginacaoDto<ResultadoDto>> buscarResultadosPublicos(int pagina, int tamanho,
                                                                    String horario, Integer periodo) {
        final int paginaFinal = Math.max(pagina, PAGINA_MINIMA);
        final int tamanhoFinal = Math.clamp(tamanho, TAMANHO_MINIMO, 50); // Máximo 50 para público

        Pageable pageable = PageRequest.of(paginaFinal, tamanhoFinal);

        Flux<br.tec.facilitaservicos.resultados.dominio.entidade.ResultadoR2dbc> query;
        Mono<Long> count;

        if (horario != null && periodo != null) {
            LocalDate dataInicio = LocalDate.now().minusDays(periodo);
            query = repositorio.findByHorarioOrderByDataResultadoDesc(horario, pageable)
                .filter(r -> r.getDataResultado().isAfter(dataInicio));
            count = repositorio.countByHorario(horario);
        } else if (horario != null) {
            query = repositorio.findByHorarioOrderByDataResultadoDesc(horario, pageable);
            count = repositorio.countByHorario(horario);
        } else if (periodo != null) {
            LocalDate dataInicio = LocalDate.now().minusDays(periodo);
            query = repositorio.findByDataResultadoAfter(dataInicio, pageable);
            count = repositorio.countByDataResultadoAfter(dataInicio);
        } else {
            query = repositorio.findAllPaginado(pageable);
            count = repositorio.count();
        }

        return Mono.zip(
            query.map(mapper::paraDto).collectList(),
            count
        ).map(tuple -> PaginacaoDto.criar(tuple.getT1(), paginaFinal, tamanhoFinal, tuple.getT2()));
    }

    /**
     * Busca resultado por horário e data.
     */
    public Mono<ResultadoDto> buscarPorHorarioData(String horario, LocalDate data) {
        return repositorio.findByHorarioAndDataResultado(horario, data)
            .map(mapper::paraDto);
    }

    /**
     * Dispara extração ETL via Scheduler.
     */
    public Mono<String> dispararExtracao(String horario, String data) {
        String jobId = UUID.randomUUID().toString();

        Map<String, Object> request = Map.of(
            "horario", horario != null ? horario : "20:00",
            "data", data,
            "jobId", jobId
        );

        return webClient.post()
            .uri("/jobs/loterias/etl")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Map.class)
            .map(response -> (String) response.get("jobId"))
            .onErrorReturn(jobId); // Retorna jobId mesmo se falhar
    }

    /**
     * Lista modalidades disponíveis.
     */
    public Flux<Map<String, String>> listarModalidades() {
        return Flux.just(
            Map.of("codigo", "megasena", "nome", "Mega Sena"),
            Map.of("codigo", "quina", "nome", "Quina"),
            Map.of("codigo", "lotofacil", "nome", "Lotofácil"),
            Map.of("codigo", "lotomania", "nome", "Lotomania"),
            Map.of("codigo", "timemania", "nome", "Timemania"),
            Map.of("codigo", "dupla-sena", "nome", "Dupla Sena"),
            Map.of("codigo", "federal", "nome", "Federal")
        );
    }

    /**
     * Último resultado por horário.
     */
    public Mono<ResultadoDto> buscarUltimoPorHorario(String horario) {
        return repositorio.findUltimoResultadoPorHorario(horario)
            .map(mapper::paraDto);
    }

    /**
     * Resultado por horário e data específica.
     */
    public Mono<ResultadoDto> buscarPorHorarioEData(String horario, LocalDate data) {
        return repositorio.findByHorarioAndDataResultado(horario, data)
            .map(mapper::paraDto);
    }

    /**
     * Resultados por período específico.
     */
    public Flux<ResultadoDto> buscarPorPeriodo(LocalDate de, LocalDate ate) {
        return repositorio.findByPeriodo(de, ate, Pageable.unpaged())
            .map(mapper::paraDto);
    }
}