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
- ✅ Banco: Corrigido nome + variáveis ambiente
- ✅ Spring Cloud: Compatibilidade resolvida
- ✅ Redis: SerializationContext completo + database=2
- ⚠️ PENDENTE: Criar banco `conexao_sorte_resultados`

---

## 📋 **CHECKLIST PARA FUTURAS ALTERAÇÕES**

**Banco de Dados:**
- [ ] Nome: `conexao_sorte_resultados` (nunca `conexao_de_sorte`)
- [ ] Variáveis: `SPRING_DATASOURCE_*` (padrão Spring Boot)
- [ ] R2DBC: URL com `r2dbc:mysql://`
- [ ] Flyway: URL com `jdbc:mysql://` (fixo, não variável)

**Deployment:**
- [ ] Container environment usa `SPRING_DATASOURCE_*`
- [ ] Azure Key Vault mapeado corretamente
- [ ] Banco `conexao_sorte_resultados` existe e acessível

**Redis:**
- [ ] Database 2 dedicado para resultados
- [ ] SerializationContext completo (key, value, hashKey, hashValue)
- [ ] Commons-pool2 dependency presente

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