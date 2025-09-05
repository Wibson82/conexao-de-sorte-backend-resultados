package br.tec.facilitaservicos.resultados.aplicacao.servico;

import br.tec.facilitaservicos.resultados.dominio.repositorio.RepositorioResultadoR2dbc;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

@Service
public class ServicoHorarioValido {

    private static final Pattern HH_MM = Pattern.compile("^\\d{2}:\\d{2}$");
    private final RepositorioResultadoR2dbc repositorio;

    public ServicoHorarioValido(RepositorioResultadoR2dbc repositorio) {
        this.repositorio = repositorio;
    }

    public Mono<Boolean> isValido(String horario) {
        if (horario == null || !HH_MM.matcher(horario).matches()) {
            return Mono.just(false);
        }
        // Considera válido se houver ao menos um registro recente com esse horário
        return repositorio.findByHorarioOrderByDataResultadoDesc(horario, PageRequest.of(0, 1))
                .hasElements();
    }
}

