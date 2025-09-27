-- ============================================================================
-- 📊 SCRIPT DE INICIALIZAÇÃO - DATABASE RESULTADOS
-- ============================================================================
--
-- Criação da estrutura inicial do banco de dados para o microserviço
-- de resultados com otimizações para alta performance e consultas frequentes.
--
-- Inclui:
-- - Tabela de resultados otimizada
-- - Índices para consultas rápidas
-- - Usuário específico para a aplicação
-- - Configurações de performance
-- ============================================================================

-- Criar database se não existir
CREATE DATABASE IF NOT EXISTS conexao_de_sorte
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Usar o database
USE conexao_de_sorte;

-- Criar tabela de resultados
CREATE TABLE IF NOT EXISTS resultados (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    horario VARCHAR(5) NOT NULL COMMENT 'Horário do resultado (HH:mm)',
    primeiro VARCHAR(2) NOT NULL COMMENT 'Primeiro número sorteado',
    segundo VARCHAR(2) NOT NULL COMMENT 'Segundo número sorteado',
    terceiro VARCHAR(2) NOT NULL COMMENT 'Terceiro número sorteado',
    quarto VARCHAR(2) NOT NULL COMMENT 'Quarto número sorteado',
    quinto VARCHAR(2) NOT NULL COMMENT 'Quinto número sorteado',
    sexto VARCHAR(2) NOT NULL COMMENT 'Sexto número sorteado',
    setimo VARCHAR(2) NOT NULL COMMENT 'Sétimo número sorteado',
    soma VARCHAR(3) COMMENT 'Soma de todos os números',
    data_resultado DATE NOT NULL COMMENT 'Data do resultado',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Data de criação do registro',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Data da última modificação',

    -- Índices para performance
    INDEX idx_data_resultado (data_resultado DESC),
    INDEX idx_horario (horario),
    INDEX idx_data_horario (data_resultado DESC, horario),
    INDEX idx_soma (soma),

    -- Índices para busca por números específicos
    INDEX idx_primeiro (primeiro),
    INDEX idx_segundo (segundo),
    INDEX idx_terceiro (terceiro),
    INDEX idx_quarto (quarto),
    INDEX idx_quinto (quinto),
    INDEX idx_sexto (sexto),
    INDEX idx_setimo (setimo),

    -- Índice composto para consultas de período
    INDEX idx_periodo (data_resultado, horario, id),

    -- Constraint única para evitar duplicatas
    UNIQUE KEY uk_horario_data (horario, data_resultado)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Tabela de resultados de loteria - Microserviço Resultados';

-- Dados de exemplo para testes (opcional)
INSERT IGNORE INTO resultados (horario, primeiro, segundo, terceiro, quarto, quinto, sexto, setimo, soma, data_resultado) VALUES
('14:00', '01', '15', '23', '34', '45', '56', '67', '241', CURDATE()),
('18:00', '05', '12', '28', '33', '41', '52', '63', '234', CURDATE()),
('21:00', '03', '17', '25', '36', '44', '55', '61', '241', CURDATE()),
('14:00', '07', '18', '29', '32', '43', '54', '65', '248', DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
('18:00', '02', '14', '26', '35', '46', '57', '68', '248', DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
('21:00', '09', '16', '27', '31', '42', '53', '64', '242', DATE_SUB(CURDATE(), INTERVAL 1 DAY));

ALTER TABLE conexao_de_sorte.resultados
  ROW_FORMAT=DYNAMIC,
  STATS_PERSISTENT=1,
  STATS_AUTO_RECALC=1;

-- Analisar tabela para otimizar estatísticas
ANALYZE TABLE conexao_de_sorte.resultados;

-- Criar usuário específico para a aplicação (se não existir)
-- A senha deve ser fornecida via variável de ambiente (ex.: DB_PASSWORD)
-- NOTA DE SEGURANÇA: criação/alteração de usuários e senhas NÃO deve
-- ser feita por este script. As credenciais e a criação/gestão de usuários
-- deverão ser realizadas fora do código da aplicação (por exemplo, via
-- pipeline CI/CD, Docker Secrets ou administração do banco).
--
-- Removemos as operações de CREATE USER / GRANT / FLUSH do script para
-- cumprir a política de segurança: NUNCA alterar usuário/senha a partir
-- do código do projeto. Se for necessário que este script se conecte ao
-- servidor MySQL para criar o banco/tabelas, as credenciais deverão ser
-- providas ao container via Docker Secrets (fornecidos pelo workflow) e
-- não devem ser modificadas pelo script.

-- Log da inicialização
SELECT 'Database resultados inicializado com sucesso!' as status;
