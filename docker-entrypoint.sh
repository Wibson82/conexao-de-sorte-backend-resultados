#!/bin/bash
# ============================================================================
# 🐳 DOCKER ENTRYPOINT - RESULTADOS MICROSERVICE
# ============================================================================
#
# Script de inicialização personalizado para o Resultados Backend
# Contexto: Microserviço de processamento de resultados/loterias (Porta 8083)
# - Validações específicas de segurança para dados sensíveis
# - Health checks otimizados para processamento batch
# - Configuração JVM específica para processamento de dados
# - Validação de conectividade com RabbitMQ
# - Verificações de conformidade de segurança
#
# Uso: Configurar no Dockerfile como ENTRYPOINT
# ============================================================================

set -euo pipefail

# ============================================================================
# 📋 CONFIGURAÇÃO ESPECÍFICA DO RESULTADOS
# ============================================================================

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Função para ler secrets de arquivos Docker
read_secret() {
    local secret_name="$1"
    local secret_file="/run/secrets/$secret_name"

    if [[ -f "$secret_file" ]]; then
        cat "$secret_file"
    else
        echo ""
    fi
}

# Ler secrets dos arquivos Docker e exportar como variáveis de ambiente
echo "🔐 Lendo secrets do Docker..."
export CONEXAO_DE_SORTE_DATABASE_R2DBC_URL=$(read_secret "conexao-de-sorte-database-r2dbc-url")
export CONEXAO_DE_SORTE_DATABASE_USERNAME=$(read_secret "conexao-de-sorte-database-username")
export CONEXAO_DE_SORTE_DATABASE_PASSWORD=$(read_secret "conexao-de-sorte-database-password")
export CONEXAO_DE_SORTE_REDIS_HOST=$(read_secret "conexao-de-sorte-redis-host")
export CONEXAO_DE_SORTE_REDIS_PORT=$(read_secret "conexao-de-sorte-redis-port")
export CONEXAO_DE_SORTE_REDIS_PASSWORD=$(read_secret "conexao-de-sorte-redis-password")
export CONEXAO_DE_SORTE_REDIS_DATABASE=$(read_secret "conexao-de-sorte-redis-database")
export CONEXAO_DE_SORTE_JWT_ISSUER=$(read_secret "conexao-de-sorte-jwt-issuer")
export CONEXAO_DE_SORTE_JWT_JWKS_URI=$(read_secret "conexao-de-sorte-jwt-jwks-uri")
export CONEXAO_DE_SORTE_SERVER_PORT=$(read_secret "conexao-de-sorte-server-port")

# Definir valores padrão para variáveis de host/porta se não estiverem nos secrets
export CONEXAO_DE_SORTE_DATABASE_HOST="${CONEXAO_DE_SORTE_DATABASE_HOST:-conexao-mysql}"
export CONEXAO_DE_SORTE_DATABASE_PORT="${CONEXAO_DE_SORTE_DATABASE_PORT:-3306}"

# Função de log
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')] [RESULTADOS]${NC} $1"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] [RESULTADOS] ERROR:${NC} $1" >&2
}

success() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] [RESULTADOS] SUCCESS:${NC} $1"
}

warning() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] [RESULTADOS] WARNING:${NC} $1"
}

# ============================================================================
# 🔧 VALIDAÇÃO DE AMBIENTE - RESULTADOS ESPECÍFICO
# ============================================================================

log "🚀 Iniciando validação de ambiente - Resultados Microservice..."

# Verificar se estamos rodando como usuário correto
if [[ "$(id -u)" -eq 0 ]]; then
    warning "Executando como root - isso pode ser inseguro em produção"
fi

# Variáveis obrigatórias específicas do Resultados
required_vars=(
    "CONEXAO_DE_SORTE_DATABASE_R2DBC_URL"
    "CONEXAO_DE_SORTE_DATABASE_USERNAME"
    "CONEXAO_DE_SORTE_DATABASE_PASSWORD"
    "CONEXAO_DE_SORTE_REDIS_HOST"
    "CONEXAO_DE_SORTE_REDIS_PORT"
    "CONEXAO_DE_SORTE_REDIS_PASSWORD"
    "CONEXAO_DE_SORTE_REDIS_DATABASE"
    "CONEXAO_DE_SORTE_JWT_ISSUER"
    "CONEXAO_DE_SORTE_JWT_JWKS_URI"
    "CONEXAO_DE_SORTE_SERVER_PORT"
)

missing_vars=()
for var in "${required_vars[@]}"; do
    if [[ -z "${!var:-}" ]]; then
        missing_vars+=("$var")
    fi
done

