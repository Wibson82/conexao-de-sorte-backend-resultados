# 🤖 DIRETRIZES DE DESENVOLVIMENTO PARA MODELOS DE IA

## 📋 VISÃO GERAL

Este documento estabelece as diretrizes obrigatórias para todos os modelos de inteligência artificial que trabalham neste projeto. **NUNCA** comprometa estes padrões por conveniência ou velocidade.

---

## 🎯 PRINCÍPIOS FUNDAMENTAIS

### ✅ SEMPRE FAZER
- **Código válido para produção** - Todo código deve estar pronto para deploy
- **Boas práticas do mercado** - Seguir padrões da indústria
- **Segurança em primeiro lugar** - Implementar todas as medidas de proteção
- **Programação defensiva** - Validar entradas, tratar erros, logs detalhados
- **Versionamento progressivo** - NUNCA fazer downgrade de versões
- **Português obrigatório** - Toda comunicação, documentação e comentários em português
- **Código reativo** - Sempre usar programação reativa e não-bloqueante
- **Constantes descritivas** - NUNCA usar valores hardcoded, sempre criar constantes
- **Azure Key Vault flexível** - SEMPRE usar variáveis de ambiente para endpoints e nomes do Key Vault
- **Commits descritivos obrigatórios** - Todo prompt implementado ou etapa concluída deve gerar commit IMEDIATAMENTE
- **Executar git add . && git commit** - OBRIGATÓRIO após cada implementação
- **Solicitar consentimento** - NUNCA remover arquivos sem autorização explícita do usuário
- **Não tomar decisões** - Apenas executar o que foi solicitado explicitamente no prompt

### ❌ NUNCA FAZER
- Downgrade de versões de dependências
- Código sem tratamento de erro
- Endpoints sem validação de entrada
- Logs sem informações de contexto
- Configurações hardcoded
- Endpoints do Azure Key Vault hardcoded
- Senhas ou tokens em código
- Comunicação em inglês (exceto nomes técnicos)
- Código bloqueante ou síncrono
- Valores mágicos ou hardcoded no código
- Remover arquivos sem consentimento explícito do usuário
- Tomar decisões não solicitadas no prompt
- Implementar funcionalidades sem commit descritivo

---

## 🏗️ STACK TECNOLÓGICA OBRIGATÓRIA

### Java & Spring
```yaml
Java: 24+ (LTS mais recente disponível)
Spring Boot: 3.5+
Spring Security: 6.1+
Spring WebFlux: Reativo obrigatório
```

### Banco de Dados
```yaml
R2DBC: Para acesso reativo
MySQL: 8.0+
Flyway: Para migrations
Connection Pool: Configurado adequadamente
```

### Observabilidade
```yaml
Micrometer: Métricas
Prometheus: Coleta de métricas
Actuator: Health checks
Logging: Estruturado (JSON)
Tracing: Distribuído habilitado
```

---

## 🔒 SEGURANÇA OBRIGATÓRIA

### Autenticação & Autorização
- **JWT via JWKS** - Validação de tokens
- **OAuth2 Resource Server** - Configuração adequada
- **Roles e Scopes** - Controle granular de acesso
- **Rate Limiting** - Proteção contra abuso

### Headers de Segurança
```yaml
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
Content-Security-Policy: Configurado adequadamente
```

### Validação de Dados
- **Bean Validation** - Todas as entradas
- **Sanitização** - Dados de usuário
- **Escape** - Saídas para frontend
- **Limites** - Tamanho de payload, paginação

---

## 🎨 ARQUITETURA & DESIGN

### SOLID Principles
- **S** - Single Responsibility
- **O** - Open/Closed
- **L** - Liskov Substitution
- **I** - Interface Segregation
- **D** - Dependency Inversion

