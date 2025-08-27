#!/bin/bash
# =============================================================================
# 🔐 SETUP SECRETS - CONFIGURAÇÃO SEGURA DE SEGREDOS NO SERVIDOR
# =============================================================================
# Script para configurar secrets do Azure Key Vault no servidor de produção
# usando o padrão /run/secrets com configtree do Spring Boot
# =============================================================================

set -euo pipefail

# ===== CONFIGURAÇÕES =====
SECRETS_DIR="/run/secrets"
SERVICE_USER="appuser"
VAULT_NAME="${AZURE_KEYVAULT_NAME:-kv-conexao-de-sorte}"
LOG_FILE="/var/log/setup-secrets.log"

# ===== FUNÇÕES =====
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') [INFO] $1" | tee -a "$LOG_FILE"
}

error() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') [ERROR] $1" | tee -a "$LOG_FILE" >&2
    exit 1
}

create_secret_file() {
    local name=$1
    local value=$2
    local file="$SECRETS_DIR/$name"
    
    if [[ -z "$value" || "$value" == "null" ]]; then
        log "⚠️ Skipping $name (empty or null value)"
        return
    fi
    
    log "📝 Creating secret file: $name"
    
    # Create secret file with secure permissions
    echo "$value" | sudo tee "$file" > /dev/null
    
    # Set ownership and permissions
    sudo chown root:root "$file"
    sudo chmod 0400 "$file"
    
    # Allow app user to read via ACL (if available) or group
    if command -v setfacl >/dev/null 2>&1; then
        sudo setfacl -m u:$SERVICE_USER:r "$file" || {
            log "⚠️ ACL not available, using group permissions"
            sudo chgrp $SERVICE_USER "$file"
            sudo chmod 0440 "$file"
        }
    else
        sudo chgrp $SERVICE_USER "$file"
        sudo chmod 0440 "$file"
    fi
    
    log "✅ Secret $name created successfully"
}

