package br.tec.facilitaservicos.resultados.aplicacao.mapper;

import br.tec.facilitaservicos.resultados.apresentacao.dto.ResultadoDto;
import br.tec.facilitaservicos.resultados.dominio.entidade.ResultadoR2dbc;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultadoMapperTest {

    @Test
    void deveConverterEntidadeParaDtoEVoltar() {
        ResultadoMapper mapper = new ResultadoMapper();

        ResultadoR2dbc entidade = new ResultadoR2dbc.Builder()
                .horario("12:00")
                .primeiro("01").segundo("02").terceiro("03")
                .quarto("04").quinto("05").sexto("06").setimo("07")
                .soma("28")
                .dataResultado(LocalDate.now())
                .dataCriacao(LocalDateTime.now())
                .dataModificacao(LocalDateTime.now())
                .build();

        ResultadoDto dto = mapper.paraDto(entidade);
        assertEquals("12:00", dto.horario());
        assertEquals(7, dto.numeros().size());
        assertEquals("28", dto.soma());

        ResultadoR2dbc back = mapper.paraEntidade(dto);
        assertEquals(dto.horario(), back.getHorario());
        assertEquals(dto.dataResultado(), back.getDataResultado());
    }
}

