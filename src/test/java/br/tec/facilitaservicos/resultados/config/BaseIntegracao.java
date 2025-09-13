package br.tec.facilitaservicos.resultados.config;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.MySQLContainer;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * Classe base para testes de integração com MySQL via Testcontainers
 * 
 * Fornece um container MySQL configurado automaticamente e registra
 * as propriedades dinâmicas do R2DBC para conexão com o banco de teste.
 */
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
public abstract class BaseIntegracao {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
        .withDatabaseName("testdb")
        .withUsername("testuser")
        .withPassword("testpass");

    @DynamicPropertySource
    static void registrarPropriedades(final DynamicPropertyRegistry r) {
        r.add("spring.r2dbc.url", () ->
            "r2dbc:mysql://" + mysql.getHost() + ":" + mysql.getFirstMappedPort() + "/" + mysql.getDatabaseName()
        );
        r.add("spring.r2dbc.username", mysql::getUsername);
        r.add("spring.r2dbc.password", mysql::getPassword);

        // Desabilitar migração automática em testes
        r.add("spring.flyway.enabled", () -> false);
        r.add("spring.sql.init.mode", () -> "never");
        
        // Desabilitar repositórios Redis em testes
        r.add("spring.data.redis.repositories.enabled", () -> false);
    }
}