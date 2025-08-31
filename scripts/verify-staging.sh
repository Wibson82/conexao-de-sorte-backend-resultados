#!/usr/bin/env bash
set -euo pipefail

# Verify staging readiness for Results service
# Usage: BASE_URL=http://localhost:8082 ./scripts/verify-staging.sh

BASE_URL="${BASE_URL:-http://localhost:8082}"
echo "[resultados] Verificando em: $BASE_URL"

curl -fsS "$BASE_URL/actuator/health" >/dev/null && echo "[resultados] ✅ actuator/health"
curl -fsS "$BASE_URL/actuator/metrics" >/dev/null && echo "[resultados] ✅ actuator/metrics"

code=$(curl -s -o /dev/null -w '%{http_code}' "$BASE_URL/rest/v1/resultados")
echo "[resultados] resultados -> HTTP $code"

echo "[resultados] ✅ Verificações básicas concluídas"