# ===== MAIN EXECUTION =====
main() {
    log "🔐 Starting secure secrets setup for resultados microservice..."
    
    # Create service user if not exists
    if ! id "$SERVICE_USER" >/dev/null 2>&1; then
        log "👤 Creating service user: $SERVICE_USER"
        sudo useradd -r -s /bin/false -M "$SERVICE_USER"
    fi
    
    # Create secrets directory
    log "📁 Setting up secrets directory: $SECRETS_DIR"
    sudo mkdir -p "$SECRETS_DIR"
    sudo chown root:root "$SECRETS_DIR"
    sudo chmod 755 "$SECRETS_DIR"
    
    # Authenticate with Azure using Managed Identity or Service Principal
    log "🔐 Authenticating with Azure..."
    if ! az account show >/dev/null 2>&1; then
        # Try managed identity first
        az login --identity || {
            error "❌ Azure authentication failed. Ensure managed identity or service principal is configured."
        }
    fi
    
    log "🔍 Fetching secrets from Azure Key Vault: $VAULT_NAME"
    
    # Fetch and create secret files
    # Database secrets
    log "🗄️ Processing database secrets..."
    # Preferred secret names (R2DBC)
    DB_R2DBC_URL=$(az keyvault secret show --vault-name "$VAULT_NAME" --name "conexao-de-sorte-database-r2dbc-url" --query value -o tsv 2>/dev/null || echo "")
    DB_USER=$(az keyvault secret show --vault-name "$VAULT_NAME" --name "conexao-de-sorte-database-username" --query value -o tsv 2>/dev/null || echo "")
    DB_PASSWORD=$(az keyvault secret show --vault-name "$VAULT_NAME" --name "conexao-de-sorte-database-password" --query value -o tsv 2>/dev/null || echo "")
    # Backward-compatible fallback (deprecated): conexao-de-sorte-database-url
    if [[ -z "$DB_R2DBC_URL" || "$DB_R2DBC_URL" == "null" ]]; then
        DB_R2DBC_URL=$(az keyvault secret show --vault-name "$VAULT_NAME" --name "conexao-de-sorte-database-url" --query value -o tsv 2>/dev/null || echo "")
    fi

    # Determine database name per microservice (resultados)
    # Priority:
    # 1) Specific secret: conexao-de-sorte-resultados-database-name
    # 2) Generic secret:  conexao-de-sorte-database-name
    # 3) Env var:         DATABASE_NAME
    # 4) Conventional:    conexao_sorte_resultados
    DB_NAME_SPECIFIC=$(az keyvault secret show --vault-name "$VAULT_NAME" --name "conexao-de-sorte-resultados-database-name" --query value -o tsv 2>/dev/null || echo "")
    DB_NAME_GENERIC=$(az keyvault secret show --vault-name "$VAULT_NAME" --name "conexao-de-sorte-database-name" --query value -o tsv 2>/dev/null || echo "")
    DB_NAME=${DB_NAME_SPECIFIC:-}
    if [[ -z "$DB_NAME" || "$DB_NAME" == "null" ]]; then
        DB_NAME=${DB_NAME_GENERIC:-}
    fi
    if [[ -z "$DB_NAME" || "$DB_NAME" == "null" ]]; then
        DB_NAME=${DATABASE_NAME:-}
    fi
    if [[ -z "$DB_NAME" || "$DB_NAME" == "null" ]]; then
        DB_NAME="conexao_sorte_resultados"
    fi

    # Optional host override via secret or env to support docker host connectivity
    DB_HOST_OVERRIDE_SECRET=$(az keyvault secret show --vault-name "$VAULT_NAME" --name "conexao-de-sorte-database-host-override" --query value -o tsv 2>/dev/null || echo "")
    DB_HOST_OVERRIDE="${DB_HOST_OVERRIDE:-}"
    [[ -z "$DB_HOST_OVERRIDE" || "$DB_HOST_OVERRIDE" == "null" ]] && DB_HOST_OVERRIDE="$DB_HOST_OVERRIDE_SECRET"

    # Normalize URL: inject DB name if missing; override host when requested or when localhost is used
    if [[ -n "$DB_R2DBC_URL" && "$DB_R2DBC_URL" != "null" ]]; then
        # Ensure a db path exists
        if [[ "$DB_R2DBC_URL" =~ ^r2dbc:mysql://[^/]+$ ]]; then
            DB_R2DBC_URL="$DB_R2DBC_URL/$DB_NAME"
        fi

        # Extract components r2dbc:mysql://host[:port]/rest
        proto_prefix="r2dbc:mysql://"
        remainder="${DB_R2DBC_URL#${proto_prefix}}"
        hostport="${remainder%%/*}"
        restpath="${remainder#*/}"
        # Split host and port
        host="${hostport%%:*}"
        port="${hostport#*:}"
        [[ "$port" == "$host" ]] && port="3306"

        # Apply override rules
        if [[ -n "$DB_HOST_OVERRIDE" && "$DB_HOST_OVERRIDE" != "null" ]]; then
            hostport="$DB_HOST_OVERRIDE"
        elif [[ "$host" == "localhost" || "$host" == "127.0.0.1" ]]; then
            hostport="host.docker.internal:${port}"
        fi

        DB_R2DBC_URL="${proto_prefix}${hostport}/${restpath}"
    fi

    # Expor secrets para Spring via configtree
    create_secret_file "spring.r2dbc.url" "$DB_R2DBC_URL"
    create_secret_file "spring.r2dbc.username" "$DB_USER"
    create_secret_file "spring.r2dbc.password" "$DB_PASSWORD"

    # Gerar propriedades de Flyway a partir do R2DBC URL (when possible)
    if [[ -n "$DB_R2DBC_URL" && "$DB_R2DBC_URL" != "null" ]]; then
        # Convert r2dbc:mysql://host:port/db?...  -> jdbc:mysql://host:port/db?... (remover params incompatíveis se necessário)
        JDBC_URL="${DB_R2DBC_URL/r2dbc:mysql:/jdbc:mysql:}"
        create_secret_file "spring.flyway.url" "$JDBC_URL"
    fi
    create_secret_file "spring.flyway.user" "$DB_USER"
    create_secret_file "spring.flyway.password" "$DB_PASSWORD"
    
    # Redis secrets
    log "🚀 Processing Redis secrets..."
    REDIS_HOST=$(az keyvault secret show --vault-name "$VAULT_NAME" --name "conexao-de-sorte-redis-host" --query value -o tsv 2>/dev/null || echo "redis")
    REDIS_PORT=$(az keyvault secret show --vault-name "$VAULT_NAME" --name "conexao-de-sorte-redis-port" --query value -o tsv 2>/dev/null || echo "6379")
    REDIS_PASSWORD=$(az keyvault secret show --vault-name "$VAULT_NAME" --name "conexao-de-sorte-redis-password" --query value -o tsv 2>/dev/null || echo "")
    
    create_secret_file "REDIS_HOST" "$REDIS_HOST"
    create_secret_file "REDIS_PORT" "$REDIS_PORT"
    create_secret_file "REDIS_PASSWORD" "$REDIS_PASSWORD"
    
    # JWT secrets (base64 decode for keys)
    log "🔑 Processing JWT secrets..."
    JWT_SIGNING_KEY_B64=$(az keyvault secret show --vault-name "$VAULT_NAME" --name "conexao-de-sorte-jwt-signing-key" --query value -o tsv 2>/dev/null || echo "")
    JWT_VERIFICATION_KEY_B64=$(az keyvault secret show --vault-name "$VAULT_NAME" --name "conexao-de-sorte-jwt-verification-key" --query value -o tsv 2>/dev/null || echo "")
    JWT_KEY_ID=$(az keyvault secret show --vault-name "$VAULT_NAME" --name "conexao-de-sorte-jwt-key-id" --query value -o tsv 2>/dev/null || echo "")
    JWT_SECRET=$(az keyvault secret show --vault-name "$VAULT_NAME" --name "conexao-de-sorte-jwt-secret" --query value -o tsv 2>/dev/null || echo "")
    
    # Decode base64 keys if they exist
    if [[ -n "$JWT_SIGNING_KEY_B64" && "$JWT_SIGNING_KEY_B64" != "null" ]]; then
        JWT_SIGNING_KEY=$(echo "$JWT_SIGNING_KEY_B64" | base64 -d)
        create_secret_file "JWT_SIGNING_KEY" "$JWT_SIGNING_KEY"
    fi
    
    if [[ -n "$JWT_VERIFICATION_KEY_B64" && "$JWT_VERIFICATION_KEY_B64" != "null" ]]; then
        JWT_VERIFICATION_KEY=$(echo "$JWT_VERIFICATION_KEY_B64" | base64 -d)
        create_secret_file "JWT_VERIFICATION_KEY" "$JWT_VERIFICATION_KEY"
    fi
    
    create_secret_file "JWT_KEY_ID" "$JWT_KEY_ID"
    create_secret_file "JWT_SECRET" "$JWT_SECRET"
    
    # Encryption secrets
    log "🔐 Processing encryption secrets..."
    ENCRYPTION_MASTER_KEY=$(az keyvault secret show --vault-name "$VAULT_NAME" --name "conexao-de-sorte-encryption-master-key" --query value -o tsv 2>/dev/null || echo "")
    create_secret_file "ENCRYPTION_MASTER_KEY" "$ENCRYPTION_MASTER_KEY"
    
    # Clear sensitive variables from memory
    unset DB_PASSWORD REDIS_PASSWORD JWT_SECRET ENCRYPTION_MASTER_KEY
    unset JWT_SIGNING_KEY JWT_VERIFICATION_KEY JWT_SIGNING_KEY_B64 JWT_VERIFICATION_KEY_B64
    
    log "📋 Secrets setup verification:"
    ls -la "$SECRETS_DIR" | grep -v -E '^\s*total' || true
    
    log "✅ Secrets setup completed successfully!"
    log "📋 Next steps:"
    log "   1. Restart the resultados microservice"
    log "   2. Verify configtree is loading secrets correctly"
    log "   3. Check application logs for any errors"
    
    return 0
}

# ===== EXECUTION =====
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    # Check if running as root or with sudo
    if [[ $EUID -ne 0 ]]; then
        error "❌ This script must be run as root or with sudo"
    fi
    
    # Validate environment
    if ! command -v az >/dev/null 2>&1; then
        error "❌ Azure CLI not found. Please install it first."
    fi
    
    # Get vault name from environment or use default
    VAULT_NAME="${AZURE_KEYVAULT_NAME:-$VAULT_NAME}"
    
    log "🚀 Starting secrets setup with vault: $VAULT_NAME"
    main "$@"
fi
