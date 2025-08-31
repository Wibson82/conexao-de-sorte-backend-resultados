package br.tec.facilitaservicos.resultados.apresentacao.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaginacaoDtoTest {

    @Test
    void deveCriarPaginacaoVazia() {
        PaginacaoDto<String> p = PaginacaoDto.vazia(0, 10);
        assertTrue(p.primeiro());
        assertTrue(p.ultimo());
        assertEquals(0, p.numeroElementos());
        assertFalse(p.temConteudo());
    }

    @Test
    void deveCriarPaginacaoComDados() {
        PaginacaoDto<String> p = PaginacaoDto.criar(List.of("a","b"), 1, 2, 5);
        assertEquals(1, p.pagina());
        assertEquals(2, p.tamanho());
        assertEquals(5, p.total());
        assertEquals(3, p.totalPaginas());
        assertFalse(p.primeiro());
        assertTrue(p.temConteudo());
        assertEquals(2, p.numeroElementos());
    }
}

