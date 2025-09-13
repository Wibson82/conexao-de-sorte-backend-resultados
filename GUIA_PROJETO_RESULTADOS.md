# 🏆 Guia do Projeto: Resultados
## Microserviço de Jogos e Sorteios

> **🎯 Contexto**: Microserviço responsável pelos resultados de jogos, sorteios, ranking de usuários e distribuição de prêmios na plataforma.

---

## 📋 INFORMAÇÕES DO PROJETO

### **Identificação:**
- **Nome**: conexao-de-sorte-backend-resultados
- **Porta**: 8083
- **Rede Principal**: conexao-network-swarm
- **Database**: conexao_sorte_resultados (MySQL 8.4)
- **Runner**: `[self-hosted, Linux, X64, conexao, conexao-de-sorte-backend-resultados]`

### **Tecnologias Específicas:**
- Spring Boot 3.5.5 + Spring WebFlux (reativo)
- R2DBC MySQL (persistência reativa)
- Redis (cache inteligente de resultados)
- Scheduler (cálculos automáticos)
- Cache L2 com TTL específico

---

## 🗄️ ESTRUTURA DO BANCO DE DADOS

### **Database**: `conexao_sorte_resultados`

#### **Tabelas:**
1. **`resultados`** - Resultados de jogos e sorteios

#### **Estrutura da Tabela:**
```sql
-- resultados
id (Long PK, AUTO_INCREMENT)
horario (String)           -- Horário do sorteio
primeiro (String)          -- 1º prêmio
segundo (String)           -- 2º prêmio  
terceiro (String)          -- 3º prêmio
quarto (String)            -- 4º prêmio
quinto (String)            -- 5º prêmio
sexto (String)             -- 6º prêmio
setimo (String)            -- 7º prêmio
soma (String)              -- Soma calculada automaticamente
data_resultado (Date)      -- Data do resultado
data_criacao (DateTime)    -- Timestamp criação
data_atualizacao (DateTime) -- Timestamp atualização
```

#### **Relacionamentos Inter-Serviços:**
- Trigger eventos → financeiro.transacao (creditação prêmios)

### **Configuração R2DBC:**
```yaml
r2dbc:
  url: r2dbc:mysql://mysql-proxy:6033/conexao_sorte_resultados
  pool:
    initial-size: 1
    max-size: 15
```

---

## 🔐 SECRETS ESPECÍFICOS

### **Azure Key Vault Secrets Utilizados:**
```yaml
# Database
conexao-de-sorte-database-r2dbc-url
conexao-de-sorte-database-username
conexao-de-sorte-database-password

# Redis Cache
conexao-de-sorte-redis-host
conexao-de-sorte-redis-password
conexao-de-sorte-redis-port

# JWT for service-to-service
conexao-de-sorte-jwt-secret
conexao-de-sorte-jwt-verification-key

# External APIs (se houver)
conexao-de-sorte-api-rate-limit-key
conexao-de-sorte-webhook-secret
```

### **Cache Redis Específico:**
```yaml
redis:
  host: conexao-redis
  cache-names:
    - resultados         # TTL: 300s (5min)
    - ranking           # TTL: 900s (15min)
    - estatisticas      # TTL: 1800s (30min)
    - premiacao         # TTL: 60s (1min)
```

---

## 🌐 INTEGRAÇÃO DE REDE

