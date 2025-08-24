package br.tec.facilitaservicos.resultados.apresentacao.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO reativo para estatísticas de resultados
 * 
 * @param totalResultados Total de resultados no sistema
 * @param totalSorteios Total de números sorteados
 * @param periodoInicio Data do primeiro resultado
 * @param periodoFim Data do último resultado
 * @param numerosMaisFrequentes Top números mais sorteados
 * @param numerosMenosFrequentes Top números menos sorteados
 * @param horariosAtivos Horários com resultados
 * @param mediaNumerosPorSorteio Média de números por sorteio
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EstatisticasDto(
    Long totalResultados,
    Long totalSorteios,
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate periodoInicio,
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate periodoFim,
    
    List<RankingDto> numerosMaisFrequentes,
    List<RankingDto> numerosMenosFrequentes,
    List<String> horariosAtivos,
    Double mediaNumerosPorSorteio
) {
    
    /**
     * Cria estatísticas básicas
     */
    public static EstatisticasDto basicas(Long totalResultados, Long totalSorteios) {
        return new EstatisticasDto(
            totalResultados,
            totalSorteios,
            null, null, null, null, null, null
        );
    }
    
    /**
     * Cria estatísticas completas
     */
    public static EstatisticasDto completas(
            Long totalResultados,
            Long totalSorteios,
            LocalDate periodoInicio,
            LocalDate periodoFim,
            List<RankingDto> numerosMaisFrequentes,
            List<RankingDto> numerosMenosFrequentes,
            List<String> horariosAtivos
    ) {
        Double media = totalResultados > 0 ? totalSorteios.doubleValue() / totalResultados.doubleValue() : 0.0;
        
        return new EstatisticasDto(
            totalResultados,
            totalSorteios,
            periodoInicio,
            periodoFim,
            numerosMaisFrequentes,
            numerosMenosFrequentes,
            horariosAtivos,
            Math.round(media * 100.0) / 100.0
        );
    }
    
    /**
     * Verifica se tem dados do período
     */
    public boolean temPeriodo() {
        return periodoInicio != null && periodoFim != null;
    }
    
    /**
     * Verifica se tem rankings
     */
    public boolean temRankings() {
        return (numerosMaisFrequentes != null && !numerosMaisFrequentes.isEmpty()) ||
               (numerosMenosFrequentes != null && !numerosMenosFrequentes.isEmpty());
    }
}