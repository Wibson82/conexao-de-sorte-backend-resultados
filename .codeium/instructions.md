# 🔮 Codeium AI - Diretrizes de Desenvolvimento

## 📋 INSTRUÇÕES OBRIGATÓRIAS PARA CODEIUM AI

Estas diretrizes devem ser seguidas **SEMPRE** ao gerar código para este projeto. **NUNCA** comprometa estes padrões.

### 🎯 PRINCÍPIOS FUNDAMENTAIS

#### ✅ SEMPRE FAZER
- **Português obrigatório** - Toda comunicação, documentação e comentários em português
- **Código reativo** - Sempre usar `Mono`/`Flux` e programação não-bloqueante
- **Constantes descritivas** - NUNCA usar valores hardcoded, sempre criar constantes
- **Azure Key Vault flexível** - SEMPRE usar variáveis de ambiente para endpoints e nomes do Key Vault
- **Código válido para produção** - Todo código deve estar pronto para deploy
- **Segurança em primeiro lugar** - Implementar todas as medidas de proteção
- **Programação defensiva** - Validar entradas, tratar erros, logs detalhados
- **Commits descritivos obrigatórios** - Todo prompt implementado ou etapa concluída deve gerar commit
- **Solicitar consentimento** - NUNCA remover arquivos sem autorização explícita do usuário
- **Não tomar decisões** - Apenas executar o que foi solicitado explicitamente no prompt

#### ❌ NUNCA FAZER
- Comunicação em inglês (exceto nomes técnicos)
- Código bloqueante ou síncrono
- Valores mágicos ou hardcoded no código
- Endpoints do Azure Key Vault hardcoded
- Downgrade de versões de dependências
- Código sem tratamento de erro
- Configurações hardcoded
- Remover arquivos sem consentimento explícito do usuário
- Tomar decisões não solicitadas no prompt
- Implementar funcionalidades sem commit descritivo

### 🏗️ STACK TECNOLÓGICA
```yaml
Java: 24+ (LTS mais recente)
Spring Boot: 3.5+
Spring WebFlux: Reativo obrigatório
R2DBC: Para acesso reativo ao banco
MySQL: 8.0+
Redis: Cache distribuído
```

### 🔒 SEGURANÇA OBRIGATÓRIA
- JWT via JWKS para autenticação
- OAuth2 Resource Server
- Headers de segurança configurados
- Bean Validation em todas as entradas
- Rate limiting implementado

### 🎨 PADRÕES DE CÓDIGO

#### Estrutura de Arquivos
```
src/main/java/br/tec/facilitaservicos/[projeto]/
├── aplicacao/servico/     # Serviços de Negócio
├── dominio/entidade/      # Entidades de Domínio
├── infraestrutura/        # Configurações
└── apresentacao/          # Controladores REST
```

#### Constantes Obrigatórias
```java
// ✅ CORRETO - Usar constantes descritivas
private static final String NOME_SERVICO = "resultados-microservice";
private static final int LIMITE_MAXIMO_RANKING = 50;
private static final String TEMPLATE_ERRO = \"\"\"{"erro": "%s", "timestamp": "%s"}\"\"\";

// ❌ ERRADO - Valores hardcoded
String serviceName = "resultados-microservice"; // NUNCA fazer isso
int limit = 50; // NUNCA fazer isso
```

#### Programação Reativa
```java
// ✅ CORRETO - Código reativo
public Mono<ResultadoDto> buscarPorId(Long id) {
    return repositorio.findById(id)
        .map(mapper::paraDto)
        .switchIfEmpty(Mono.error(new NotFoundException("Resultado não encontrado")));
}

// ❌ ERRADO - Código bloqueante
public ResultadoDto buscarPorId(Long id) {
    return repositorio.findById(id).block(); // NUNCA usar .block()
}
```

### 📝 DOCUMENTAÇÃO
```java
/**
 * Busca resultados paginados com filtros opcionais.
 * 
 * @param pagina Número da página (0-based)
 * @param tamanho Tamanho da página (máximo 100)
 * @param filtros Filtros opcionais
 * @return Mono com página de resultados
 * @throws ValidationException Se parâmetros inválidos
 */
```

### 🛡️ TRATAMENTO DE ERROS
```java
// Estrutura padrão de erro
{
    "status": 400,
    "erro": "Validação falhou",
    "mensagem": "Parâmetros inválidos fornecidos",
    "timestamp": "2024-01-01T10:00:00Z",
    "path": "/api/resultados"
}
```

### ⚡ PERFORMANCE
- Connection pooling configurado
- Cache Redis + Caffeine
- Paginação em todas as listagens
- Compression de responses HTTP

### 🔍 LOGGING
```java
// ✅ CORRETO - Log estruturado em português
log.info("Operação realizada com sucesso", 
    Map.of(
        "operacao", "buscar_resultados",
        "usuario", userId,
        "tempo_execucao", duration
    )
);
```

---

## ⚠️ AVISOS CRÍTICOS

> **🚨 OBRIGATÓRIO**: Estas diretrizes são MANDATÓRIAS. Qualquer desvio será rejeitado.

> **🔒 SEGURANÇA**: NUNCA comprometa a segurança por velocidade.

> **🎯 QUALIDADE**: Todo código deve estar pronto para produção.

---

**Para diretrizes completas, consulte**: `AI_DEVELOPMENT_GUIDELINES.md`
