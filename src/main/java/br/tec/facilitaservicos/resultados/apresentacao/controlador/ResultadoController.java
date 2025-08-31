package br.tec.facilitaservicos.resultados.apresentacao.controlador;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import br.tec.facilitaservicos.resultados.aplicacao.servico.ResultadoService;
import br.tec.facilitaservicos.resultados.apresentacao.dto.EstatisticasDto;
import br.tec.facilitaservicos.resultados.apresentacao.dto.PaginacaoDto;
import br.tec.facilitaservicos.resultados.apresentacao.dto.RankingDto;
import br.tec.facilitaservicos.resultados.apresentacao.dto.ResultadoDto;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * ============================================================================
 * 📊 CONTROLADOR REATIVO - RESULTADOS DE LOTERIA
 * ============================================================================
 * 
 * Controlador 100% reativo para consulta de resultados usando WebFlux
 * 
 * Endpoints disponíveis:
 * - GET /rest/v1/resultados - Buscar resultados paginados
 * - GET /rest/v1/resultados/{id} - Buscar resultado específico  
 * - GET /rest/v1/resultados/ranking - Ranking de números mais sorteados
 * - GET /rest/v1/resultados/estatisticas - Estatísticas agregadas
 * - GET /rest/v1/resultados/hoje - Resultados de hoje
 * - GET /rest/v1/resultados/horarios - Horários disponíveis por data
 * - GET /rest/v1/resultados/ultimo/{horario} - Último resultado por horário
 * 
 * @author Sistema de Migração R2DBC
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/rest/v1/resultados")
@Validated
@Tag(name = "Resultados", description = "API para consulta de resultados de loteria")
public class ResultadoController {

    private final ResultadoService service;

    @Value("${pagination.default-size:20}")
    private int tamanhoDefault;

    public ResultadoController(ResultadoService service) {
        this.service = service;
    }

    @Operation(summary = "Buscar resultados paginados", 
               description = "Busca resultados com paginação e filtros opcionais")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resultados encontrados com sucesso",
                    content = @Content(schema = @Schema(implementation = PaginacaoDto.class))),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @RateLimiter(name = "resultados-service")
    public Mono<ResponseEntity<PaginacaoDto<ResultadoDto>>> buscarResultados(
            @Parameter(description = "Número da página (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            
            @Parameter(description = "Tamanho da página", example = "20")  
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho,
            
            @Parameter(description = "Ordenação (campo,direção)", example = "dataResultado,desc")
            @RequestParam(required = false) String ordenarPor,
            
            @Parameter(description = "Período em dias (filtro)", example = "30")
            @RequestParam(required = false) @Min(1) Integer periodo
    ) {
        return service.buscarResultados(pagina, tamanho, ordenarPor, periodo)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Buscar resultado por ID", 
               description = "Busca um resultado específico pelo identificador")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resultado encontrado",
                    content = @Content(schema = @Schema(implementation = ResultadoDto.class))),
        @ApiResponse(responseCode = "404", description = "Resultado não encontrado"),
        @ApiResponse(responseCode = "400", description = "ID inválido")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @RateLimiter(name = "resultados-service")
    public Mono<ResponseEntity<ResultadoDto>> buscarPorId(
            @Parameter(description = "ID do resultado", example = "123")
            @PathVariable @Min(1) Long id
    ) {
        return service.buscarPorId(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Ranking de números mais sorteados", 
               description = "Retorna ranking dos números mais frequentes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ranking gerado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    })
    @GetMapping(value = "/ranking", produces = MediaType.APPLICATION_JSON_VALUE)
    @RateLimiter(name = "resultados-service")
    public Flux<RankingDto> buscarRanking(
            @Parameter(description = "Temporada em dias", example = "90")
            @RequestParam(required = false) @Min(1) Integer temporada,
            
            @Parameter(description = "Limite de resultados", example = "50")
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) Integer limite
    ) {
        return service.buscarRanking(temporada, limite);
    }

    @Operation(summary = "Estatísticas gerais", 
               description = "Retorna estatísticas agregadas do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estatísticas calculadas",
                    content = @Content(schema = @Schema(implementation = EstatisticasDto.class))),
        @ApiResponse(responseCode = "500", description = "Erro ao calcular estatísticas")
    })
    @GetMapping(value = "/estatisticas", produces = MediaType.APPLICATION_JSON_VALUE)
    @RateLimiter(name = "resultados-service")
    public Mono<ResponseEntity<EstatisticasDto>> buscarEstatisticas() {
        return service.buscarEstatisticas()
            .map(ResponseEntity::ok);
    }

    @Operation(summary = "Resultados de hoje", 
               description = "Busca todos os resultados do dia atual")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resultados de hoje"),
        @ApiResponse(responseCode = "204", description = "Nenhum resultado hoje")
    })
    @GetMapping(value = "/hoje", produces = MediaType.APPLICATION_JSON_VALUE)
    @RateLimiter(name = "resultados-service")
    public Mono<ResponseEntity<PaginacaoDto<ResultadoDto>>> buscarResultadosHoje(
            @Parameter(description = "Número da página", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            
            @Parameter(description = "Tamanho da página", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int tamanho
    ) {
        return service.buscarResultadosHoje(pagina, tamanho)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Último resultado por horário", 
               description = "Busca o resultado mais recente de um horário específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Último resultado encontrado"),
        @ApiResponse(responseCode = "404", description = "Nenhum resultado para o horário"),
        @ApiResponse(responseCode = "400", description = "Horário inválido")
    })
    @GetMapping(value = "/ultimo/{horario}", produces = MediaType.APPLICATION_JSON_VALUE)
    @RateLimiter(name = "resultados-service")
    public Mono<ResponseEntity<ResultadoDto>> buscarUltimoPorHorario(
            @Parameter(description = "Horário do resultado", example = "14:00")
            @PathVariable @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "Horário deve estar no formato HH:mm") String horario
    ) {
        return service.buscarUltimoPorHorario(horario)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Horários disponíveis por data", 
               description = "Lista horários que possuem resultados em uma data específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de horários"),
        @ApiResponse(responseCode = "204", description = "Nenhum horário na data"),
        @ApiResponse(responseCode = "400", description = "Data inválida")
    })
    @GetMapping(value = "/horarios", produces = MediaType.APPLICATION_JSON_VALUE)
    @RateLimiter(name = "resultados-service")
    public Mono<java.util.List<String>> buscarHorariosPorData(
            @Parameter(description = "Data para buscar horários", example = "2024-01-15")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data
    ) {
        return service.buscarHorariosPorData(data).collectList();
    }
}