### **Comunicação Entrada (Server):**
- **Gateway** → Resultados (rotas /resultados/*)
- **Frontend** → Resultados (consulta resultados)
- **Scheduler** → Resultados (cálculos automáticos)

### **Comunicação Saída (Client):**
- Resultados → **Autenticação** (validação JWT)
- Resultados → **Financeiro** (creditação de prêmios)
- Resultados → **Notificações** (avisos de resultados)
- Resultados → **Auditoria** (eventos de sorteio)

### **Portas e Endpoints:**
```yaml
server.port: 8083

# Endpoints principais:
GET    /resultados/latest
GET    /resultados/hoje
GET    /resultados/data/{data}
GET    /resultados/historico
POST   /resultados            # Admin only
PUT    /resultados/{id}       # Admin only
GET    /resultados/ranking
GET    /resultados/estatisticas
GET    /actuator/health
```

---

## 🔗 DEPENDÊNCIAS CRÍTICAS

### **Serviços Dependentes (Upstream):**
1. **MySQL** (mysql-proxy:6033) - Persistência principal
2. **Redis** (conexao-redis:6379) - Cache inteligente
3. **Autenticação** (8081) - Validação JWT
4. **Azure Key Vault** - Secrets management

### **Serviços Consumidores (Downstream):**
- **Frontend** - Exibição de resultados
- **Financeiro** - Creditação automática
- **Notificações** - Avisos de prêmios
- **Gateway** - Roteamento público

### **Ordem de Deploy:**
```
1. MySQL + Redis (infrastructure)
2. Autenticação (JWT validation)
3. Resultados (core results)
4. Financeiro (prize distribution)
5. Frontend (display results)
```

---

## 🚨 ESPECIFICIDADES DOS JOGOS

### **Tipos de Resultados:**
- **Loterias**: 6-7 números + soma
- **Sorteios**: Números únicos
- **Ranking**: Top players/winners
- **Estatísticas**: Números mais sorteados

### **Cálculos Automáticos:**
```yaml
calculos:
  soma-automatica: true
  validacao-numeros: true
  duplicatas-check: true
  ranking-update: auto
  cache-invalidation: smart
```

### **Cache Strategy:**
```yaml
cache:
  resultados:
    ttl: 300s           # 5 minutos
    max-entries: 1000
    eviction: LRU
  ranking:
    ttl: 900s           # 15 minutos  
    max-entries: 100
    eviction: LRU
```

---

## 📊 MÉTRICAS ESPECÍFICAS

### **Custom Metrics:**
- `resultados_consultas_total{tipo}` - Consultas por tipo
- `resultados_cache_hits_total{cache_name}` - Cache hits
- `resultados_calculos_duration` - Tempo de cálculos
- `resultados_ranking_updates` - Atualizações de ranking
- `resultados_premiacao_events` - Eventos de premiação
- `resultados_api_calls_total{endpoint}` - Chamadas por endpoint

### **Alertas Configurados:**
- Cache miss rate > 30%
- Cálculo duration > 5s
- API response time P95 > 1s
- Database query time > 100ms
- Failed premio distribution > 1%

---

## 🔧 CONFIGURAÇÕES ESPECÍFICAS

### **Application Properties:**
```yaml
# Resultados Configuration
resultados:
  auto-calculo: true
  max-numeros: 7
  validacao-soma: true
  historico-dias: 365
  
# Cache Configuration
cache:
  resultados-ttl: PT5M
  ranking-ttl: PT15M
  estatisticas-ttl: PT30M
  
# Premiação
premiacao:
  auto-credit: true
  delay-seconds: 30
  retry-attempts: 3
  
# Scheduler
scheduler:
  calculo-ranking: "0 */15 * * * *"  # A cada 15min
  limpeza-cache: "0 0 2 * * *"       # 02:00 diário
  backup-resultados: "0 0 3 * * SUN"  # Domingo 03:00
```

### **Business Rules:**
```yaml
regras:
  numeros-min: 1
  numeros-max: 60
  soma-validacao: true
  duplicatas-proibidas: true
  data-futura-proibida: true
```

---

## 🧪 TESTES E VALIDAÇÕES

### **Health Checks:**
```bash
# Health principal
curl -f http://localhost:8083/actuator/health

# Database connectivity
curl -f http://localhost:8083/actuator/health/db

# Redis connectivity
curl -f http://localhost:8083/actuator/health/redis

# Business health
curl -f http://localhost:8083/resultados/health
```

### **Smoke Tests Pós-Deploy:**
```bash
# 1. Consultar últimos resultados
curl http://localhost:8083/resultados/latest

# 2. Consultar ranking
curl http://localhost:8083/resultados/ranking

# 3. Testar cache
curl -w "%{time_total}" http://localhost:8083/resultados/hoje
curl -w "%{time_total}" http://localhost:8083/resultados/hoje  # Deve ser mais rápido

# 4. Validar cálculo automático
curl -X POST http://localhost:8083/resultados/admin/test-calculo \
  -H "Authorization: Bearer $ADMIN_JWT"
```

---

## ⚠️ TROUBLESHOOTING

### **Problema: Cálculos Incorretos**
```bash
# 1. Verificar logs de cálculo
docker service logs conexao-resultados | grep "calculo"

# 2. Validar dados fonte
mysql -e "SELECT * FROM resultados WHERE data_resultado = CURDATE()"

# 3. Recalcular manualmente
curl -X POST http://localhost:8083/resultados/admin/recalcular \
  -H "Authorization: Bearer $ADMIN_JWT"
```

### **Problema: Cache Inconsistente**
```bash
# 1. Verificar cache Redis
redis-cli -a $REDIS_PASS KEYS "resultados:*"

# 2. Invalidar cache específico
redis-cli -a $REDIS_PASS DEL "resultados:latest"

# 3. Clear all cache
curl -X POST http://localhost:8083/resultados/admin/clear-cache \
  -H "Authorization: Bearer $ADMIN_JWT"
```

### **Problema: Performance Queries**
```bash
# 1. Slow query log
mysql -e "SHOW VARIABLES LIKE 'slow_query_log'"

# 2. Database metrics
curl http://localhost:8083/actuator/metrics/r2dbc.pool.connections

# 3. Cache statistics
curl http://localhost:8083/actuator/metrics/cache.gets
```

---

## 📋 CHECKLIST PRÉ-DEPLOY

### **Configuração:**
- [ ] Database `conexao_sorte_resultados` criado
- [ ] Redis cache configurado
- [ ] JWT secrets no Key Vault
- [ ] Scheduler configurado
- [ ] Cache TTL configurado

### **Business Logic:**
- [ ] Regras de validação ativas
- [ ] Cálculo automático habilitado
- [ ] Premiação automática configurada
- [ ] Ranking auto-update funcionando

### **Integração:**
- [ ] Financeiro para creditação
- [ ] Notificações para avisos
- [ ] Auditoria para eventos
- [ ] Frontend para exibição

---

## 🔄 DISASTER RECOVERY

### **Backup Crítico:**
1. **Database `conexao_sorte_resultados`** (crítico - histórico)
2. **Cache Redis** (pode ser reconstruído)
3. **Configurações de cálculo**
4. **Logs de auditoria**

### **Recovery Procedure:**
1. Restore database resultados
2. Clear Redis cache (força reload)
3. Restart results service
4. Execute recálculo de ranking
5. Validate cálculos automáticos
6. Test premiação flow
7. Smoke tests completos

### **Dados Calculados:**
- Ranking pode ser recalculado
- Cache pode ser reconstruído
- Soma é calculada automaticamente
- Estatísticas são derivadas

---

## 💡 OPERATIONAL NOTES

### **Performance Optimization:**
- Cache agressivo para consultas frequentes
- Índices database otimizados
- Connection pooling configurado
- Query optimization contínua

### **Scheduled Jobs:**
```bash
# Ranking update (15 min)
0 */15 * * * * → Atualizar ranking

# Cache cleanup (daily 2AM)  
0 0 2 * * * → Limpeza cache

# Backup results (weekly)
0 0 3 * * SUN → Backup semanal
```

### **Monitoring Critical:**
- Disponibilidade de resultados (SLA 99.9%)
- Latência cache < 50ms
- Precisão de cálculos (100%)
- Creditação automática funcionando

---

**📅 Última Atualização**: Setembro 2025  
**🏷️ Versão**: 1.0  
**🏆 Criticidade**: ALTA - Core business logic da plataforma