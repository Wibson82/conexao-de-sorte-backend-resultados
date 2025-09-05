package br.tec.facilitaservicos.resultados.apresentacao.controlador;

import br.tec.facilitaservicos.resultados.aplicacao.servico.LoteriaService;
import br.tec.facilitaservicos.resultados.aplicacao.servico.ResultadoService;
import br.tec.facilitaservicos.resultados.apresentacao.dto.PaginacaoDto;
import br.tec.facilitaservicos.resultados.apresentacao.dto.ResultadoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/rest/v1/loterias")
@Tag(name = "Loterias", description = "Consulta de modalidades e resultados por modalidade")
public class LoteriaController {

    private final LoteriaService loteriaService;
    private final ResultadoService resultadoService;

    public LoteriaController(LoteriaService loteriaService, ResultadoService resultadoService) {
        this.loteriaService = loteriaService;
        this.resultadoService = resultadoService;
    }

    @GetMapping(value = "/modalidades", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Listar modalidades (horários) recentes")
    public Mono<List<String>> listarModalidades() {
        return loteriaService.listarModalidadesRecentes();
    }

    @GetMapping(value = "/{modalidade}/ultimo", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Último resultado da modalidade (horário)")
    public Mono<ResponseEntity<ResultadoDto>> ultimoPorModalidade(
            @PathVariable @Pattern(regexp = "^\\d{2}:\\d{2}$") String modalidade) {
        return resultadoService.buscarUltimoPorHorario(modalidade)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/{modalidade}/periodo", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Resultados por modalidade no período")
    public Mono<ResponseEntity<PaginacaoDto<ResultadoDto>>> porPeriodo(
            @PathVariable @Pattern(regexp = "^\\d{2}:\\d{2}$") String modalidade,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) {
        return loteriaService.listarPorModalidadeEPeriodo(modalidade, de, ate, pagina, tamanho)
                .map(ResponseEntity::ok);
    }
}

