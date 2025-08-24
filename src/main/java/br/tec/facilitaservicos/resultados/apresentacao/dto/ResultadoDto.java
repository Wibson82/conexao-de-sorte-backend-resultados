package br.tec.facilitaservicos.resultados.apresentacao.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO reativo para resposta de Resultado
 * 
 * @param id Identificador único
 * @param horario Horário do resultado
 * @param numeros Lista de números sorteados
 * @param soma Soma dos números
 * @param dataResultado Data do resultado
 * @param dataCriacao Data de criação do registro
 * @param dataModificacao Data da última modificação
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResultadoDto(
    Long id,
    
    @NotBlank(message = "Horário é obrigatório")
    String horario,
    
    @NotNull(message = "Números são obrigatórios")
    List<String> numeros,
    
    String soma,
    
    @NotNull(message = "Data do resultado é obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dataResultado,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime dataCriacao,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime dataModificacao
) {
    
    /**
     * Cria DTO com dados básicos
     */
    public static ResultadoDto criar(String horario, List<String> numeros, LocalDate dataResultado) {
        return new ResultadoDto(null, horario, numeros, null, dataResultado, null, null);
    }
    
    /**
     * Cria DTO completo
     */
    public static ResultadoDto completo(Long id, String horario, List<String> numeros, String soma,
                                       LocalDate dataResultado, LocalDateTime dataCriacao, LocalDateTime dataModificacao) {
        return new ResultadoDto(id, horario, numeros, soma, dataResultado, dataCriacao, dataModificacao);
    }
    
    /**
     * Verifica se tem todos os números necessários
     */
    public boolean numerosCompletos() {
        return numeros != null && numeros.size() == 7;
    }
    
    /**
     * Obtém representação textual dos números
     */
    public String numerosTexto() {
        return numeros != null ? String.join("-", numeros) : "";
    }
    
    /**
     * Verifica se é resultado de hoje
     */
    public boolean isHoje() {
        return dataResultado != null && dataResultado.equals(LocalDate.now());
    }
}