package br.tec.facilitaservicos.resultados.aplicacao.servico;

import br.tec.facilitaservicos.resultados.apresentacao.dto.PaginacaoDto;
import br.tec.facilitaservicos.resultados.apresentacao.dto.ResultadoDto;
import br.tec.facilitaservicos.resultados.aplicacao.mapper.ResultadoMapper;
import br.tec.facilitaservicos.resultados.dominio.entidade.ResultadoR2dbc;
import br.tec.facilitaservicos.resultados.dominio.repositorio.RepositorioResultadoR2dbc;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoteriaService {

    private final RepositorioResultadoR2dbc repositorio;
    private final ResultadoMapper mapper;

    public LoteriaService(RepositorioResultadoR2dbc repositorio, ResultadoMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    public Mono<List<String>> listarModalidadesRecentes() {
        // Retorna horários distintos do dia atual; se vazio, tenta dias recentes
        return repositorio.findHorariosPorData(LocalDate.now()).collectList()
                .flatMap(list -> list.isEmpty() ?
                        repositorio.findResultadosRecentes(PageRequest.of(0, 200))
                                .map(ResultadoR2dbc::getHorario)
                                .distinct()
                                .collectList() : Mono.just(list));
    }

    public Mono<PaginacaoDto<ResultadoDto>> listarPorModalidadeEPeriodo(String modalidade, LocalDate de, LocalDate ate, int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(Math.max(0, pagina), Math.max(1, tamanho));
        Flux<ResultadoDto> resultados = repositorio.findByPeriodo(de, ate, pageable)
                .filter(r -> modalidade.equals(r.getHorario()))
                .map(mapper::paraDto);

        Mono<Long> total = repositorio.countByPeriodo(de, ate)
                .flatMap(cnt -> resultados.count().map(Long::valueOf)); // ajustar para contagem filtrada

        return Mono.zip(resultados.collectList(), total)
                .map(tuple -> PaginacaoDto.criar(tuple.getT1(), pagina, tamanho, tuple.getT2()));
    }
}

