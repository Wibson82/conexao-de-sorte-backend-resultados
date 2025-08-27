# 📚 LIÇÕES APRENDIDAS - MICROSERVIÇO RESULTADOS

> **INSTRUÇÕES PARA AGENTES DE IA:** Este arquivo contém lições aprendidas críticas deste microserviço. SEMPRE atualize este arquivo após resolver problemas, implementar correções ou descobrir melhores práticas. Use o formato padronizado abaixo.

---

## 🎯 **METADADOS DO MICROSERVIÇO**
- **Nome:** conexao-de-sorte-backend-resultados
- **Responsabilidade:** Resultados de loteria, estatísticas, rankings
- **Tecnologias:** Spring Boot 3.5.5, WebFlux, R2DBC, Java 24
- **Porta:** 8082
- **Banco de Dados:** `conexao_sorte_resultados` (novo, dedicado)
- **Última Atualização:** 2025-08-27

---

## ✅ **CORREÇÕES APLICADAS (2025-08-27)**

### 🗄️ **1. Configuração de Banco INCORRETA**
**Problema CRÍTICO:** Variáveis de ambiente e nome do banco incorretos
**Sintoma:** `Access denied for user 'resultados_user' (using password: NO)`

**Solução Antes:**
```yaml
r2dbc:
  url: ${DB_URL:...conexao_de_sorte}     # ❌ Nome errado
  username: ${DB_USER:resultados_user}   # ❌ Variável errada  
  password: ${DB_PASSWORD:}              # ❌ Não chegava
flyway:
  url: ${DB_URL:jdbc:...}                # ❌ R2DBC URL no Flyway
```

**Solução Depois:**
```yaml
r2dbc:
  url: ${SPRING_DATASOURCE_URL:r2dbc:mysql://...conexao_sorte_resultados}
  username: ${SPRING_DATASOURCE_USERNAME:conexao_sorte}
  password: ${SPRING_DATASOURCE_PASSWORD:}
flyway:
  url: jdbc:mysql://...conexao_sorte_resultados  # ✅ JDBC fixo
```

**Lições CRÍTICAS:**
- Nome do banco: `conexao_sorte_resultados` (específico do microserviço)
- Variáveis: `SPRING_DATASOURCE_*` (padrão Spring Boot)
- Flyway precisa de URL JDBC fixa (não R2DBC)

### 🌩️ **2. Spring Cloud Azure Incompatibilidade**
**Problema:** `Spring Boot [3.5.5] is not compatible with Spring Cloud release train`
**Solução:** `spring.cloud.compatibility-verifier.enabled: false`

### 🔐 **3. Azure Key Vault Endpoint Hardcoded**
**Problema CRÍTICO:** Endpoint do Azure Key Vault estava hardcoded no código
**Sintoma:** `Failed to configure KeyVault property source 'kv-conexao-de-sorte'`

**Solução Antes:**
```yaml
spring:
  cloud:
    azure:
      keyvault:
        secret:
          property-sources:
            - endpoint: https://kv-conexao-de-sorte.vault.azure.net/  # ❌ HARDCODED
              name: kv-conexao-de-sorte
```

**Solução Depois:**
```yaml
spring:
  cloud:
    azure:
      keyvault:
        secret:
          property-sources:
            - endpoint: ${AZURE_KEYVAULT_ENDPOINT:}  # ✅ VARIÁVEL DE AMBIENTE
              name: ${AZURE_KEYVAULT_NAME:kv-conexao-de-sorte}
```

**Lições CRÍTICAS:**
- **NUNCA** hardcode endpoints do Azure Key Vault
- Use `${AZURE_KEYVAULT_ENDPOINT:}` para flexibilidade entre ambientes
- Use `${AZURE_KEYVAULT_NAME:}` para permitir diferentes Key Vaults
- Valores hardcoded impedem deployment em diferentes ambientes (dev/staging/prod)
- **INSTRUÇÃO PARA AGENTES DE IA:** Sempre use variáveis de ambiente para configurações do Azure
**Causa:** Spring Cloud Azure 5.18.0 + Spring Boot 3.5.5

### 🔧 **3. Redis SerializationContext**
**Problema:** ClassNotFoundException + SerializationContext incompleto
**Solução:** Adicionado `hashKey` e `hashValue` serializers + commons-pool2

---

## 🚨 **PROBLEMAS CONHECIDOS & SOLUÇÕES**

### ❌ **Primeira Conexão com Banco**
**Status:** ⚠️ PENDENTE - banco `conexao_sorte_resultados` precisa ser criado
**Action Required:** 
1. Criar novo banco `conexao_sorte_resultados`
2. Manter estrutura atual do banco principal sem alteração
3. Migrar dados específicos de resultados (se necessário)

