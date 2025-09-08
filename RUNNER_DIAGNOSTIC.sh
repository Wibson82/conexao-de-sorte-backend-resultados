#!/bin/bash

# Script de Diagnóstico Completo do Runner srv649924
# Identifica e corrige problemas de permissão e conectividade

set -e

echo "🔍 DIAGNÓSTICO COMPLETO DO RUNNER srv649924"
echo "============================================="
echo


# Função para log colorido
log_info() { echo -e "\033[34m[INFO]\033[0m $1"; }
log_warn() { echo -e "\033[33m[WARN]\033[0m $1"; }
log_error() { echo -e "\033[31m[ERROR]\033[0m $1"; }
log_success() { echo -e "\033[32m[SUCCESS]\033[0m $1"; }

# 1. VERIFICAR STATUS ATUAL
echo "📊 1. STATUS ATUAL DO RUNNER"
echo "----------------------------"
log_info "Verificando status do serviço..."
sudo systemctl status actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service --no-pager -l
echo

# 2. ANALISAR LOGS DETALHADOS
echo "📋 2. ANÁLISE DE LOGS DETALHADOS"
echo "--------------------------------"
log_info "Últimos 50 logs do runner..."
sudo journalctl -u actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service -n 50 --no-pager
echo

# 3. VERIFICAR DIRETÓRIO E PERMISSÕES
echo "📁 3. VERIFICAÇÃO DE DIRETÓRIOS E PERMISSÕES"
echo "--------------------------------------------"
log_info "Verificando diretório do runner..."
ls -la /opt/actions-runner/
echo

log_info "Verificando permissões dos arquivos críticos..."
ls -la /opt/actions-runner/runsvc.sh
ls -la /opt/actions-runner/bin/RunnerService.js
ls -la /opt/actions-runner/externals/node20/bin/node
echo

# 4. VERIFICAR USUÁRIO E GRUPOS
echo "👤 4. VERIFICAÇÃO DE USUÁRIO E GRUPOS"
echo "------------------------------------"
log_info "Verificando usuário do runner..."
ps aux | grep RunnerService.js | grep -v grep
echo

log_info "Verificando grupos do usuário actions-runner..."
id actions-runner 2>/dev/null || log_warn "Usuário actions-runner não encontrado"
echo

# 5. TESTAR CONECTIVIDADE
echo "🌐 5. TESTE DE CONECTIVIDADE"
echo "----------------------------"
log_info "Testando conectividade com GitHub..."
curl -s -o /dev/null -w "Status HTTP: %{http_code}\nTempo: %{time_total}s\n" https://api.github.com
echo

log_info "Testando resolução DNS..."
nslookup github.com
echo

# 6. VERIFICAR ESPAÇO EM DISCO
echo "💾 6. VERIFICAÇÃO DE ESPAÇO EM DISCO"
echo "-----------------------------------"
log_info "Espaço disponível..."
df -h /opt/actions-runner/
echo

log_info "Inodes disponíveis..."
df -i /opt/actions-runner/
echo

# 7. VERIFICAR PROCESSOS
echo "⚙️ 7. VERIFICAÇÃO DE PROCESSOS"
echo "------------------------------"
log_info "Processos relacionados ao runner..."
ps aux | grep -E "(runner|github|actions)" | grep -v grep
echo

# 8. CORREÇÕES AUTOMÁTICAS
echo "🔧 8. APLICANDO CORREÇÕES AUTOMÁTICAS"
echo "------------------------------------"

# Parar o serviço
log_info "Parando o serviço do runner..."
sudo systemctl stop actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service
sleep 3

# Corrigir permissões
log_info "Corrigindo permissões do diretório..."
sudo chown -R actions-runner:actions-runner /opt/actions-runner/
sudo chmod +x /opt/actions-runner/runsvc.sh
sudo chmod +x /opt/actions-runner/externals/node20/bin/node
sudo chmod +x /opt/actions-runner/bin/RunnerService.js

# Limpar logs antigos se necessário
log_info "Limpando logs antigos..."
sudo find /opt/actions-runner/_diag/ -name "*.log" -mtime +7 -delete 2>/dev/null || true

# Verificar e criar diretórios necessários
log_info "Verificando diretórios necessários..."
sudo mkdir -p /opt/actions-runner/_diag
sudo mkdir -p /opt/actions-runner/_work
sudo chown -R actions-runner:actions-runner /opt/actions-runner/_diag
sudo chown -R actions-runner:actions-runner /opt/actions-runner/_work

# Reiniciar o serviço
log_info "Reiniciando o serviço do runner..."
sudo systemctl start actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service
sleep 5

# 9. VERIFICAÇÃO FINAL
echo "✅ 9. VERIFICAÇÃO FINAL"
echo "----------------------"
log_info "Status após correções..."
sudo systemctl status actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service --no-pager
echo

log_info "Verificando no GitHub..."
gh api repos/Wibson82/conexao-de-sorte-backend-resultados/actions/runners --jq '.runners[] | select(.name == "srv649924") | {name: .name, status: .status, busy: .busy}'
echo

# 10. MONITORAMENTO EM TEMPO REAL
echo "📡 10. INICIANDO MONITORAMENTO"
echo "-----------------------------"
log_info "Monitorando logs em tempo real por 30 segundos..."
log_info "Pressione Ctrl+C para interromper"
echo

timeout 30s sudo journalctl -u actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service -f --no-pager || true

echo
log_success "Diagnóstico completo finalizado!"
echo
echo "📋 PRÓXIMOS PASSOS:"
echo "1. Verificar se o runner aparece como 'online' no GitHub"
echo "2. Testar execução de um workflow simples"
echo "3. Monitorar logs por alguns minutos"
echo
echo "🔧 COMANDOS ÚTEIS:"
echo "- Status: sudo systemctl status actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service"
echo "- Logs: sudo journalctl -u actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service -f"
echo "- GitHub: gh api repos/Wibson82/conexao-de-sorte-backend-resultados/actions/runners"
echo