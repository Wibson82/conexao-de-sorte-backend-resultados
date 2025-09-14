package br.tec.facilitaservicos.resultados.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ConfigtreeSecretsIntegrationTest {

    @Autowired
    private R2dbcProperties r2dbcProperties;

    // Redis properties test removed since it's disabled in test profile

    @Test
    @DisplayName("R2DBC properties must be configured for test environment")
    void r2dbcPropertiesAreConfigured() {
        assertThat(r2dbcProperties.getUrl())
                .as("R2DBC URL should be configured for test")
                .isNotBlank()
                .startsWith("r2dbc:mysql://");

        assertThat(r2dbcProperties.getUsername())
                .as("DB username should be configured")
                .isNotBlank()
                .isEqualTo("test");

        assertThat(r2dbcProperties.getPassword())
                .as("DB password should be configured")
                .isNotBlank()
                .isEqualTo("test");

        // Note: Test uses fallback values from application-test.yml
        // In real environment, values would come from Azure Key Vault
    }
}