### ❌ **Mapeamento de Variáveis Ambiente**
**Sintoma:** Variáveis não chegam no container
**Causa:** Deploy script usando `DB_USER` mas aplicação esperando `SPRING_DATASOURCE_USERNAME`
**Solução:** Padronizar em `SPRING_DATASOURCE_*` em todo ecosystem

### ❌ **Flyway vs R2DBC URLs**
**Erro Comum:** Usar mesma URL para Flyway e R2DBC
**Solução:** 
- R2DBC: `r2dbc:mysql://...`
- Flyway: `jdbc:mysql://...`

---

## 🎯 **BOAS PRÁTICAS IDENTIFICADAS**

### ✅ **Configuração de Banco Correta:**
```yaml
# R2DBC (reativo)
spring:
  r2dbc:
    url: ${SPRING_DATASOURCE_URL:r2dbc:mysql://host/conexao_sorte_resultados}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}

# Flyway (migrations)  
spring:
  flyway:
    url: jdbc:mysql://host/conexao_sorte_resultados  # JDBC fixo
    user: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
```

### ✅ **Redis Otimizado para Resultados:**
```yaml
spring:
  data:
    redis:
      database: 2  # DB 2 dedicado para resultados
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 20  # Resultados = consultas frequentes
```

---

## 🔄 **HISTÓRICO DE MUDANÇAS**

### **2025-08-27**
- ✅ Banco: Corrigido nome + variáveis ambiente e defaults Docker (`conexao-mysql`)
- ✅ Spring Cloud: Compatibilidade resolvida
- ✅ EntryPoint: pré-checagem de conectividade com fallbacks (conexao-mysql → host.docker.internal → gateway → localhost)
- ✅ .dockerignore: adicionado para reduzir contexto e evitar vazamentos
- ✅ Flyway: desativado temporariamente para isolar conectividade
- ✅ Redis: auto-config desabilitada quando não configurado (evita UnknownHost "redis")
- ⚠️ PENDENTE: Confirmar criação do banco `conexao_sorte_resultados` e reativar Flyway

---

## 📋 **CHECKLIST PARA FUTURAS ALTERAÇÕES**

**Banco de Dados:**
- [ ] Nome: `conexao_sorte_resultados` (nunca `conexao_de_sorte`)
- [ ] Variáveis: `SPRING_DATASOURCE_*` OU `DB_*` (ambas aceitas; usar Key Vault + configtree)
- [ ] R2DBC: URL com `r2dbc:mysql://`
- [ ] Flyway: URL com `jdbc:mysql://` (fixo, não variável)

**Conectividade em Docker:**
- [ ] Usar hostname interno: `conexao-mysql` (mesma rede)
- [ ] Evitar `localhost` no container (aponta para o próprio container)
- [ ] Se necessário, usar `DB_HOST_OVERRIDE`/segredo para forçar host

**Deployment:**
- [ ] Container com `/run/secrets` populado (Key Vault via OIDC)
- [ ] Azure Key Vault mapeado corretamente
- [ ] Banco `conexao_sorte_resultados` existe e acessível

**Redis:**
- [ ] Se não houver Redis, desabilitar auto-config e health
- [ ] Quando houver, usar hostname interno (ex.: `conexao-redis`) e credenciais via Key Vault
- [ ] Database 2 dedicado para resultados; commons-pool2 presente

---

## 🧭 Diretrizes rápidas para Redis (temporariamente desativado)

- Em produção, provisionar Redis como serviço dedicado (não acoplado a um microserviço específico).
- Nome interno na rede Docker: ex.: `conexao-redis`.
- Segredos via Key Vault (`REDIS_HOST`, `REDIS_PASSWORD`, `REDIS_DB`).
- Reativar auto-config removendo exclusões em `spring.autoconfigure.exclude` e restabelecer health quando disponível.

---

## 🤖 **INSTRUÇÕES PARA AGENTES DE IA**

**🚨 BANCO DE DADOS - ATENÇÃO ESPECIAL:**
1. **Nome do banco:** `conexao_sorte_resultados` (ESPECÍFICO)
2. **Primeira conexão:** Banco precisa existir antes do deploy
3. **Variáveis ambiente:** Sempre `SPRING_DATASOURCE_*`
4. **Flyway:** JDBC URL fixa (não usar variável R2DBC)

**Regras de Ouro:**
- NUNCA usar `conexao_de_sorte` (nome genérico)
- SEMPRE testar conexão antes de deploy
- Flyway = JDBC, R2DBC = reativo
- Redis database 2 = dedicado para resultados

**Deploy Checklist:**
1. Banco `conexao_sorte_resultados` existe?
2. Variáveis `SPRING_DATASOURCE_*` configuradas?
3. Redis database 2 configurado?
4. Spring Cloud compatibility disabled?

---

*📝 Arquivo gerado automaticamente em 2025-08-27 por Claude Code*
*🗄️ IMPORTANTE: Primeiro microserviço com banco dedicado - requer setup especial*
