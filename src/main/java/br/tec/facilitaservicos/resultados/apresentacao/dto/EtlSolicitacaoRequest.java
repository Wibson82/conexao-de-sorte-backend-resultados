package br.tec.facilitaservicos.resultados.apresentacao.dto;

import jakarta.validation.constraints.NotBlank;

public record EtlSolicitacaoRequest(
        @NotBlank String modalidade,
        String data // yyyy-MM-dd opcional
) {}

