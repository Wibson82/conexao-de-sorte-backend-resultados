package br.tec.facilitaservicos.resultados.configuracao;

import java.time.LocalDate;

import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;

import br.tec.facilitaservicos.resultados.dominio.repositorio.RepositorioResultadoR2dbc;
import reactor.core.publisher.Flux;

/**
 * Autoconfiguration para testes que fornece beans essenciais do R2DBC
 * quando os testes são executados com o perfil "test".
 *
 * Resolve o problema de carregamento de contexto para testes que dependem
 * de R2dbcProperties sem precisar das propriedades reais do Azure Key Vault.
 */
@AutoConfiguration
@Profile("test")
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "test")
public class TestConfigtreeSecretsAutoConfiguration {

    /**
     * Fornece R2dbcProperties para testes, com valores padrão para teste
     * sem depender do configtree ou do Azure Key Vault.
     *
     * A anotação @Primary garante que este bean tenha precedência sobre
     * outros beans de R2dbcProperties que podem ser criados automaticamente.
     *
     * @return R2dbcProperties configurado para testes
     */
    @Bean
    @Primary
    public R2dbcProperties r2dbcProperties() {
        R2dbcProperties properties = new R2dbcProperties();
        properties.setUrl("r2dbc:mysql://localhost:3306/test");
        properties.setUsername("test");
        properties.setPassword("test");
        return properties;
    }

    /**
     * Cria um mock do RepositorioResultadoR2dbc para testes.
     * Isso evita erros relacionados a propriedades inexistentes nas entidades
     * ou problemas de conexão com o banco de dados durante os testes.
     *
     * @return Mock do RepositorioResultadoR2dbc
     */
    @Bean
    @Primary
    public RepositorioResultadoR2dbc repositorioResultadoR2dbc() {
        RepositorioResultadoR2dbc mockRepo = mock(RepositorioResultadoR2dbc.class);

        // Configurações padrão para os métodos mais comuns
        Mockito.when(mockRepo.findAll()).thenReturn(Flux.empty());
        Mockito.when(mockRepo.findAllPaginado(Mockito.any(Pageable.class))).thenReturn(Flux.empty());
        Mockito.when(mockRepo.findByDataResultadoAfter(
            Mockito.any(LocalDate.class), Mockito.any(Pageable.class)))
            .thenReturn(Flux.empty());

        return mockRepo;
    }
}