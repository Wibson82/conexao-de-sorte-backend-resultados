package br.tec.facilitaservicos.resultados.aplicacao.mapper;

import br.tec.facilitaservicos.resultados.apresentacao.dto.ResultadoDto;
import br.tec.facilitaservicos.resultados.dominio.entidade.ResultadoR2dbc;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Mapper reativo para conversão entre ResultadoR2dbc e ResultadoDto
 * 
 * @author Sistema de Migração R2DBC
 * @version 1.0
 * @since 2024
 */
@Component
public class ResultadoMapper {

    /**
     * Converte entidade para DTO
     * @param entidade Entidade R2DBC
     * @return DTO reativo
     */
    public ResultadoDto paraDto(ResultadoR2dbc entidade) {
        if (entidade == null) {
            return null;
        }

        return ResultadoDto.completo(
            entidade.getId(),
            entidade.getHorario(),
            entidade.obterNumeros(),
            entidade.getSoma(),
            entidade.getDataResultado(),
            entidade.getDataCriacao(),
            entidade.getDataModificacao()
        );
    }

    /**
     * Converte DTO para entidade
     * @param dto DTO de entrada
     * @return Entidade R2DBC
     */
    public ResultadoR2dbc paraEntidade(ResultadoDto dto) {
        if (dto == null || !dto.numerosCompletos()) {
            return null;
        }

        var numeros = dto.numeros();
        var entidade = new ResultadoR2dbc(
            dto.horario(),
            numeros.get(0), numeros.get(1), numeros.get(2), numeros.get(3),
            numeros.get(4), numeros.get(5), numeros.get(6),
            dto.dataResultado()
        );

        if (dto.id() != null) {
            entidade.setId(dto.id());
        }
        
        if (dto.soma() != null) {
            entidade.setSoma(dto.soma());
        }

        return entidade;
    }

    /**
     * Converte Mono de entidade para Mono de DTO
     * @param monoEntidade Mono da entidade
     * @return Mono do DTO
     */
    public Mono<ResultadoDto> paraDto(Mono<ResultadoR2dbc> monoEntidade) {
        return monoEntidade.map(this::paraDto);
    }

    /**
     * Converte Flux de entidades para Flux de DTOs
     * @param fluxEntidades Flux das entidades
     * @return Flux dos DTOs
     */
    public Flux<ResultadoDto> paraDto(Flux<ResultadoR2dbc> fluxEntidades) {
        return fluxEntidades.map(this::paraDto);
    }

    /**
     * Converte Mono de DTO para Mono de entidade
     * @param monoDto Mono do DTO
     * @return Mono da entidade
     */
    public Mono<ResultadoR2dbc> paraEntidade(Mono<ResultadoDto> monoDto) {
        return monoDto.map(this::paraEntidade);
    }

    /**
     * Converte Flux de DTOs para Flux de entidades
     * @param fluxDtos Flux dos DTOs
     * @return Flux das entidades
     */
    public Flux<ResultadoR2dbc> paraEntidade(Flux<ResultadoDto> fluxDtos) {
        return fluxDtos.map(this::paraEntidade);
    }
}