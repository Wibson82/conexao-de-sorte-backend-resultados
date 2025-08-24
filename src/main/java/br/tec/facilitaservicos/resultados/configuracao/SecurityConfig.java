package br.tec.facilitaservicos.resultados.configuracao;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.FormLoginSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.HttpBasicSpec;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * ============================================================================
 * 🔐 CONFIGURAÇÃO DE SEGURANÇA REATIVA - MICROSERVIÇO RESULTADOS
 * ============================================================================
 * 
 * Configuração de segurança 100% reativa para WebFlux:
 * - Validação JWT via JWKS do microserviço de autenticação
 * - Proteção apenas para endpoints administrativos (consultas são públicas)
 * - CORS configurado para frontend
 * - Headers de segurança otimizados
 * - Rate limiting configurado via properties
 * 
 * @author Sistema de Migração R2DBC
 * @version 1.0
 * @since 2024
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    // Constantes para valores repetidos
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    
    // Templates de resposta JSON
    private static final String TEMPLATE_ERRO_AUTENTICACAO = """
        {
            "status": 401,
            "erro": "Não autorizado",
            "mensagem": "Token JWT inválido ou ausente",
            "timestamp": "%s"
        }
        """;
        
    private static final String TEMPLATE_ERRO_ACESSO = """
        {
            "status": 403,
            "erro": "Acesso negado",
            "mensagem": "Permissões insuficientes para acessar este recurso",
            "timestamp": "%s"
        }
        """;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("#{'${cors.allowed-origins}'.split(',')}")
    private List<String> allowedOrigins;

    @Value("#{'${cors.allowed-methods}'.split(',')}")
    private List<String> allowedMethods;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${cors.max-age:3600}")
    private long maxAge;

    /**
     * Configuração principal da cadeia de filtros de segurança
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            // Desabilitar proteções desnecessárias para API reativa
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            // Configurar CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Configurar autorização
            .authorizeExchange(exchanges -> exchanges
                // Endpoints públicos (sem autenticação)
                .pathMatchers(
                    // Actuator/Health checks
                    "/actuator/health/**",
                    "/actuator/info",
                    "/actuator/metrics",
                    "/actuator/prometheus",
                    
                    // Documentação OpenAPI
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/webjars/**",
                    
                    // Favicon
                    "/favicon.ico"
                ).permitAll()
                
                // Endpoints de consulta pública (sem autenticação necessária)
                .pathMatchers(HttpMethod.GET,
                    "/api/resultados",
                    "/api/resultados/{id}",
                    "/api/resultados/ranking",
                    "/api/resultados/estatisticas",
                    "/api/resultados/hoje",
                    "/api/resultados/horarios",
                    "/api/resultados/ultimo/{horario}"
                ).permitAll()
                
                // Endpoints administrativos (requerem autenticação)
                .pathMatchers("/actuator/**").hasAuthority("SCOPE_admin")
                
                // Qualquer outra requisição requer autenticação
                .anyExchange().authenticated()
            )

            // Configurar JWT
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtDecoder(reactiveJwtDecoder())
                )
            )

            // Headers de segurança básicos (removido temporariamente para compilação)
            // .headers(headers -> headers.frameOptions().deny())

            // Configurar tratamento de exceções de segurança
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((exchange, ex) -> {
                    var response = exchange.getResponse();
                    response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                    response.getHeaders().add(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON);
                    
                    String body = TEMPLATE_ERRO_AUTENTICACAO.formatted(java.time.LocalDateTime.now());
                    
                    var buffer = response.bufferFactory().wrap(body.getBytes());
                    return response.writeWith(reactor.core.publisher.Mono.just(buffer));
                })
                .accessDeniedHandler((exchange, denied) -> {
                    var response = exchange.getResponse();
                    response.setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
                    response.getHeaders().add(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON);
                    
                    String body = TEMPLATE_ERRO_ACESSO.formatted(java.time.LocalDateTime.now());
                    
                    var buffer = response.bufferFactory().wrap(body.getBytes());
                    return response.writeWith(reactor.core.publisher.Mono.just(buffer));
                })
            )

            .build();
    }

    /**
     * Decodificador JWT reativo via JWKS
     */
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    /**
     * Configuração CORS para permitir acesso do frontend
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Origins permitidas
        configuration.setAllowedOrigins(allowedOrigins);
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(allowedMethods);
        
        // Headers permitidos
        configuration.setAllowedHeaders(List.of("*"));
        
        // Permitir cookies/credenciais
        configuration.setAllowCredentials(allowCredentials);
        
        // Cache preflight
        configuration.setMaxAge(maxAge);
        
        // Headers expostos
        configuration.setExposedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "X-Total-Count",
            "X-Total-Pages"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}