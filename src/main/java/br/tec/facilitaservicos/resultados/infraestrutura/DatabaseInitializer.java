package br.tec.facilitaservicos.resultados.infraestrutura;

// Não usar variáveis não utilizadas
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Inicializador reativo do banco de dados.
 * Garante que o banco e as tabelas estejam criados conforme o script init.sql.
 * Seguro para produção e compatível com Docker.
 */
@Configuration
public class DatabaseInitializer {
    private static final Logger log = Logger.getLogger(DatabaseInitializer.class.getName());
    private static final String INIT_SQL_PATH = "docker/mysql/init.sql";

    // Removido: variável não utilizada

    @Bean
    public ApplicationRunner databaseInitRunner(DatabaseClient client) {
        return applicationArgs -> {
            // Referencia os argumentos de inicialização para evitar warnings e permitir logs
            if (applicationArgs != null) {
                log.log(java.util.logging.Level.INFO, "DatabaseInitializer iniciado com argumentos: {0}", applicationArgs.getSourceArgs().length);
            }
            Mono.fromCallable(() -> {
                var resource = new ClassPathResource(INIT_SQL_PATH);
                if (!resource.exists()) {
                    log.warning("Script de inicialização não encontrado: " + INIT_SQL_PATH);
                    return null;
                }
                String sql = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                return sql;
            })
            .flatMapMany(sql -> {
                if (sql == null || sql.isBlank()) return Flux.empty();
                // Divide o script em comandos individuais por ponto e vírgula
                String[] comandos = sql.split(";\\s*(?:\\r?\\n)?");
                return Flux.fromArray(comandos);
            })
            .map(String::trim)
            .filter(comando -> !comando.isEmpty() && !comando.startsWith("--"))
                .concatMap(comando -> client.sql(comando).fetch().rowsUpdated()
                .doOnError(e -> log.log(java.util.logging.Level.WARNING, "Erro ao executar comando: {0} - {1}", new Object[]{comando, e.getMessage()}))
                .doOnSuccess(count -> log.log(java.util.logging.Level.INFO, "Comando executado: {0} - {1} linhas afetadas", new Object[]{comando, count}))
                .onErrorResume(e -> {
                    log.log(java.util.logging.Level.WARNING, "Recoverable error ao executar comando: {0} - {1}", new Object[]{comando, e.getMessage()});
                    return Mono.empty();
                })
            )
            .doOnError(e -> log.log(java.util.logging.Level.SEVERE, "Erro na inicialização do banco: {0}", e.getMessage()))
            .doOnComplete(() -> log.info("Inicialização do banco concluída."))
            .subscribe();
        };
    }
}