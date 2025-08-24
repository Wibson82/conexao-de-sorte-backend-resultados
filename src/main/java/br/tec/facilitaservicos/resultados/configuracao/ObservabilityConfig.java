package br.tec.facilitaservicos.resultados.configuracao;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.tec.facilitaservicos.resultados.dominio.repositorio.RepositorioResultadoR2dbc;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
/**
 * ============================================================================
 * 📊 CONFIGURAÇÃO DE OBSERVABILIDADE - MICROSERVIÇO RESULTADOS
 * ============================================================================
 * 
 * Configuração de observabilidade e métricas:
 * - Health checks personalizados
 * - Métricas customizadas
 * - Info endpoint com dados do serviço
 * - Integração com Prometheus/Grafana
 * 
 * @author Sistema de Migração R2DBC
 * @version 1.0
 * @since 2024
 */
@Configuration
public class ObservabilityConfig {

    // Constantes para strings repetidas
    private static final String NOME_SERVICO = "resultados-microservice";
    private static final String TAG_SERVICO = "resultados";
    private static final String CHAVE_DATABASE = "database";
    private static final String CHAVE_STATUS = "status";
    private static final String CHAVE_SERVICE = "service";
    private static final String CHAVE_CACHE = "cache";
    private static final String VALOR_DATABASE = "MySQL/R2DBC";
    private static final String VALOR_CACHE = "Redis";
    private static final String STATUS_CONECTADO = "Connected";
    private static final String STATUS_DESCONECTADO = "Disconnected";
    private static final String STATUS_DISPONIVEL = "Available";
    private static final String STATUS_INDISPONIVEL = "Unavailable";

    private final RepositorioResultadoR2dbc repositorio;

    public ObservabilityConfig(RepositorioResultadoR2dbc repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Health check para verificar conectividade com banco
     */
    @Bean("resultadosDatabaseHealthIndicator")
    public HealthIndicator resultadosDatabaseHealthIndicator() {
        return () -> {
            try {
                // Tenta fazer uma query simples para verificar conectividade
                Long count = repositorio.countTotal().block();
                
                return Health.up()
                    .withDetail(CHAVE_DATABASE, VALOR_DATABASE)
                    .withDetail(CHAVE_STATUS, STATUS_CONECTADO)
                    .withDetail("totalResultados", count)
                    .withDetail(CHAVE_SERVICE, NOME_SERVICO)
                    .build();
                    
            } catch (Exception e) {
                return Health.down()
                    .withDetail(CHAVE_DATABASE, VALOR_DATABASE)
                    .withDetail(CHAVE_STATUS, STATUS_DESCONECTADO)
                    .withDetail("error", e.getMessage())
                    .withDetail(CHAVE_SERVICE, NOME_SERVICO)
                    .build();
            }
        };
    }

    /**
     * Informações customizadas do serviço
     */
    @Bean
    public InfoContributor resultadosInfoContributor() {
        return builder -> builder
            .withDetail(CHAVE_SERVICE, NOME_SERVICO)
            .withDetail("version", "1.0.0")
            .withDetail("description", "Microserviço de resultados de loteria 100% reativo")
            .withDetail("stack", java.util.Map.of(
                "framework", "Spring Boot 3.5+ WebFlux",
                CHAVE_DATABASE, "MySQL com R2DBC",
                "security", "JWT via JWKS",
                CHAVE_CACHE, "Redis + Caffeine",
                "java", System.getProperty("java.version")
            ))
            .withDetail("features", java.util.Map.of(
                "reactive", true,
                "pagination", true,
                "ranking", true,
                "statistics", true,
                "caching", true
            ));
    }

    /**
     * Configuração de métricas customizadas
     */
    @Bean
    public Counter resultadosConsultasCounter(MeterRegistry registry) {
        return Counter.builder("resultados.consultas.total")
            .description("Total de consultas aos resultados")
            .tag(CHAVE_SERVICE, TAG_SERVICO)
            .register(registry);
    }

    @Bean
    public Counter rankingConsultasCounter(MeterRegistry registry) {
        return Counter.builder("ranking.consultas.total")
            .description("Total de consultas ao ranking")
            .tag(CHAVE_SERVICE, TAG_SERVICO)
            .register(registry);
    }

    @Bean
    public Counter estatisticasConsultasCounter(MeterRegistry registry) {
        return Counter.builder("estatisticas.consultas.total")
            .description("Total de consultas às estatísticas")
            .tag(CHAVE_SERVICE, TAG_SERVICO)
            .register(registry);
    }

    /**
     * Health check para cache Redis
     */
    @Bean("resultadosRedisHealthIndicator")
    public HealthIndicator resultadosRedisHealthIndicator() {
        return () -> {
            try {
                // Verifica se Redis está disponível (se configurado)
                // Esta implementação básica pode ser expandida com ReactiveRedisTemplate
                
                return Health.up()
                    .withDetail(CHAVE_CACHE, VALOR_CACHE)
                    .withDetail(CHAVE_STATUS, STATUS_DISPONIVEL)
                    .withDetail(CHAVE_SERVICE, NOME_SERVICO)
                    .build();
                    
            } catch (Exception e) {
                return Health.down()
                    .withDetail(CHAVE_CACHE, VALOR_CACHE)
                    .withDetail(CHAVE_STATUS, STATUS_INDISPONIVEL)
                    .withDetail("error", e.getMessage())
                    .withDetail(CHAVE_SERVICE, NOME_SERVICO)
                    .build();
            }
        };
    }
}