package br.tec.facilitaservicos.resultados.aplicacao.servico;

import org.springframework.stereotype.Service;

import java.time.*;

@Service
public class ServicoDataHoraAtual {
    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    public LocalDate dataHoje() {
        return LocalDate.now(ZONE_ID);
    }

    public LocalDateTime agora() {
        return LocalDateTime.now(ZONE_ID);
    }

    public ZonedDateTime agoraZoned() {
        return ZonedDateTime.now(ZONE_ID);
    }
}

