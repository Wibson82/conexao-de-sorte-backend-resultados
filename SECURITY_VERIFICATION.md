# 🔒 VERIFICAÇÃO DE SEGURANÇA - COMANDOS PÓS-DEPLOY

## **1. VERIFICAÇÃO DE ASSINATURA DE IMAGEM (COSIGN)**

```bash
# Instalar cosign se necessário
curl -O -L "https://github.com/sigstore/cosign/releases/latest/download/cosign-linux-amd64"
sudo mv cosign-linux-amd64 /usr/local/bin/cosign
sudo chmod +x /usr/local/bin/cosign

# Verificar assinatura keyless da imagem
cosign verify \
  --certificate-identity-regexp="https://github.com/conexaodesorte/" \
  --certificate-oidc-issuer="https://token.actions.githubusercontent.com" \
  ghcr.io/conexao-de-sorte/resultados-microservice:latest

# Verificar SBOM
cosign verify-attestation \
  --type="https://spdx.dev/Document" \
  --certificate-identity-regexp="https://github.com/conexaodesorte/" \
  --certificate-oidc-issuer="https://token.actions.githubusercontent.com" \
  ghcr.io/conexao-de-sorte/resultados-microservice:latest

# Verificar proveniência
cosign verify-attestation \
  --type="https://slsa.dev/provenance/v1" \
  --certificate-identity-regexp="https://github.com/conexaodesorte/" \
  --certificate-oidc-issuer="https://token.actions.githubusercontent.com" \
  ghcr.io/conexao-de-sorte/resultados-microservice:latest
```

## **2. VERIFICAÇÃO DE AUSÊNCIA DE SEGREDOS EM VARIÁVEIS DE AMBIENTE**

```bash
# Verificar que não há segredos em env vars do container
docker inspect resultados-microservice | jq '.[]|.Config.Env[]' | \
  grep -v -E "(JAVA_OPTS|TZ|SPRING_PROFILES_ACTIVE|SERVER_PORT|ENVIRONMENT)" | \
  grep -i -E "(password|secret|key|token|credential)"

# Deve retornar vazio ou só variáveis não sensíveis
# Se encontrar algo, é uma falha de segurança
```

## **3. VERIFICAÇÃO DE PERMISSÕES DOS SECRETS**

```bash
# Verificar estrutura de diretórios de secrets
ls -la /run/secrets/resultados/
# Deve mostrar:
# -r--------  1 root root  <size> <date> DB_PASSWORD
# -r--------  1 root root  <size> <date> JWT_SIGNING_KEY
# etc.

# Verificar permissões específicas
stat /run/secrets/resultados/DB_PASSWORD
# Deve mostrar: Access: (0400/-r--------) Uid: (0/root) Gid: (0/root)

# Verificar que arquivos não estão vazios
find /run/secrets/resultados -type f -empty
# Deve retornar vazio (nenhum arquivo vazio)

# Verificar conteúdo sem expor (apenas tamanho)
wc -c /run/secrets/resultados/* | grep -v " 0 "
# Deve mostrar arquivos com tamanho > 0
```

## **4. VERIFICAÇÃO DE ENDPOINTS ACTUATOR SEGUROS**

```bash
# Health check deve funcionar
curl -f http://localhost:8080/actuator/health
# Deve retornar: {"status":"UP"}

# Endpoints sensíveis devem estar bloqueados
curl -s http://localhost:8080/actuator/env && echo "❌ ENV ENDPOINT EXPOSTO" || echo "✅ ENV protegido"
curl -s http://localhost:8080/actuator/configprops && echo "❌ CONFIGPROPS EXPOSTO" || echo "✅ CONFIGPROPS protegido"
curl -s http://localhost:8080/actuator/beans && echo "❌ BEANS EXPOSTO" || echo "✅ BEANS protegido"
curl -s http://localhost:8080/actuator/threaddump && echo "❌ THREADDUMP EXPOSTO" || echo "✅ THREADDUMP protegido"

# Info deve funcionar (não sensível)
curl -f http://localhost:8080/actuator/info
```

## **5. VERIFICAÇÃO DE VAZAMENTO NOS LOGS**

```bash
# Verificar logs recentes não contêm secrets
docker logs resultados-microservice --since="1h" 2>&1 | \
  grep -i -E "(password|secret|key|credential|token)" | \
  grep -v -E "(jwt.*validation|key.*rotation|secret.*loaded)" && \
  echo "❌ POSSÍVEL VAZAMENTO NOS LOGS" || echo "✅ Logs seguros"

# Verificar logs de sistema
journalctl -u docker --since="1h" | \
  grep -i -E "(password|secret|key)" && \
  echo "❌ POSSÍVEL VAZAMENTO NO SISTEMA" || echo "✅ Sistema seguro"
```

## **6. VERIFICAÇÃO DE CARREGAMENTO DO CONFIGTREE**

