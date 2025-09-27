package br.tec.facilitaservicos.resultados.dominio.repositorio;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.tec.facilitaservicos.resultados.dominio.entidade.ResultadoR2dbc;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositório R2DBC reativo para Resultado
 * Fornece operações de acesso a dados reativas para resultados
 * 
 * @author Sistema de Migração R2DBC
 * @version 1.0
 * @since 2024
 */
@Repository
public interface RepositorioResultadoR2dbc extends R2dbcRepository<ResultadoR2dbc, Long> {

    /**
     * Busca resultado por horário e data
     * @param horario Horário do resultado
     * @param dataResultado Data do resultado
     * @return Mono com resultado encontrado
     */
    Mono<ResultadoR2dbc> findByHorarioAndDataResultado(String horario, LocalDate dataResultado);

    /**
     * Busca resultados por horário ordenados por data
     * @param horario Horário dos resultados
     * @param pageable Paginação
     * @return Flux com resultados do horário
     */
    Flux<ResultadoR2dbc> findByHorarioOrderByDataResultadoDesc(String horario, Pageable pageable);

    /**
     * Busca resultados por data ordenados por horário
     * @param dataResultado Data dos resultados
     * @param pageable Paginação
     * @return Flux com resultados da data
     */
    Flux<ResultadoR2dbc> findByDataResultadoOrderByHorario(LocalDate dataResultado, Pageable pageable);

    /**
     * Busca último resultado por horário
     * @param horario Horário do resultado
     * @return Mono com último resultado
     */
    @Query("SELECT * FROM resultados WHERE horario = :horario ORDER BY data_resultado DESC LIMIT 1")
    Mono<ResultadoR2dbc> findUltimoResultadoPorHorario(@Param("horario") String horario);

    /**
     * Busca resultados de hoje
     * @param pageable Paginação
     * @return Flux com resultados de hoje
     */
    @Query("SELECT * FROM resultados WHERE data_resultado = CURRENT_DATE ORDER BY horario")
    Flux<ResultadoR2dbc> findResultadosDeHoje(Pageable pageable);

    /**
     * Busca resultados por período
     * @param dataInicio Data de início
     * @param dataFim Data de fim
     * @param pageable Paginação
     * @return Flux com resultados do período
     */
    @Query("SELECT * FROM resultados WHERE data_resultado BETWEEN :dataInicio AND :dataFim ORDER BY data_resultado DESC, horario")
    Flux<ResultadoR2dbc> findByPeriodo(@Param("dataInicio") LocalDate dataInicio, 
                                       @Param("dataFim") LocalDate dataFim,
                                       Pageable pageable);

    /**
     * Busca resultados recentes (últimos 7 dias)
     * @param pageable Paginação
     * @return Flux com resultados recentes
     */
    @Query("SELECT * FROM resultados WHERE data_resultado >= CURRENT_DATE - INTERVAL 7 DAY ORDER BY data_resultado DESC, horario")
    Flux<ResultadoR2dbc> findResultadosRecentes(Pageable pageable);

    /**
     * Busca resultados por números específicos
     * @param numero Número a buscar
     * @param pageable Paginação
     * @return Flux com resultados contendo o número
     */
    @Query("SELECT * FROM resultados WHERE primeiro = :numero OR segundo = :numero OR terceiro = :numero OR " +
           "quarto = :numero OR quinto = :numero OR sexto = :numero OR setimo = :numero ORDER BY data_resultado DESC")
    Flux<ResultadoR2dbc> findByNumeroContido(@Param("numero") String numero, Pageable pageable);

    /**
     * Busca resultados com soma específica
     * @param soma Soma dos números
     * @param pageable Paginação
     * @return Flux com resultados da soma
     */
    Flux<ResultadoR2dbc> findBySomaOrderByDataResultadoDesc(String soma, Pageable pageable);

    /**
     * Conta resultados por horário
     * @param horario Horário dos resultados
     * @return Mono com contagem
     */
    @Query("SELECT COUNT(*) FROM resultados WHERE horario = :horario")
    Mono<Long> countByHorario(@Param("horario") String horario);

    /**
     * Conta resultados por data
     * @param dataResultado Data dos resultados
     * @return Mono com contagem
     */
    @Query("SELECT COUNT(*) FROM resultados WHERE data_resultado = :dataResultado")
    Mono<Long> countByDataResultado(@Param("dataResultado") LocalDate dataResultado);

    /**
     * Conta resultados por período
     * @param dataInicio Data de início
     * @param dataFim Data de fim
     * @return Mono com contagem
     */
    @Query("SELECT COUNT(*) FROM resultados WHERE data_resultado BETWEEN :dataInicio AND :dataFim")
    Mono<Long> countByPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    /**
     * Verifica se existe resultado para horário e data
     * @param horario Horário do resultado
     * @param dataResultado Data do resultado
     * @return Mono com true se existe
     */
    @Query("SELECT EXISTS(SELECT 1 FROM resultados WHERE horario = :horario AND data_resultado = :dataResultado)")
    Mono<Boolean> existsByHorarioAndDataResultado(@Param("horario") String horario, 
                                                  @Param("dataResultado") LocalDate dataResultado);