### Padrões Arquiteturais
```
src/
├── main/java/br/tec/facilitaservicos/[projeto]/
│   ├── aplicacao/           # Camada de Aplicação
│   │   ├── servico/         # Serviços de Negócio
│   │   └── dto/             # DTOs de Aplicação
│   ├── dominio/             # Camada de Domínio
│   │   ├── entidade/        # Entidades de Domínio
│   │   ├── repositorio/     # Interfaces de Repositório
│   │   └── servico/         # Serviços de Domínio
│   ├── infraestrutura/      # Camada de Infraestrutura
│   │   ├── repositorio/     # Implementações de Repositório
│   │   ├── configuracao/    # Configurações
│   │   └── seguranca/       # Segurança
│   └── apresentacao/        # Camada de Apresentação
│       ├── controlador/     # Controladores REST
│       └── dto/             # DTOs de API
```

### Constantes Obrigatórias
- **Strings repetidas** - Criar constantes para valores usados múltiplas vezes
- **Números mágicos** - Substituir por constantes com nomes descritivos
- **URLs e endpoints** - Definir como constantes de classe
- **Mensagens de erro** - Centralizar em classes de constantes

### Programação Reativa
- **Mono/Flux** - Tipos reativos obrigatórios
- **Non-blocking I/O** - Todas as operações
- **Backpressure** - Gerenciamento adequado
- **Error Handling** - Operadores reativos

---

## 📊 QUALIDADE & TESTES

### Cobertura de Testes
```yaml
Unitários: 80%+ cobertura
Integração: Cenários críticos
End-to-End: Fluxos principais
Testcontainers: Para testes de integração
```

### Qualidade de Código
- **SonarQube** - Análise estática
- **SpotBugs** - Detecção de bugs
- **Checkstyle** - Padrões de código
- **PMD** - Análise de código

---

## 🚀 DEPLOY & PRODUÇÃO

### Containerização
```dockerfile
FROM eclipse-temurin:24-jre-alpine
# Multi-stage build obrigatório
# Non-root user
# Health checks configurados
```

### Configuração
- **Externalized Config** - 12-Factor App
- **Environment Variables** - Configurações sensíveis
- **Profiles** - dev, test, prod
- **Feature Flags** - Controle de funcionalidades

### 🔐 Azure Key Vault - DIRETRIZES OBRIGATÓRIAS

**❌ NUNCA FAZER:**
```yaml
# ❌ HARDCODED - PROIBIDO
spring:
  cloud:
    azure:
      keyvault:
        secret:
          property-sources:
            - endpoint: https://kv-conexao-de-sorte.vault.azure.net/
              name: kv-conexao-de-sorte
```

**✅ SEMPRE FAZER:**
```yaml
# ✅ VARIÁVEIS DE AMBIENTE - OBRIGATÓRIO
spring:
  cloud:
    azure:
      keyvault:
        secret:
          property-sources:
            - endpoint: ${AZURE_KEYVAULT_ENDPOINT:}
              name: ${AZURE_KEYVAULT_NAME:kv-conexao-de-sorte}
```

**REGRAS CRÍTICAS:**
- **SEMPRE** use `${AZURE_KEYVAULT_ENDPOINT:}` para endpoints
- **SEMPRE** use `${AZURE_KEYVAULT_NAME:}` para nomes do Key Vault
- **NUNCA** hardcode URLs do Azure Key Vault
- **SEMPRE** permita diferentes Key Vaults por ambiente
- **SEMPRE** documente variáveis de ambiente necessárias

### Monitoramento
```yaml
Health Checks: /actuator/health
Metrics: /actuator/metrics
Prometheus: /actuator/prometheus
Logs: JSON estruturado
Alerts: Configurados adequadamente
```

---

## 🔍 AUDITORIA & COMPLIANCE

### Logging Obrigatório
```java
// Exemplo de log estruturado
log.info("Operação realizada", 
    Map.of(
        "operacao", "buscar_resultados",
        "usuario", userId,
        "parametros", parametros,
        "tempo_execucao", duration,
        "resultado", "sucesso"
    )
);
```

### Auditoria
- **Todas as operações** - CRUD completo
- **Tentativas de acesso** - Sucesso e falha
- **Mudanças de configuração** - Sistema
- **Performance** - Métricas de tempo

---

## 📝 DOCUMENTAÇÃO OBRIGATÓRIA

