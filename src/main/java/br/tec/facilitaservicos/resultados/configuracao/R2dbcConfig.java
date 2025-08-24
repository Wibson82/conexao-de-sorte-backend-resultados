package br.tec.facilitaservicos.resultados.configuracao;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/**
 * ============================================================================
 * 🗄️ CONFIGURAÇÃO R2DBC REATIVA - MICROSERVIÇO RESULTADOS
 * ============================================================================
 * 
 * Configuração do R2DBC para acesso reativo ao banco de dados:
 * - Auditoria automática habilitada
 * - Repositories reativos habilitados
 * - Configuração de pool e conexões via application.yml
 * 
 * @author Sistema de Migração R2DBC
 * @version 1.0
 * @since 2024
 */
@Configuration
@EnableR2dbcRepositories(basePackages = "br.tec.facilitaservicos.resultados.dominio.repositorio")
@EnableR2dbcAuditing
public class R2dbcConfig {
    // Configurações adicionais via application.yml
}