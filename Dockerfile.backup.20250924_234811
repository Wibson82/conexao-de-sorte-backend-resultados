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
# Run: docker run -p 8083:8083 conexaodesorte/resultados:latest
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

# Criar diretórios necessários
RUN mkdir -p /app/logs && \
    chown -R appuser:appgroup /app/logs

# Copiar JAR da aplicação do estágio de build
COPY --from=builder --chown=appuser:appgroup /build/target/*.jar app.jar

# Copy custom entrypoint script
COPY --chown=appuser:appgroup docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

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

# Health check nativo
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

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

# Mudar para usuário não-root
USER appuser:appgroup

# Start the application with custom entrypoint
ENTRYPOINT ["/app/docker-entrypoint.sh"]
CMD ["java", "-jar", "app.jar"]


# === ESTÁGIO 3: DEBUG (Opcional) ===
FROM runtime AS debug

# Configurar debug remoto (apenas para desenvolvimento, sem perfil fixo)

CMD ["-jar", "app.jar"]
