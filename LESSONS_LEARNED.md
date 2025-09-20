# 📚 LIÇÕES APRENDIDAS - MICROSERVIÇO RESULTADOS

> **INSTRUÇÕES PARA AGENTES DE IA:** Este arquivo contém lições aprendidas críticas deste microserviço. SEMPRE atualize este arquivo após resolver problemas, implementar correções ou descobrir melhores práticas. Use o formato padronizado abaixo.

---

## 🎯 **METADADOS DO MICROSERVIÇO**
- **Nome:** conexao-de-sorte-backend-resultados
- **Responsabilidade:** Resultados de loteria, estatísticas, rankings
- **Tecnologias:** Spring Boot 3.5.5, WebFlux, R2DBC, Java 25 LTS
- **Porta:** 8082
- **Banco de Dados:** `conexao_sorte_resultados` (novo, dedicado)
- **Última Atualização:** 2025-01-17

---

## ✅ **CORREÇÕES APLICADAS (2025-08-27 + 2025-01-17)**

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

### 🔐 **4. Configuração OIDC e Service Principal (2025-01-17)**
**Problema CRÍTICO:** Service Principal não configurado corretamente para GitHub Actions
**Sintoma:** Falhas de autenticação no Azure Key Vault durante CI/CD

**Solução Implementada:**
- **Service Principal:** `sp-conexao-de-sorte-github` (Object ID: `b53e8ee7-5171-4050-b1ee-f11296db1a39`)
- **App ID (CLIENT_ID):** `0d27da5e-e9dc-41e5-8e85-ccd03fc6dad7`
- **Role RBAC:** `Key Vault Secrets User` no Key Vault `kv-conexao-de-sorte`
- **GitHub Secrets:** Configurados para OIDC authentication

**GitHub Secrets Criados:**
```bash
AZURE_CLIENT_ID=0d27da5e-e9dc-41e5-8e85-ccd03fc6dad7
AZURE_TENANT_ID=<tenant-id>
AZURE_SUBSCRIPTION_ID=<subscription-id>
AZURE_KEYVAULT_ENDPOINT=https://kv-conexao-de-sorte.vault.azure.net/
AZURE_KEYVAULT_NAME=kv-conexao-de-sorte
```

**Lições CRÍTICAS:**
- **Service Principal** é obrigatório para GitHub Actions acessar Azure Key Vault
- **RBAC Role** `Key Vault Secrets User` é suficiente para leitura de secrets
- **OIDC** é mais seguro que Client Secret para CI/CD
- **GitHub Secrets** devem ser configurados no repositório para funcionar

### 🗝️ **5. Secrets Ausentes no Azure Key Vault (2025-01-17)**
**Problema CRÍTICO:** 11 secrets essenciais não existiam no Key Vault
**Sintoma:** Aplicação falhava ao carregar configurações do Azure Key Vault

**Secrets Criados:**
1. `conexao-de-sorte-redis-password` - Senha do Redis
2. `conexao-de-sorte-redis-database` - Database do Redis (valor: 2)
3. `conexao-de-sorte-database-jdbc-url` - URL JDBC do banco
4. `conexao-de-sorte-jwt-issuer` - Issuer JWT (valor: conexaodesorte.com.br)
5. `conexao-de-sorte-jwt-jwks-uri` - JWKS URI (valor: https://conexaodesorte.com.br/.well-known/jwks.json)
6. `conexao-de-sorte-encryption-backup-key` - Chave de backup criptografia
7. `conexao-de-sorte-ssl-enabled` - manter `false` (TLS no Traefik/ACME)
8. `conexao-de-sorte-ssl-keystore-path` - não utilizado (TLS na borda)
9. `conexao-de-sorte-ssl-keystore-password` - não utilizado (TLS na borda)
10. `conexao-de-sorte-cors-allowed-origins` - Origins CORS (valor: https://conexaodesorte.com.br)
11. `conexao-de-sorte-cors-allow-credentials` - Credenciais CORS (valor: true)

**Comando CLI Usado:**
```bash
az keyvault secret set --vault-name kv-conexao-de-sorte --name conexao-de-sorte-redis-password --value redis-default-password && \
az keyvault secret set --vault-name kv-conexao-de-sorte --name conexao-de-sorte-redis-database --value 2 && \
# ... (outros secrets)
```

**Lições CRÍTICAS:**
- **Análise de logs** é essencial para identificar secrets ausentes
- **Azure CLI** é eficiente para criar múltiplos secrets
- **Valores padrão** devem ser substituídos por valores de produção
- **Domínio personalizado** (conexaodesorte.com.br) deve ser usado em JWT e CORS
- **Total de 25 secrets** agora disponíveis no Key Vault

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

**🔐 AZURE KEY VAULT & OIDC - CONFIGURAÇÃO CRÍTICA:**
1. **Service Principal:** `sp-conexao-de-sorte-github` (Object ID: `b53e8ee7-5171-4050-b1ee-f11296db1a39`)
2. **RBAC Role:** `Key Vault Secrets User` é suficiente para leitura
3. **GitHub Secrets:** 5 secrets obrigatórios para OIDC (CLIENT_ID, TENANT_ID, SUBSCRIPTION_ID, KEYVAULT_ENDPOINT, KEYVAULT_NAME)
4. **Total Secrets:** 25 secrets no Key Vault (14 originais + 11 criados em 2025-01-17)
5. **Domínio:** `conexaodesorte.com.br` usado em JWT e CORS

**Regras de Ouro:**
- NUNCA usar `conexao_de_sorte` (nome genérico)
- SEMPRE testar conexão antes de deploy
- Flyway = JDBC, R2DBC = reativo
- Redis database 2 = dedicado para resultados
- NUNCA hardcode Azure Key Vault endpoints
- SEMPRE usar OIDC para GitHub Actions (mais seguro que Client Secret)
- SEMPRE verificar se todos os 25 secrets existem no Key Vault

**Deploy Checklist:**
1. Banco `conexao_sorte_resultados` existe?
2. Variáveis `SPRING_DATASOURCE_*` configuradas?
3. Redis database 2 configurado?
4. Spring Cloud compatibility disabled?
5. Service Principal `sp-conexao-de-sorte-github` tem role `Key Vault Secrets User`?
6. GitHub Secrets configurados (5 secrets OIDC)?
7. Todos os 25 secrets existem no Azure Key Vault?
8. Domínio `conexaodesorte.com.br` configurado em JWT e CORS?

---

*📝 Arquivo gerado automaticamente em 2025-08-27 por Claude Code*
*🗄️ IMPORTANTE: Primeiro microserviço com banco dedicado - requer setup especial*