if [[ ${#missing_vars[@]} -gt 0 ]]; then
    error "Variáveis de ambiente obrigatórias não definidas para Resultados:"
    for var in "${missing_vars[@]}"; do
        error "  - $var"
    fi
    exit 1
fi

# Validações específicas do Resultados
if [[ "$CONEXAO_DE_SORTE_SERVER_PORT" != "8083" ]]; then
    warning "Porta do Resultados diferente do padrão: $CONEXAO_DE_SORTE_SERVER_PORT (esperado: 8083)"
fi

# Validação de segurança: verificar se não está usando H2 em produção
if [[ "${SPRING_PROFILES_ACTIVE:-}" == "prod" ]]; then
    if [[ "${CONEXAO_DE_SORTE_DATABASE_R2DBC_URL:-}" =~ r2dbc:h2 ]]; then
        error "❌ H2 database detectado em ambiente de produção - violação de segurança"
        exit 1
    fi
    success "✅ Validação de segurança: H2 não detectado em produção"
fi

success "✅ Validação de ambiente concluída - Resultados"

# ============================================================================
# 🔐 VALIDAÇÃO DE SEGURANÇA ESPECÍFICA - RESULTADOS
# ============================================================================

log "🔐 Executando validações de segurança específicas do Resultados..."

# Verificar se há exposição de porta não segura
if [[ -n "${CONEXAO_DE_SORTE_SERVER_PORT:-}" ]]; then
    if [[ "$CONEXAO_DE_SORTE_SERVER_PORT" -lt 8080 ]]; then
        warning "Porta do Resultados abaixo de 8080: $CONEXAO_DE_SORTE_SERVER_PORT - verificar configuração de segurança"
    fi
fi

# Validar complexidade de senhas
if [[ -n "${CONEXAO_DE_SORTE_DATABASE_PASSWORD:-}" ]]; then
    if [[ ${#CONEXAO_DE_SORTE_DATABASE_PASSWORD} -lt 8 ]]; then
        error "❌ Senha do database muito curta (mínimo 8 caracteres)"
        exit 1
    fi
    success "✅ Complexidade da senha do database validada"
fi

if [[ -n "${CONEXAO_DE_SORTE_REDIS_PASSWORD:-}" ]]; then
    if [[ ${#CONEXAO_DE_SORTE_REDIS_PASSWORD} -lt 8 ]]; then
        error "❌ Senha do Redis muito curta (mínimo 8 caracteres)"
        exit 1
    fi
    success "✅ Complexidade da senha do Redis validada"
fi

# ============================================================================
# 🗄️ VALIDAÇÃO DE CONECTIVIDADE - DATABASE
# ============================================================================

log "🔍 Validando conectividade com database..."

max_attempts=30
attempt=1

while [[ $attempt -le $max_attempts ]]; do
    # Usar netcat para verificar conectividade
    if nc -z "$CONEXAO_DE_SORTE_DATABASE_HOST" "$CONEXAO_DE_SORTE_DATABASE_PORT" 2>/dev/null; then
        success "✅ Database está acessível"
        break
    fi

    if [[ $attempt -eq $max_attempts ]]; then
        error "❌ Database não ficou disponível após $max_attempts tentativas"
        exit 1
    fi

    log "⏳ Aguardando database... (tentativa $attempt/$max_attempts)"
    sleep 2
    ((attempt++))
done

# ============================================================================
# 🔴 VALIDAÇÃO DE CONECTIVIDADE - REDIS (RESULTADOS)
# ============================================================================

log "🔍 Validando conectividade com Redis (database ${CONEXAO_DE_SORTE_REDIS_DATABASE:-0})..."

max_attempts=30
attempt=1

while [[ $attempt -le $max_attempts ]]; do
    if nc -z "$CONEXAO_DE_SORTE_REDIS_HOST" "$CONEXAO_DE_SORTE_REDIS_PORT" 2>/dev/null; then
        success "✅ Redis está acessível na database ${CONEXAO_DE_SORTE_REDIS_DATABASE:-0}"
        break
    fi

    if [[ $attempt -eq $max_attempts ]]; then
        error "❌ Redis não ficou disponível após $max_attempts tentativas"
        exit 1
    fi

    log "⏳ Aguardando Redis... (tentativa $attempt/$max_attempts)"
    sleep 2
    ((attempt++))
done

# ============================================================================
# 🐰 VALIDAÇÃO DE CONECTIVIDADE - RABBITMQ (OPCIONAL)
# ============================================================================

if [[ -n "${CONEXAO_DE_SORTE_RABBITMQ_HOST:-}" ]] && [[ -n "${CONEXAO_DE_SORTE_RABBITMQ_PORT:-}" ]]; then
    log "🔍 Validando conectividade com RabbitMQ..."

    max_attempts=15
    attempt=1

    while [[ $attempt -le $max_attempts ]]; do
        if nc -z "$CONEXAO_DE_SORTE_RABBITMQ_HOST" "$CONEXAO_DE_SORTE_RABBITMQ_PORT" 2>/dev/null; then
            success "✅ RabbitMQ está acessível"
            break
        fi

        if [[ $attempt -eq $max_attempts ]]; then
            warning "⚠️ RabbitMQ não ficou disponível após $max_attempts tentativas - continuando sem RabbitMQ"
            break
        fi

        log "⏳ Aguardando RabbitMQ... (tentativa $attempt/$max_attempts)"
        sleep 2
        ((attempt++))
    done
else
    log "ℹ️ RabbitMQ não configurado - operando sem message queue"
fi

# ============================================================================
# ⚙️ CONFIGURAÇÃO JVM - RESULTADOS ESPECÍFICO
# ============================================================================

log "🔧 Configurando JVM para Resultados..."

# JVM flags otimizadas para processamento de dados (batch processing)
JVM_OPTS=(
    "-XX:+UseContainerSupport"
    "-XX:MaxRAMPercentage=75.0"
    "-XX:+UseG1GC"
    "-XX:MaxGCPauseMillis=200"
    "-XX:+UseStringDeduplication"
    "-XX:+OptimizeStringConcat"
    "-XX:+UseCompressedOops"
    "-Djava.security.egd=file:/dev/./urandom"
    "-Dspring.jmx.enabled=false"
    "-XX:+DisableExplicitGC"
    "-XX:+UnlockExperimentalVMOptions"
    "-XX:+UseJVMCICompiler"
    "-Dreactor.netty.pool.maxConnections=1000"
    "-Dreactor.netty.pool.maxIdleTime=45000"
    "-Dreactor.netty.pool.maxLifeTime=60000"
    "-Xlog:gc*:file=/app/logs/gc.log:time,level,tags:filecount=5,filesize=10m"
)

# Adicionar flags específicas baseadas no ambiente
if [[ "${SPRING_PROFILES_ACTIVE:-}" == "prod" ]]; then
    JVM_OPTS+=(
        "-XX:+DisableExplicitGC"
        "-XX:+UnlockExperimentalVMOptions"
        "-XX:+UseJVMCICompiler"
        "-XX:+HeapDumpOnOutOfMemoryError"
        "-XX:HeapDumpPath=/app/logs/"
    )
fi

# Exportar JVM_OPTS
export JAVA_OPTS="${JVM_OPTS[*]}"

success "✅ JVM configurada para Resultados"

# ============================================================================
# 📊 INFORMAÇÕES DO AMBIENTE - RESULTADOS
# ============================================================================

log "📋 Informações do ambiente - Resultados:"
echo "  - Service: Conexão de Sorte - Resultados Microservice"
echo "  - Profile: ${SPRING_PROFILES_ACTIVE:-default}"
echo "  - Server Port: $CONEXAO_DE_SORTE_SERVER_PORT (Padrão: 8083)"
echo "  - Database: $CONEXAO_DE_SORTE_DATABASE_HOST:$CONEXAO_DE_SORTE_DATABASE_PORT"
echo "  - Redis: $CONEXAO_DE_SORTE_REDIS_HOST:$CONEXAO_DE_SORTE_REDIS_PORT (DB: ${CONEXAO_DE_SORTE_REDIS_DATABASE:-0})"
echo "  - RabbitMQ: ${CONEXAO_DE_SORTE_RABBITMQ_HOST:-Não configurado}:${CONEXAO_DE_SORTE_RABBITMQ_PORT:-5672}"
echo "  - JWT Issuer: $CONEXAO_DE_SORTE_JWT_ISSUER"
echo "  - Health Endpoint: http://localhost:$CONEXAO_DE_SORTE_SERVER_PORT/actuator/health"
echo "  - Traefik Health: https://traefik.conexaodesorte.com.br/health/service/resultados"
echo "  - JVM Memory: $(java -XX:+PrintFlagsFinal -version 2>&1 | grep -E "MaxHeapSize|InitialHeapSize" | awk '{print $4/1024/1024 "MB"}' | tr '\n' ' / ')"

# ============================================================================
# 🏃 EXECUÇÃO DA APLICAÇÃO - RESULTADOS
# ============================================================================

log "🏃 Iniciando Resultados Microservice..."

# Executar aplicação com exec para permitir signal handling
exec "$@"
