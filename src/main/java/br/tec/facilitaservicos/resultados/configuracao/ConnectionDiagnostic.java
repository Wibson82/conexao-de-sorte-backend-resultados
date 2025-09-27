package br.tec.facilitaservicos.resultados.configuracao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.mysql.MySqlConnectionFactoryProvider;

import java.time.Duration;

/**
 * ============================================================================
 * 🔍 DIAGNÓSTICO DE CONECTIVIDADE R2DBC
 * ============================================================================
 *
 * Componente para diagnosticar problemas de conectividade com MySQL
 * em ambiente Docker Swarm. Fornece logs detalhados para debug.
 */
@Component
public class ConnectionDiagnostic {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionDiagnostic.class);

    @Value("${spring.r2dbc.url}")
    private String r2dbcUrl;

    @Value("${spring.r2dbc.username}")
    private String username;

    @Value("${spring.r2dbc.password:}")
    private String password;

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        logger.info("🔍 === DIAGNÓSTICO DE CONECTIVIDADE R2DBC ===");

        // Log configurações (mascarar senha)
        logger.info("📋 Configurações de conexão:");
        logger.info("   URL: {}", r2dbcUrl);
        logger.info("   Username: {}", username);
        logger.info("   Password: {}", password != null && !password.isEmpty() ? "***configurado***" : "***VAZIO***");

        // Debug dos secrets
        logEnvironmentSecrets(event);

        // Testar conectividade
        testConnection();
    }

    private void logEnvironmentSecrets(ApplicationReadyEvent event) {
        var env = event.getApplicationContext().getEnvironment();

        logger.info("🔍 Debug de secrets e propriedades:");

        // Verificar propriedades específicas
        String[] propsToCheck = {
            "spring.r2dbc.url",
            "spring.r2dbc.username",
            "spring.r2dbc.password",
            "conexao-de-sorte-database-r2dbc-url",
            "conexao-de-sorte-database-username",
            "conexao-de-sorte-database-password"
        };

        for (String prop : propsToCheck) {
            String value = env.getProperty(prop);
            if (prop.contains("password") && value != null && !value.isEmpty()) {
                logger.info("   {}: ***configurado***", prop);
            } else {
                logger.info("   {}: {}", prop, value != null ? value : "***NULL***");
            }
        }
    }

    private void testConnection() {
        try {
            logger.info("🔌 Testando conectividade R2DBC...");

            // Criar ConnectionFactory manualmente para teste
            ConnectionFactoryOptions options = ConnectionFactoryOptions.parse(r2dbcUrl)
                .mutate()
                .option(ConnectionFactoryOptions.USER, username)
                .option(ConnectionFactoryOptions.PASSWORD, password != null ? password : "")
                .build();

            ConnectionFactory factory = new MySqlConnectionFactoryProvider().create(options);

            // Teste de conexão com timeout
            Mono.from(factory.create())
                .timeout(Duration.ofSeconds(10))
                .doOnNext(connection -> {
                    logger.info("✅ CONECTIVIDADE OK - Conexão estabelecida!");
                    // Fechar conexão
                    Mono.from(connection.close()).subscribe();
                })
                .doOnError(error -> {
                    logger.error("❌ FALHA NA CONECTIVIDADE:");
                    logger.error("   Tipo: {}", error.getClass().getSimpleName());
                    logger.error("   Mensagem: {}", error.getMessage());

                    // Análise detalhada do erro
                    analyzeError(error);
                })
                .subscribe();

        } catch (Exception e) {
            logger.error("💥 ERRO CRÍTICO no diagnóstico:", e);
        }
    }

    private void analyzeError(Throwable error) {
        String message = error.getMessage();

        if (message != null) {
            if (message.contains("Connection refused")) {
                logger.error("🚫 ANÁLISE: MySQL não está acessível na rede");
                logger.error("   ➤ Verificar se MySQL container está rodando");
                logger.error("   ➤ Verificar rede Docker Swarm 'conexao-network-swarm'");
                logger.error("   ➤ Verificar hostname 'conexao-mysql'");
            } else if (message.contains("Access denied")) {
                logger.error("🔐 ANÁLISE: Credenciais incorretas");
                logger.error("   ➤ Verificar secret 'conexao-de-sorte-database-username'");
                logger.error("   ➤ Verificar secret 'conexao-de-sorte-database-password'");
            } else if (message.contains("Unknown database")) {
                logger.error("🗄️ ANÁLISE: Database não existe");
                logger.error("   ➤ Database 'conexao_de_sorte' não foi criado");
                logger.error("   ➤ Verificar parâmetro 'createDatabaseIfNotExist=true'");
            } else if (message.contains("timeout")) {
                logger.error("⏱️ ANÁLISE: Timeout de conexão");
                logger.error("   ➤ Rede muito lenta ou MySQL sobrecarregado");
            } else {
                logger.error("❓ ANÁLISE: Erro desconhecido - {}", message);
            }
        }
    }
}