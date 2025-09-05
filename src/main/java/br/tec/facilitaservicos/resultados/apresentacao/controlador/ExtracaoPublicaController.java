package br.tec.facilitaservicos.resultados.apresentacao.controlador;

import br.tec.facilitaservicos.resultados.aplicacao.servico.IntegracaoSchedulerService;
import br.tec.facilitaservicos.resultados.aplicacao.servico.ServicoHorarioValido;
import br.tec.facilitaservicos.resultados.apresentacao.dto.EtlSolicitacaoRequest;
import br.tec.facilitaservicos.resultados.apresentacao.dto.EtlSolicitacaoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RestController
@RequestMapping("/rest/v1/resultados/publico/extrair")
@Tag(name = "Extração Pública", description = "Disparo de extrações de resultados (via Scheduler)")
public class ExtracaoPublicaController {

    private final IntegracaoSchedulerService schedulerService;
    private final ServicoHorarioValido horarioValido;

    public ExtracaoPublicaController(IntegracaoSchedulerService schedulerService, ServicoHorarioValido horarioValido) {
        this.schedulerService = schedulerService;
        this.horarioValido = horarioValido;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Disparar extração por modalidade (admin)")
    @PreAuthorize("hasAnyAuthority('SCOPE_admin','SCOPE_results.write')")
    public Mono<ResponseEntity<EtlSolicitacaoResponse>> disparar(@Valid @RequestBody EtlSolicitacaoRequest req) {
        LocalDate data = (req.data() != null && !req.data().isBlank()) ? LocalDate.parse(req.data()) : null;
        return horarioValido.isValido(req.modalidade())
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Modalidade/Horário inválido")))
                .flatMap(ok -> schedulerService.dispararEtlLoterias(req.modalidade(), data))
                .map(jobId -> ResponseEntity.ok(EtlSolicitacaoResponse.of(jobId)));
    }

    @GetMapping(value = "/{horario}/{data}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Disparar extração via URL (admin)")
    @PreAuthorize("hasAnyAuthority('SCOPE_admin','SCOPE_results.write')")
    public Mono<ResponseEntity<EtlSolicitacaoResponse>> dispararGet(
            @PathVariable @Pattern(regexp = "^\\d{2}:\\d{2}$") String horario,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return horarioValido.isValido(horario)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Horário inválido")))
                .flatMap(ok -> schedulerService.dispararEtlLoterias(horario, data))
                .map(jobId -> ResponseEntity.ok(EtlSolicitacaoResponse.of(jobId)));
    }
}