```bash
# Verificar que Spring está carregando secrets via configtree
docker logs resultados-microservice 2>&1 | grep -i configtree
# Deve mostrar: "Loading configuration from configtree"

# Verificar que não há erros de carregamento de propriedades
docker logs resultados-microservice 2>&1 | grep -i -E "(error.*property|failed.*load|configuration.*error)"
# Não deve mostrar erros relacionados a propriedades

# Verificar conexão com banco de dados funcionando
curl -f http://localhost:8080/actuator/health/db
# Deve retornar: {"status":"UP"}
```

## **7. VERIFICAÇÃO DE CONECTIVIDADE JWT**

```bash
# Se o microserviço de auth estiver rodando, testar JWT
# (Substituir por endpoint real de validação)
curl -H "Authorization: Bearer <test-jwt-token>" \
  http://localhost:8080/rest/v1/resultados/test
# Deve retornar 200 com token válido, 401 sem token
```

## **8. VERIFICAÇÃO DE ROTAÇÃO DE CHAVES**

```bash
# Verificar data de criação das chaves JWT no Key Vault
az keyvault secret show --vault-name "kv-conexao-de-sorte" \
  --name "conexao-de-sorte-jwt-key-id" --query "attributes.created" -o tsv

# Verificar próxima data de rotação
az keyvault secret show --vault-name "kv-conexao-de-sorte" \
  --name "conexao-de-sorte-encryption-master-key" \
  --query "attributes.expires" -o tsv
```

## **9. SCRIPT DE VERIFICAÇÃO COMPLETA**

```bash
#!/bin/bash
# verify-security.sh - Script de verificação completa

set -euo pipefail

echo "🔒 VERIFICAÇÃO COMPLETA DE SEGURANÇA"
echo "===================================="

# 1. Verificar container está rodando
if ! docker ps | grep -q resultados-microservice; then
    echo "❌ Container não está rodando"
    exit 1
fi
echo "✅ Container está rodando"

# 2. Verificar health
if curl -f -s http://localhost:8080/actuator/health > /dev/null; then
    echo "✅ Health check passou"
else
    echo "❌ Health check falhou"
    exit 1
fi

# 3. Verificar endpoints sensíveis bloqueados
if curl -f -s http://localhost:8080/actuator/env > /dev/null; then
    echo "❌ Endpoint /env está exposto"
    exit 1
else
    echo "✅ Endpoint /env está protegido"
fi

# 4. Verificar secrets existem e têm permissões corretas
if [[ ! -d "/run/secrets/resultados" ]]; then
    echo "❌ Diretório de secrets não existe"
    exit 1
fi

for secret in DB_PASSWORD JWT_SIGNING_KEY ENCRYPTION_MASTER_KEY; do
    if [[ ! -f "/run/secrets/resultados/$secret" ]]; then
        echo "❌ Secret $secret não existe"
        exit 1
    fi
    
    PERMS=$(stat -c "%a" "/run/secrets/resultados/$secret")
    if [[ "$PERMS" != "400" ]]; then
        echo "❌ Secret $secret tem permissões incorretas: $PERMS"
        exit 1
    fi
done
echo "✅ Todos os secrets existem com permissões corretas"

# 5. Verificar não há vazamento em env vars
if docker inspect resultados-microservice | jq '.[]|.Config.Env[]' | \
   grep -i -E "(password|secret|key)" | \
   grep -v -E "(JAVA_OPTS|SPRING_|TZ)" > /dev/null; then
    echo "❌ Possível vazamento em variáveis de ambiente"
    exit 1
else
    echo "✅ Nenhum segredo em variáveis de ambiente"
fi

echo ""
echo "🎉 VERIFICAÇÃO COMPLETA: TODAS AS CHECAGENS PASSARAM"
echo "✅ Sistema está seguro e em conformidade"
```

## **10. MONITORAMENTO CONTÍNUO**

```bash
# Configurar alertas para expiração de chaves (crontab)
0 9 * * * /usr/local/bin/check-key-expiration.sh

# Script de monitoramento de expiração
cat > /usr/local/bin/check-key-expiration.sh << 'EOF'
#!/bin/bash
VAULT_NAME="kv-conexao-de-sorte"
DAYS_WARNING=30

EXPIRES=$(az keyvault secret show --vault-name "$VAULT_NAME" \
  --name "conexao-de-sorte-encryption-master-key" \
  --query "attributes.expires" -o tsv)

if [[ -n "$EXPIRES" ]]; then
    EXPIRES_EPOCH=$(date -d "$EXPIRES" +%s)
    NOW_EPOCH=$(date +%s)
    DAYS_LEFT=$(( (EXPIRES_EPOCH - NOW_EPOCH) / 86400 ))
    
    if [[ $DAYS_LEFT -le $DAYS_WARNING ]]; then
        echo "⚠️ ALERTA: Chave expira em $DAYS_LEFT dias!"
        # Enviar alerta (email, Slack, etc.)
    fi
fi
EOF
chmod +x /usr/local/bin/check-key-expiration.sh
```
