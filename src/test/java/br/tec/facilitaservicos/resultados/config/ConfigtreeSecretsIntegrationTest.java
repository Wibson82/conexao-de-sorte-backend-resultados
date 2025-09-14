package br.tec.facilitaservicos.resultados.config;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import br.tec.facilitaservicos.resultados.configuracao.TestConfigtreeSecretsAutoConfiguration;

/**
 * Testes de integração para validar o carregamento de secrets via configtree
 * usando o modo de fallback com valores específicos para testes.
 */
@SpringBootTest(classes = ConfigtreeSecretsIntegrationTest.TestConfiguration.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.profiles.active=test",
    "azure.keyvault.enabled=false"
})
class ConfigtreeSecretsIntegrationTest {

    @Autowired
    private R2dbcProperties r2dbcProperties;

    /**
     * Configuração específica para este teste que desabilita o carregamento de repositórios
     * e usa apenas o básico necessário para testar o carregamento das propriedades do R2DBC.
     */
    @EnableAutoConfiguration
    @Import(TestConfigtreeSecretsAutoConfiguration.class)
    static class TestConfiguration {
        // Classe de configuração vazia para carregar apenas o que precisamos
    }

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

        // Note: Test uses fallback values from TestConfigtreeSecretsAutoConfiguration
        // In real environment, values would come from Azure Key Vault
    }
}

