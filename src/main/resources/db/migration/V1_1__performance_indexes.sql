-- Índices de performance e unicidade para consultas críticas
-- Compatível com MySQL 8+

-- Deduplicação segura antes da UNIQUE (mantém o maior id)
DELETE r1 FROM resultados r1
INNER JOIN resultados r2
  ON r1.horario = r2.horario
 AND r1.data_resultado = r2.data_resultado
 AND r1.id < r2.id;

-- Evita duplicidade por horário e data
ALTER TABLE resultados
  ADD UNIQUE INDEX uk_resultados_horario_data (horario, data_resultado);

-- Suporte a ordenações e filtros mais usados
CREATE INDEX idx_resultados_data_horario ON resultados (data_resultado DESC, horario);
CREATE INDEX idx_resultados_horario_data ON resultados (horario, data_resultado DESC);

-- Suporte a busca por soma
CREATE INDEX idx_resultados_soma_data ON resultados (soma, data_resultado DESC);

-- Suporte a buscas por números (OR em múltiplas colunas)
CREATE INDEX idx_resultados_primeiro  ON resultados (primeiro);
CREATE INDEX idx_resultados_segundo   ON resultados (segundo);
CREATE INDEX idx_resultados_terceiro  ON resultados (terceiro);
CREATE INDEX idx_resultados_quarto    ON resultados (quarto);
CREATE INDEX idx_resultados_quinto    ON resultados (quinto);
CREATE INDEX idx_resultados_sexto     ON resultados (sexto);
CREATE INDEX idx_resultados_setimo    ON resultados (setimo);
