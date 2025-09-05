package br.tec.facilitaservicos.resultados.apresentacao.dto;

public record EtlSolicitacaoResponse(String jobId) {
    public static EtlSolicitacaoResponse of(String jobId) { return new EtlSolicitacaoResponse(jobId); }
}

