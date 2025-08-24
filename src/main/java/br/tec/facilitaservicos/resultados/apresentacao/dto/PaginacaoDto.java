package br.tec.facilitaservicos.resultados.apresentacao.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO reativo para resposta paginada
 * 
 * @param conteudo Lista de itens da página atual
 * @param pagina Número da página atual (0-based)
 * @param tamanho Tamanho da página
 * @param total Total de elementos
 * @param totalPaginas Total de páginas
 * @param primeiro Se é a primeira página
 * @param ultimo Se é a última página
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaginacaoDto<T>(
    List<T> conteudo,
    int pagina,
    int tamanho,
    long total,
    int totalPaginas,
    boolean primeiro,
    boolean ultimo
) {
    
    /**
     * Cria resposta paginada vazia
     */
    public static <T> PaginacaoDto<T> vazia(int pagina, int tamanho) {
        return new PaginacaoDto<>(
            List.of(),
            pagina,
            tamanho,
            0L,
            0,
            true,
            true
        );
    }
    
    /**
     * Cria resposta paginada com dados
     */
    public static <T> PaginacaoDto<T> criar(List<T> conteudo, int pagina, int tamanho, long total) {
        int totalPaginas = (int) Math.ceil((double) total / tamanho);
        boolean primeiro = pagina == 0;
        boolean ultimo = pagina >= totalPaginas - 1;
        
        return new PaginacaoDto<>(
            conteudo,
            pagina,
            tamanho,
            total,
            Math.max(totalPaginas, 1),
            primeiro,
            ultimo
        );
    }
    
    /**
     * Verifica se tem conteúdo
     */
    public boolean temConteudo() {
        return conteudo != null && !conteudo.isEmpty();
    }
    
    /**
     * Obtém número de elementos na página atual
     */
    public int numeroElementos() {
        return conteudo != null ? conteudo.size() : 0;
    }
}