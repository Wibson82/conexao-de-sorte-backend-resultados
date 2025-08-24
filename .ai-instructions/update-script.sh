#!/bin/bash

# 🔄 Script de Sincronização de Diretrizes para Modelos de IA
# Este script atualiza TODOS os arquivos de instruções quando as diretrizes centrais são modificadas

set -e

echo "🤖 Iniciando sincronização de diretrizes para modelos de IA..."

# Diretório base do projeto
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
AI_GUIDELINES="$PROJECT_ROOT/AI_DEVELOPMENT_GUIDELINES.md"

# Verificar se o arquivo de diretrizes centrais existe
if [ ! -f "$AI_GUIDELINES" ]; then
    echo "❌ Erro: Arquivo AI_DEVELOPMENT_GUIDELINES.md não encontrado!"
    exit 1
fi

echo "📋 Arquivo de diretrizes encontrado: $AI_GUIDELINES"

# Template base para todos os modelos
generate_instructions() {
    local model_name="$1"
    local model_icon="$2"
    local target_file="$3"
    
    cat > "$target_file" << EOF
# $model_icon $model_name - Diretrizes de Desenvolvimento

## 📋 INSTRUÇÕES OBRIGATÓRIAS PARA $(echo "$model_name" | tr '[:lower:]' '[:upper:]')

Estas diretrizes devem ser seguidas **SEMPRE** ao gerar código para este projeto. **NUNCA** comprometa estes padrões.

### 🎯 PRINCÍPIOS FUNDAMENTAIS

#### ✅ SEMPRE FAZER
- **Português obrigatório** - Toda comunicação, documentação e comentários em português
- **Código reativo** - Sempre usar \`Mono\`/\`Flux\` e programação não-bloqueante
- **Constantes descritivas** - NUNCA usar valores hardcoded, sempre criar constantes
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
- Downgrade de versões de dependências
- Código sem tratamento de erro
- Configurações hardcoded
- Remover arquivos sem consentimento explícito do usuário
- Tomar decisões não solicitadas no prompt
- Implementar funcionalidades sem commit descritivo

### 🏗️ STACK TECNOLÓGICA
\`\`\`yaml
Java: 24+ (LTS mais recente)
Spring Boot: 3.5+
Spring WebFlux: Reativo obrigatório
R2DBC: Para acesso reativo ao banco
MySQL: 8.0+
Redis: Cache distribuído
\`\`\`

### 🔒 SEGURANÇA OBRIGATÓRIA
- JWT via JWKS para autenticação
- OAuth2 Resource Server
- Headers de segurança configurados
- Bean Validation em todas as entradas
- Rate limiting implementado

### 🎨 PADRÕES DE CÓDIGO

#### Estrutura de Arquivos
\`\`\`
src/main/java/br/tec/facilitaservicos/[projeto]/
├── aplicacao/servico/     # Serviços de Negócio
├── dominio/entidade/      # Entidades de Domínio
├── infraestrutura/        # Configurações
└── apresentacao/          # Controladores REST
\`\`\`

#### Constantes Obrigatórias
\`\`\`java
// ✅ CORRETO - Usar constantes descritivas
private static final String NOME_SERVICO = "resultados-microservice";
private static final int LIMITE_MAXIMO_RANKING = 50;
private static final String TEMPLATE_ERRO = \"\"\"{"erro": "%s", "timestamp": "%s"}\"\"\";

// ❌ ERRADO - Valores hardcoded
String serviceName = "resultados-microservice"; // NUNCA fazer isso
int limit = 50; // NUNCA fazer isso
\`\`\`

#### Programação Reativa
\`\`\`java
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
\`\`\`

### 📝 DOCUMENTAÇÃO
\`\`\`java
/**
 * Busca resultados paginados com filtros opcionais.
 * 
 * @param pagina Número da página (0-based)
 * @param tamanho Tamanho da página (máximo 100)
 * @param filtros Filtros opcionais
 * @return Mono com página de resultados
 * @throws ValidationException Se parâmetros inválidos
 */
\`\`\`

### 🛡️ TRATAMENTO DE ERROS
\`\`\`java
// Estrutura padrão de erro
{
    "status": 400,
    "erro": "Validação falhou",
    "mensagem": "Parâmetros inválidos fornecidos",
    "timestamp": "2024-01-01T10:00:00Z",
    "path": "/api/resultados"
}
\`\`\`

### ⚡ PERFORMANCE
- Connection pooling configurado
- Cache Redis + Caffeine
- Paginação em todas as listagens
- Compression de responses HTTP

### 🔍 LOGGING
\`\`\`java
// ✅ CORRETO - Log estruturado em português
log.info("Operação realizada com sucesso", 
    Map.of(
        "operacao", "buscar_resultados",
        "usuario", userId,
        "tempo_execucao", duration
    )
);
\`\`\`

---

## ⚠️ AVISOS CRÍTICOS

> **🚨 OBRIGATÓRIO**: Estas diretrizes são MANDATÓRIAS. Qualquer desvio será rejeitado.

> **🔒 SEGURANÇA**: NUNCA comprometa a segurança por velocidade.

> **🎯 QUALIDADE**: Todo código deve estar pronto para produção.

---

**Para diretrizes completas, consulte**: \`AI_DEVELOPMENT_GUIDELINES.md\`
EOF
}

# Array com informações dos modelos: "nome:ícone:arquivo"
models=(
    "GitHub Copilot:🤖:.github/copilot-instructions.md"
    "Cursor AI:🎯:.cursor/instructions.md"
    "Windsurf:🌊:.windsurf/instructions.md"
    "TabNine AI:🧠:.tabnine/instructions.md"
    "Continue AI:🔄:.continue/instructions.md"
    "Qodo Gen AI:🎯:.qodo/instructions.md"
    "Claude AI:🤖:.claude/instructions.md"
    "Gemini AI:💎:.gemini/instructions.md"
    "Augment AI:🚀:.augment/instructions.md"
    "Codeium AI:🔮:.codeium/instructions.md"
)

# Criar diretórios se não existirem e gerar arquivos
for model_info in "${models[@]}"; do
    IFS=':' read -r name icon file <<< "$model_info"
    
    # Criar diretório se necessário
    dir=$(dirname "$PROJECT_ROOT/$file")
    mkdir -p "$dir"
    
    # Gerar arquivo de instruções
    echo "📝 Atualizando: $file"
    generate_instructions "$name" "$icon" "$PROJECT_ROOT/$file"
done

echo ""
echo "✅ Sincronização concluída com sucesso!"
echo "📊 Total de modelos atualizados: ${#models[@]}"
echo ""
echo "🔍 Arquivos atualizados:"
for model_info in "${models[@]}"; do
    IFS=':' read -r name icon file <<< "$model_info"
    echo "   $icon $file"
done
echo ""
echo "⚠️  Lembre-se: Sempre atualize AI_DEVELOPMENT_GUIDELINES.md PRIMEIRO!"
echo "🎯 Próximo passo: Commit e push das alterações"
