package br.tec.facilitaservicos.resultados.aplicacao.servico;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Map;

@Service
public class IntegracaoSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(IntegracaoSchedulerService.class);

    private final WebClient webClient;

    public IntegracaoSchedulerService(@Value("${scheduler.base-url:http://conexao-scheduler:8080}") String schedulerBaseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(schedulerBaseUrl)
                .build();
    }

    public Mono<String> dispararEtlLoterias(String modalidade, LocalDate data) {
        logger.info("Disparando ETL para modalidade={} data={}", modalidade, data);

        Map<String, Object> body = Map.of(
                "modalidade", modalidade,
                "data", data != null ? data.toString() : null
        );

        return webClient.post()
                .uri("/jobs/loterias/etl")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(Map.class)
                .map(resp -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> response = (Map<String, Object>) resp;
                    return String.valueOf(response.getOrDefault("jobId", ""));
                });
    }
}

