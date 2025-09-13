package br.tec.facilitaservicos.resultados.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;

class ConfigtreeSecretsIntegrationTest extends BaseIntegracao {

    @Autowired
    private R2dbcProperties r2dbcProperties;

    @Autowired
    private RedisProperties redisProperties;

    @Test
    @DisplayName("R2DBC properties must be configured from Testcontainers MySQL")
    void r2dbcPropertiesAreConfigured() {
        assertThat(r2dbcProperties.getUrl())
                .as("R2DBC URL should be loaded from Testcontainers")
                .isNotBlank()
                .startsWith("r2dbc:mysql://");

        assertThat(r2dbcProperties.getUsername())
                .as("DB username from Testcontainers")
                .isEqualTo("testuser");

        assertThat(r2dbcProperties.getPassword())
                .as("DB password from Testcontainers")
                .isEqualTo("testpass");

        // Redis properties test removed since it's disabled in test profile
    }
}

