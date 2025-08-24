package br.tec.facilitaservicos.resultados.configuracao;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * ============================================================================
 * 🚀 CONFIGURAÇÃO DE CACHE REATIVO - MICROSERVIÇO RESULTADOS  
 * ============================================================================
 * 
 * Configuração de cache híbrido:
 * - Cache local (Caffeine) para consultas frequentes
 * - Cache distribuído (Redis) para compartilhamento entre instâncias
 * - TTL configurável por tipo de cache
 * - Fallback automático em caso de erro
 * 
 * @author Sistema de Migração R2DBC
 * @version 1.0
 * @since 2024
 */
@Configuration
@ConditionalOnProperty(name = "features.statistics-cache", havingValue = "true", matchIfMissing = true)
public class CacheConfig {

    @Value("${cache.resultados.ttl:300}")
    private int resultadosTtl;

    @Value("${cache.ranking.ttl:900}")
    private int rankingTtl;

    @Value("${cache.estatisticas.ttl:1800}")
    private int estatisticasTtl;

    /**
     * Template reativo do Redis
     */
    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.host")
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        
        RedisSerializationContext<String, Object> serializationContext = 
            RedisSerializationContext.<String, Object>newSerializationContext()
                .key(new StringRedisSerializer())
                .value(new GenericJackson2JsonRedisSerializer())
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }

    /**
     * Cache local para resultados
     */
    @Bean("cacheResultados")
    public Cache<String, Object> cacheResultados() {
        return Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofSeconds(resultadosTtl))
            .recordStats()
            .build();
    }

    /**
     * Cache local para rankings
     */
    @Bean("cacheRanking")
    public Cache<String, Object> cacheRanking() {
        return Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(Duration.ofSeconds(rankingTtl))
            .recordStats()
            .build();
    }

    /**
     * Cache local para estatísticas
     */
    @Bean("cacheEstatisticas")
    public Cache<String, Object> cacheEstatisticas() {
        return Caffeine.newBuilder()
            .maximumSize(50)
            .expireAfterWrite(Duration.ofSeconds(estatisticasTtl))
            .recordStats()
            .build();
    }
}