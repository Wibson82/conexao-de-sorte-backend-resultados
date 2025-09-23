# ============================================================================
# 🐳 DOCKERFILE MULTI-ESTÁGIO - MICROSERVIÇO RESULTADOS
# ============================================================================
#
# Dockerfile otimizado para microserviço de resultados com:
# - Multi-stage build para reduzir tamanho da imagem
# - Java 25 LTS com JVM otimizada para containers
# - Usuário não-root para segurança
# - Health check nativo
# - Otimizações de performance para processamento de dados
# - Suporte a debug remoto (desenvolvimento)
#
# Build: docker build -t conexaodesorte/resultados:latest .
# Run: docker run -p 8082:8082 conexaodesorte/resultados:latest
#
# @author Sistema de Migração R2DBC
# @version 1.0
# @since 2024
# ============================================================================

# === ESTÁGIO 1: BUILD ===
FROM amazoncorretto:25-alpine3.22 AS builder

# Instalar Maven
RUN apk add --no-cache maven

# Metadados da imagem
LABEL maintainer="Conexão de Sorte <tech@conexaodesorte.com>"
LABEL description="Microserviço de Resultados - Build Stage"
LABEL version="1.0.0"

# Variáveis de build
ARG BUILD_DATE
ARG VCS_REF
ARG VERSION=1.0.0

# Definir diretório de trabalho
WORKDIR /build

# Copiar arquivos de configuração Maven (cache layer)
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

# Download de dependências (layer cacheável)
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

# Copiar código fonte
COPY src/ src/

# Build da aplicação com otimizações
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B \
    -Dspring-boot.build-image.pullPolicy=IF_NOT_PRESENT \
    -Dmaven.compiler.debug=false

# === ESTÁGIO 2: RUNTIME ===
FROM amazoncorretto:25-alpine3.22 AS runtime

