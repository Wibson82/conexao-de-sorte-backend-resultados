package br.tec.facilitaservicos.resultados;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/**
 * ============================================================================
 * 📊 APLICAÇÃO PRINCIPAL - MICROSERVIÇO RESULTADOS
 * ============================================================================
 * 
 * Microserviço de resultados de loteria 100% reativo usando:
 * - Spring Boot 3.5+
 * - WebFlux (reativo)
 * - R2DBC (reativo)
 * - Spring Security reativo com JWT
 * - Paginação otimizada para alta cardinalidade
 * - Cache inteligente para consultas frequentes
 * - Rate limiting por endpoint
 * 
 * Endpoints:
 * - GET /api/resultados - Buscar resultados paginados
 * - GET /api/resultados/{id} - Buscar resultado específico
 * - GET /api/resultados/ranking - Ranking com filtros
 * - GET /api/resultados/estatisticas - Estatísticas agregadas
 * 
 * ============================================================================
 */
@SpringBootApplication
@EnableR2dbcRepositories
@EnableR2dbcAuditing
public class ResultadosApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ResultadosApplication.class, args);
    }
}