### API Documentation
- **OpenAPI 3.0** - Especificação completa
- **Swagger UI** - Interface interativa
- **Exemplos** - Request/Response
- **Error Codes** - Documentados

### Código
```java
/**
 * Busca resultados paginados com filtros opcionais.
 * 
 * @param pagina Número da página (0-based)
 * @param tamanho Tamanho da página (máximo 100)
 * @param filtros Filtros opcionais
 * @return Mono com página de resultados
 * @throws ValidationException Se parâmetros inválidos
 * @throws ServiceException Se erro interno
 */
```

---

## ⚡ PERFORMANCE

### Otimizações Obrigatórias
- **Connection Pooling** - R2DBC e Redis
- **Caching** - Redis + Caffeine
- **Paginação** - Todas as listagens
- **Lazy Loading** - Dados relacionados
- **Compression** - Responses HTTP

### Limites
```yaml
Request Timeout: 30s
Connection Pool: Min 10, Max 50
Cache TTL: Configurado por endpoint
Rate Limit: Por usuário/IP
```

---

## 🛡️ TRATAMENTO DE ERROS

### Estrutura Padrão
```java
{
    "status": 400,
    "erro": "Validação falhou",
    "mensagem": "Parâmetros inválidos fornecidos",
    "detalhes": ["Campo 'email' é obrigatório"],
    "timestamp": "2024-01-01T10:00:00Z",
    "path": "/api/usuarios",
    "traceId": "abc123"
}
```

### Logs de Erro
- **Stack trace completo** - Em desenvolvimento
- **Contexto relevante** - Parâmetros, estado
- **Correlation ID** - Para rastreamento
- **Severity levels** - ERROR, WARN, INFO, DEBUG

---

## 🔄 INTEGRAÇÃO CONTÍNUA

### Pipeline Obrigatório
1. **Compile** - Java 24
2. **Test** - Unitários + Integração
3. **Quality Gate** - SonarQube
4. **Security Scan** - OWASP, Snyk
5. **Build Image** - Docker
6. **Deploy** - Staging → Production

### Commits Obrigatórios
- **Todo prompt implementado** deve gerar commit descritivo IMEDIATAMENTE
- **Toda etapa concluída** deve ser commitada separadamente
- **Mensagens em português** com descrição detalhada das mudanças
- **Formato**: `feat: implementa [funcionalidade] - [descrição detalhada]`
- **OBRIGATÓRIO**: Executar `git add .` e `git commit` após CADA implementação
- **NUNCA finalizar** uma tarefa sem commit das alterações

### Critérios de Qualidade
- **Cobertura** ≥ 80%
- **Duplicação** ≤ 3%
- **Complexidade** ≤ 10
- **Vulnerabilidades** = 0
- **Code Smells** ≤ 5

---

## 📚 RECURSOS ADICIONAIS

### Dependências Recomendadas
```xml
<!-- Sempre usar versões mais recentes compatíveis -->
<spring-boot.version>3.5+</spring-boot.version>
<java.version>24</java.version>
<testcontainers.version>1.20+</testcontainers.version>
<micrometer.version>1.13+</micrometer.version>
```

### Ferramentas de Desenvolvimento
- **IntelliJ IDEA** - IDE recomendada
- **Docker Desktop** - Containers locais
- **Postman/Insomnia** - Testes de API
- **JProfiler** - Profiling de performance

---

## ⚠️ AVISOS IMPORTANTES

> **🚨 CRÍTICO**: Estas diretrizes são OBRIGATÓRIAS. Qualquer desvio deve ser justificado e aprovado.

> **🔒 SEGURANÇA**: NUNCA comprometa a segurança por velocidade de desenvolvimento.

> **📈 QUALIDADE**: Código de baixa qualidade é técnica debt que impacta toda a equipe.

> **🎯 PRODUÇÃO**: Todo código deve estar pronto para produção desde o primeiro commit.

---

**Versão**: 1.0  
**Última Atualização**: 2024-08-24  
**Próxima Revisão**: 2024-12-01

---

*Este documento é um contrato de qualidade. Ao trabalhar neste projeto, você concorda em seguir todas estas diretrizes sem exceção.*
