package br.tec.facilitaservicos.resultados.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        // Ensure our local secrets folder is imported as a configtree source
        "spring.config.import=optional:configtree:./run/secrets/"
})
class ConfigtreeSecretsIntegrationTest {

    @Autowired
    private R2dbcProperties r2dbcProperties;

    @Autowired
    private RedisProperties redisProperties;

    @Test
    @DisplayName("Configtree secrets must bind to R2DBC and Redis properties")
    void configtreeSecretsAreBound() {
        assertThat(r2dbcProperties.getUrl())
                .as("R2DBC URL should be loaded from configtree")
                .isNotBlank()
                .startsWith("r2dbc:mysql://");

        assertThat(r2dbcProperties.getUsername())
                .as("DB username from configtree")
                .isEqualTo("testuser");

        assertThat(r2dbcProperties.getPassword())
                .as("DB password from configtree")
                .isEqualTo("testpass");

        assertThat(redisProperties.getHost()).isEqualTo("127.0.0.1");
        assertThat(redisProperties.getPort()).isEqualTo(6380);
        assertThat(redisProperties.getPassword()).isEqualTo("redispass");
    }
}

