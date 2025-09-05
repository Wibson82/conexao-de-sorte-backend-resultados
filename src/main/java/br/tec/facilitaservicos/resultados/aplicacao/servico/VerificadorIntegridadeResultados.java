package br.tec.facilitaservicos.resultados.aplicacao.servico;

import br.tec.facilitaservicos.resultados.dominio.repositorio.RepositorioResultadoR2dbc;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Map;

@Service
public class VerificadorIntegridadeResultados {

    private final RepositorioResultadoR2dbc repositorio;

    public VerificadorIntegridadeResultados(RepositorioResultadoR2dbc repositorio) {
        this.repositorio = repositorio;
    }

    public Mono<Map<String, Object>> verificarPresenca(String horario, LocalDate data) {
        return repositorio.existsByHorarioAndDataResultado(horario, data)
                .map(exists -> Map.of(
                        "horario", horario,
                        "data", data,
                        "presente", exists
                ));
    }
}