# Instalar dependências do sistema
RUN apk add --no-cache \
    tzdata \
    curl \
    dumb-init \
    mysql-client \
    netcat-openbsd \
    && rm -rf /var/cache/apk/*

# Configurar timezone
ENV TZ=America/Sao_Paulo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Criar usuário não-root para segurança
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Definir diretório da aplicação
WORKDIR /app

# Copiar JAR da aplicação do estágio de build
COPY --from=builder --chown=appuser:appgroup /build/target/*.jar app.jar

# Copiar script de inicialização do database
COPY --chown=appuser:appgroup scripts/init-database.sh /app/init-database.sh
RUN chmod +x /app/init-database.sh

# Build-time args (Key Vault → Build Args → ENV)
ARG CONEXAO_DE_SORTE_DATABASE_URL
ARG CONEXAO_DE_SORTE_DATABASE_JDBC_URL
ARG CONEXAO_DE_SORTE_DATABASE_R2DBC_URL
ARG CONEXAO_DE_SORTE_DATABASE_USERNAME
ARG CONEXAO_DE_SORTE_DATABASE_PASSWORD
ARG CONEXAO_DE_SORTE_REDIS_HOST
ARG CONEXAO_DE_SORTE_REDIS_PORT
ARG CONEXAO_DE_SORTE_REDIS_PASSWORD
ARG CONEXAO_DE_SORTE_REDIS_DATABASE
ARG CONEXAO_DE_SORTE_JWT_ISSUER
ARG CONEXAO_DE_SORTE_JWT_JWKS_URI
ARG CONEXAO_DE_SORTE_CORS_ALLOWED_ORIGINS
ARG CONEXAO_DE_SORTE_CORS_ALLOW_CREDENTIALS

ENV CONEXAO_DE_SORTE_DATABASE_URL=${CONEXAO_DE_SORTE_DATABASE_URL} \
    CONEXAO_DE_SORTE_DATABASE_JDBC_URL=${CONEXAO_DE_SORTE_DATABASE_JDBC_URL} \
    CONEXAO_DE_SORTE_DATABASE_R2DBC_URL=${CONEXAO_DE_SORTE_DATABASE_R2DBC_URL} \
    CONEXAO_DE_SORTE_DATABASE_USERNAME=${CONEXAO_DE_SORTE_DATABASE_USERNAME} \
    CONEXAO_DE_SORTE_DATABASE_PASSWORD=${CONEXAO_DE_SORTE_DATABASE_PASSWORD} \
    CONEXAO_DE_SORTE_REDIS_HOST=${CONEXAO_DE_SORTE_REDIS_HOST} \
    CONEXAO_DE_SORTE_REDIS_PORT=${CONEXAO_DE_SORTE_REDIS_PORT} \
    CONEXAO_DE_SORTE_REDIS_PASSWORD=${CONEXAO_DE_SORTE_REDIS_PASSWORD} \
    CONEXAO_DE_SORTE_REDIS_DATABASE=${CONEXAO_DE_SORTE_REDIS_DATABASE} \
    CONEXAO_DE_SORTE_JWT_ISSUER=${CONEXAO_DE_SORTE_JWT_ISSUER} \
    CONEXAO_DE_SORTE_JWT_JWKS_URI=${CONEXAO_DE_SORTE_JWT_JWKS_URI} \
    CONEXAO_DE_SORTE_CORS_ALLOWED_ORIGINS=${CONEXAO_DE_SORTE_CORS_ALLOWED_ORIGINS} \
    CONEXAO_DE_SORTE_CORS_ALLOW_CREDENTIALS=${CONEXAO_DE_SORTE_CORS_ALLOW_CREDENTIALS}
## JVM otimizada para containers: flags removidas para compatibilidade total com Java 25 LTS
# As flags e perfis devem ser definidos externamente via workflow/deploy

# Variáveis de ambiente da aplicação


# Expor porta da aplicação


# Health check nativo
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

ARG VERSION=1.0.0
ARG BUILD_DATE=unknown
ARG VCS_REF=unknown
# Labels para metadata
LABEL org.opencontainers.image.title="Conexão de Sorte - Resultados"
LABEL org.opencontainers.image.description="Microserviço de Resultados de Loterias"
LABEL org.opencontainers.image.version=${VERSION}
LABEL org.opencontainers.image.created=${BUILD_DATE}
LABEL org.opencontainers.image.revision=${VCS_REF}
LABEL org.opencontainers.image.vendor="Conexão de Sorte"
LABEL org.opencontainers.image.licenses="MIT"
LABEL org.opencontainers.image.url="https://conexaodesorte.com"
LABEL org.opencontainers.image.source="https://github.com/conexaodesorte/resultados"

# Script de entrada com pré-checagem de conexão ao banco (embutido no build)
RUN printf '%s\n' '#!/bin/sh' \
    'set -eu' \
    '' \
    'log() {' \
    '  printf "%s %s\\n" "$(date '+%Y-%m-%dT%H:%M:%S%z')" "$*"' \
    '}' \
    '' \
    'SECRETS_DIR=${SECRETS_DIR:-/run/secrets}' \
    'R2DBC_FILE="$SECRETS_DIR/spring.r2dbc.url"' \
    'JDBC_FILE="$SECRETS_DIR/spring.flyway.url"' \
    '' \
    'has_nc() { command -v nc >/dev/null 2>&1; }' \
    '' \
    'can_connect() {' \
    '  host="$1"; port="$2"' \
    '  if has_nc; then' \
    '    log "→ Testando TCP $host:$port (nc)"' \
    '    nc -z -w 2 "$host" "$port" >/dev/null 2>&1' \
    '  else' \
    '    log "→ Testando TCP $host:$port (/dev/tcp)"' \
    '    (echo > /dev/tcp/"$host"/"$port") >/dev/null 2>&1 || return 1' \
    '  fi' \
    '}' \
    '' \
    'rewrite_urls() {' \
    '  new_hostport="$1"' \
    '  log "⤴️  Reescrevendo URLs para host:porta '"'"'$new_hostport'"'"'"'"'"'""'"""'"''"'"'"'"'"'"'"'"'"'"''"'"'"'"'"'"''"'"'"'"'"'"'"'"'"'"'"''"'"'"'"'"'"'""'"'"'"'"'"''"'"'"'"'"'"'"'"'"'"'"''"'"'"'"'""'"'"''"'"'"'"'"'"'"'"'"'"'"''"'"'"'"'"'"'"'"'"'"'"''"'"'"'"'"'"'"'"''"'"'"'"'"'"'"'"'"'"'"'"'"''"'"'"'"'"'"'"'"'"'"'"''"'"'"'"'"'"'""'"'"'"'"'"''"'"'"'"'"'"'"'"'"'"'"''"'"'"'"'"'"'"'"'"'"'"'"'"'"''"'"'"'"'"'"'"'"'"'"'"'"'"''"'"'"'"'"'"'""'"'"'"'"'"'"'"''"'"'"'"'"'"'"'"'"'"''"'"'"'"'"'""'"'"'"'"'"'"''"'"'"'"'"'""'' \
    '  if [ -f "$R2DBC_FILE" ]; then' \
    '    r2dbc=$(cat "$R2DBC_FILE")' \
    '    proto="r2dbc:mysql://"' \
    '    rest="${r2dbc#${proto}}"' \
    '    rest_no_host="${rest#*/}"' \
    '    echo "${proto}${new_hostport}/${rest_no_host}" > "$R2DBC_FILE"' \
    '    log "R2DBC URL -> $(cat "$R2DBC_FILE")"' \
    '  fi' \
    '  if [ -f "$JDBC_FILE" ]; then' \
    '    jdbc=$(cat "$JDBC_FILE")' \
    '    proto="jdbc:mysql://"' \
    '    rest="${jdbc#${proto}}"' \
    '    rest_no_host="${rest#*/}"' \
    '    echo "${proto}${new_hostport}/${rest_no_host}" > "$JDBC_FILE"' \
    '    log "JDBC URL  -> $(cat "$JDBC_FILE")"' \
    '  fi' \
    '}' \
    '' \
    'preflight_db() {' \
    '  [ -f "$R2DBC_FILE" ] || return 0' \
    '  url=$(cat "$R2DBC_FILE")' \
    '  log "🔎 URL R2DBC atual: $url"' \
    '  base="${url#r2dbc:mysql://}"' \
    '  hostport="${base%%/*}"' \
    '  host="${hostport%%:*}"' \
    '  port="${hostport#*:}"' \
    '  [ "$port" = "$host" ] && port=3306' \
    '  log "🔎 Host alvo: $host | Porta: $port"' \
    '  gw=$(ip route 2>/dev/null | awk '\''/default/ {print $3; exit}'\'')' \
    '  [ -z "${gw:-}" ] && gw=$(awk '\''$2==00000000 {print $3}'\'' /proc/net/route 2>/dev/null | head -n1)' \
    '  log "🌐 Gateway padrão: ${gw:-desconhecido}"' \
    '  if command -v ip >/dev/null 2>&1; then' \
    '    log "🌐 Interfaces:"' \
    '    ip -o -4 addr show | awk '\''{print "    "$2" -> "$4}'\'' | while read -r line; do log "$line"; done' \
    '  fi' \
    '  if can_connect "$host" "$port"; then' \
    '    log "✅ DB alcançável em $host:$port"' \
    '    return 0' \
    '  fi' \
    '  log "⚠️ DB inacessível em $host:$port. Tentando fallbacks..."' \
    '  if can_connect "conexao-mysql" "$port"; then' \
    '    log "🔁 Alternando para conexao-mysql:$port"' \
    '    rewrite_urls "conexao-mysql:$port"' \
    '    return 0' \
    '  fi' \
    '  if can_connect "host.docker.internal" "$port"; then' \
    '    log "🔁 Alternando para host.docker.internal:$port"' \
    '    rewrite_urls "host.docker.internal:$port"' \
    '    return 0' \
    '  fi' \
    '  if [ -n "${gw:-}" ] && can_connect "$gw" "$port"; then' \
    '    log "🔁 Alternando para gateway $gw:$port"' \
    '    rewrite_urls "$gw:$port"' \
    '    return 0' \
    '  fi' \
    '  if can_connect "127.0.0.1" "$port" || can_connect "localhost" "$port"; then' \
    '    log "🔁 Alternando para localhost:$port"' \
    '    rewrite_urls "127.0.0.1:$port"' \
    '    return 0' \
    '  fi' \
    '  log "❌ Nenhum host de DB acessível a partir do container. Prosseguindo; a aplicação pode falhar."' \
    '}' \
    '' \
    'preflight_db || true' \
    'log "🚀 Iniciando aplicação Java"' \
    'exec dumb-init -- java -jar /app/app.jar' \
    > /app/docker-entrypoint.sh && \
    chmod +x /app/docker-entrypoint.sh && \
    chown appuser:appgroup /app/docker-entrypoint.sh

# Script de entrada que executa inicialização do DB e depois a aplicação
RUN printf '%s\n' '#!/bin/sh' \
    'set -e' \
    'echo "🚀 Iniciando container resultados..."' \
    '' \
    '# Executar inicialização do database' \
    'if [ -f /app/init-database.sh ]; then' \
    '    echo "🗄️ Executando inicialização do database..."' \
    '    /app/init-database.sh' \
    'else' \
    '    echo "⚠️ Script de inicialização não encontrado, prosseguindo..."' \
    'fi' \
    '' \
    '# Iniciar aplicação Java' \
    'echo "☕ Iniciando aplicação Java..."' \
    'exec dumb-init -- java -jar /app/app.jar' \
    > /app/entrypoint.sh && \
    chmod +x /app/entrypoint.sh && \
    chown appuser:appgroup /app/entrypoint.sh

# Mudar para usuário não-root
USER appuser:appgroup

ENTRYPOINT ["/app/entrypoint.sh"]


# === ESTÁGIO 3: DEBUG (Opcional) ===
FROM runtime AS debug

# Configurar debug remoto (apenas para desenvolvimento, sem perfil fixo)

CMD ["-jar", "app.jar"]
