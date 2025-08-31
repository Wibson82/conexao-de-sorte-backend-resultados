package br.tec.facilitaservicos.resultados.apresentacao.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EstatisticasDtoTest {

    @Test
    void deveCriarBasicas() {
        var e = EstatisticasDto.basicas(100L, 700L);
        assertEquals(100L, e.totalResultados());
        assertEquals(700L, e.totalSorteios());
        assertFalse(e.temPeriodo());
    }

    @Test
    void deveCriarCompletasECalcularMedia() {
        var inicio = LocalDate.of(2024,1,1);
        var fim = LocalDate.of(2024,12,31);
        var mais = List.of(RankingDto.criar("07", 10L));
        var menos = List.of(RankingDto.criar("13", 1L));
        var e = EstatisticasDto.completas(10L, 70L, inicio, fim, mais, menos, List.of("10:00"));
        assertTrue(e.temPeriodo());
        assertTrue(e.temRankings());
        assertEquals(7.0, e.mediaNumerosPorSorteio());
    }
}

