package br.tec.facilitaservicos.resultados.apresentacao.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO reativo para ranking de números
 * 
 * @param numero Número sorteado
 * @param frequencia Quantidade de vezes sorteado
 * @param percentual Percentual de frequência
 * @param posicao Posição no ranking
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RankingDto(
    String numero,
    Long frequencia,
    Double percentual,
    Integer posicao
) {
    
    /**
     * Cria ranking básico
     */
    public static RankingDto criar(String numero, Long frequencia) {
        return new RankingDto(numero, frequencia, null, null);
    }
    
    /**
     * Cria ranking completo
     */
    public static RankingDto completo(String numero, Long frequencia, Double percentual, Integer posicao) {
        return new RankingDto(numero, frequencia, percentual, posicao);
    }
    
    /**
     * Calcula percentual baseado no total
     */
    public RankingDto comPercentual(Long totalSorteios) {
        if (totalSorteios == null || totalSorteios == 0) {
            return this;
        }
        
        double pct = (frequencia.doubleValue() / totalSorteios.doubleValue()) * 100.0;
        return new RankingDto(numero, frequencia, Math.round(pct * 100.0) / 100.0, posicao);
    }
    
    /**
     * Adiciona posição no ranking
     */
    public RankingDto comPosicao(Integer posicao) {
        return new RankingDto(numero, frequencia, percentual, posicao);
    }
}