    /**
     * Busca horários disponíveis para uma data
     * @param dataResultado Data para buscar horários
     * @return Flux com horários disponíveis
     */
    @Query("SELECT DISTINCT horario FROM resultados WHERE data_resultado = :dataResultado ORDER BY horario")
    Flux<String> findHorariosPorData(@Param("dataResultado") LocalDate dataResultado);

    /**
     * Busca datas disponíveis para um horário
     * @param horario Horário para buscar datas
     * @param pageable Paginação
     * @return Flux com datas disponíveis
     */
    @Query("SELECT DISTINCT data_resultado FROM resultados WHERE horario = :horario ORDER BY data_resultado DESC")
    Flux<LocalDate> findDatasPorHorario(@Param("horario") String horario, Pageable pageable);

    /**
     * Busca estatísticas de frequência de números
     * @param limite Limite de resultados
     * @return Flux com estatísticas de números mais sorteados
     */
    @Query("SELECT numero, COUNT(*) as frequencia FROM (" +
           "SELECT primeiro as numero FROM resultados UNION ALL " +
           "SELECT segundo as numero FROM resultados UNION ALL " +
           "SELECT terceiro as numero FROM resultados UNION ALL " +
           "SELECT quarto as numero FROM resultados UNION ALL " +
           "SELECT quinto as numero FROM resultados UNION ALL " +
           "SELECT sexto as numero FROM resultados UNION ALL " +
           "SELECT setimo as numero FROM resultados" +
           ") AS todos_numeros GROUP BY numero ORDER BY frequencia DESC LIMIT :limite")
    Flux<Object[]> findEstatisticasFrequenciaNumeros(@Param("limite") Integer limite);

    /**
     * Busca números mais sorteados em período
     * @param dataInicio Data de início
     * @param dataFim Data de fim
     * @param limite Limite de resultados
     * @return Flux com números mais frequentes
     */
    @Query("SELECT numero, COUNT(*) as frequencia FROM (" +
           "SELECT primeiro as numero FROM resultados WHERE data_resultado BETWEEN :dataInicio AND :dataFim UNION ALL " +
           "SELECT segundo as numero FROM resultados WHERE data_resultado BETWEEN :dataInicio AND :dataFim UNION ALL " +
           "SELECT terceiro as numero FROM resultados WHERE data_resultado BETWEEN :dataInicio AND :dataFim UNION ALL " +
           "SELECT quarto as numero FROM resultados WHERE data_resultado BETWEEN :dataInicio AND :dataFim UNION ALL " +
           "SELECT quinto as numero FROM resultados WHERE data_resultado BETWEEN :dataInicio AND :dataFim UNION ALL " +
           "SELECT sexto as numero FROM resultados WHERE data_resultado BETWEEN :dataInicio AND :dataFim UNION ALL " +
           "SELECT setimo as numero FROM resultados WHERE data_resultado BETWEEN :dataInicio AND :dataFim" +
           ") AS numeros_periodo GROUP BY numero ORDER BY frequencia DESC LIMIT :limite")
    Flux<Object[]> findNumerosMaisSorteadosNoPeriodo(@Param("dataInicio") LocalDate dataInicio,
                                                     @Param("dataFim") LocalDate dataFim,
                                                     @Param("limite") Integer limite);

    /**
     * Conta total de resultados
     * @return Mono com contagem total
     */
    @Query("SELECT COUNT(*) FROM resultados")
    Mono<Long> countTotal();

    /**
     * Busca resultado mais antigo
     * @return Mono com resultado mais antigo
     */
    @Query("SELECT * FROM resultados ORDER BY data_resultado ASC, horario ASC LIMIT 1")
    Mono<ResultadoR2dbc> findResultadoMaisAntigo();

    /**
     * Busca resultado mais recente
     * @return Mono com resultado mais recente
     */
    @Query("SELECT * FROM resultados ORDER BY data_resultado DESC, horario DESC LIMIT 1")
    Mono<ResultadoR2dbc> findResultadoMaisRecente();

    /**
     * Busca todos os resultados paginados
     * @param pageable Paginação
     * @return Flux com todos os resultados
     */
    @Query("SELECT * FROM resultados ORDER BY data_resultado DESC, horario")
    Flux<ResultadoR2dbc> findAllPaginado(Pageable pageable);

    // Métodos adicionais para busca por data
    Flux<ResultadoR2dbc> findByDataResultadoAfter(LocalDate dataResultado, Pageable pageable);
    Mono<Long> countByDataResultadoAfter(LocalDate dataResultado);
}