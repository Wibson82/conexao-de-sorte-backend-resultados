# ============================================================================
# 🐳 DOCKERFILE MULTI-ESTÁGIO - MICROSERVIÇO RESULTADOS
# ============================================================================
#
# Dockerfile otimizado para microserviço de resultados com:
# - Multi-stage build para reduzir tamanho da imagem
# - Java 24 com JVM otimizada para containers
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
FROM maven:3.9.11-eclipse-temurin-24-alpine AS builder

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
FROM eclipse-temurin:24-jre-alpine AS runtime

# Instalar dependências do sistema
RUN apk add --no-cache \
    tzdata \
    curl \
    dumb-init \
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

# Configurar JVM otimizada para containers e processamento de dados
ENV JAVA_OPTS="\
    -server \
    -XX:+UseContainerSupport \
    -XX:+UnlockExperimentalVMOptions \
    -XX:+UseZGC \
    -XX:+UnlockDiagnosticVMOptions \
    -XX:+UseTransparentHugePages \
    -XX:+OptimizeStringConcat \
    -XX:+UseStringDeduplication \
    -XX:+FlightRecorder \
    -Xms256m \
    -Xmx1024m \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/tmp/heapdump.hprof \
    -Djava.security.egd=file:/dev/./urandom \
    -Djava.awt.headless=true \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=America/Sao_Paulo"

# Variáveis de ambiente da aplicação
ENV SPRING_PROFILES_ACTIVE=prod
ENV SPRING_CONFIG_LOCATION=classpath:/application.yml
ENV SERVER_PORT=8082
ENV MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,prometheus
ENV LOGGING_LEVEL_ROOT=INFO

# Expor porta da aplicação
EXPOSE 8082

# Health check nativo
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

# Mudar para usuário não-root
USER appuser:appgroup

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

# Comando de inicialização com dumb-init para signal handling
ENTRYPOINT ["dumb-init", "--"]
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# === ESTÁGIO 3: DEBUG (Opcional) ===
FROM runtime AS debug

# Configurar debug remoto
ENV JAVA_OPTS="$JAVA_OPTS \
    -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 \
    -Dspring.profiles.active=dev \
    -Dlogging.level.br.tec.facilitaservicos=DEBUG"

# Expor porta de debug
EXPOSE 5005

# Comando para debug
CMD ["sh", "-c", "echo 'Starting RESULTS service in DEBUG mode on port 5005' && java $JAVA_OPTS -jar app.jar"]