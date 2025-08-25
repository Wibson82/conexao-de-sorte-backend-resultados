package br.tec.facilitaservicos.resultados;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

import br.tec.facilitaservicos.resultados.configuracao.AplicacaoProperties;

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
 * - Azure Key Vault para gerenciamento de secrets
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
@EnableCaching
@EnableR2dbcAuditing
@EnableConfigurationProperties(AplicacaoProperties.class)
public class ResultadosApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ResultadosApplication.class, args);
    }
}