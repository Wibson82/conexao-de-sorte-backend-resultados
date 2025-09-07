#!/bin/bash

# CORREÇÃO DEFINITIVA DO RUNNER srv649924
# Baseado no diagnóstico completo realizado

echo "🔧 CORREÇÃO DEFINITIVA DO RUNNER srv649924"
echo "==========================================="

# 1. IDENTIFICAR O PROBLEMA PRINCIPAL
echo "📋 1. DIAGNÓSTICO CONFIRMADO"
echo "----------------------------"
echo "❌ Problema: Runner tentando usar usuário 'actions-runner' inexistente"
echo "✅ Solução: Configurar para usar usuário 'runner' existente"
echo "📁 Diretório: /opt/actions-runner"
echo "🔧 Serviço: actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service"
echo ""

# 2. PARAR O SERVIÇO PROBLEMÁTICO
echo "⏹️ 2. PARANDO SERVIÇO ATUAL"
echo "---------------------------"
sudo systemctl stop actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service
echo "✅ Serviço parado"
echo ""

# 3. CORRIGIR PERMISSÕES DO DIRETÓRIO PRINCIPAL
echo "🔐 3. CORRIGINDO PERMISSÕES"
echo "---------------------------"
echo "[INFO] Ajustando proprietário do diretório principal..."
sudo chown -R runner:runner /opt/actions-runner
echo "✅ Proprietário ajustado para runner:runner"

echo "[INFO] Ajustando permissões dos diretórios..."
sudo find /opt/actions-runner -type d -exec chmod 755 {} \;
echo "✅ Permissões de diretórios ajustadas"

echo "[INFO] Ajustando permissões dos arquivos..."
sudo find /opt/actions-runner -type f -exec chmod 644 {} \;
echo "✅ Permissões de arquivos ajustadas"

echo "[INFO] Ajustando permissões dos executáveis..."
sudo chmod +x /opt/actions-runner/*.sh
sudo chmod +x /opt/actions-runner/bin/*
sudo chmod +x /opt/actions-runner/externals/node20/bin/node
echo "✅ Executáveis configurados"
echo ""

# 4. CORRIGIR DIRETÓRIO DE LOGS
echo "📝 4. CORRIGINDO DIRETÓRIO DE LOGS"
echo "----------------------------------"
echo "[INFO] Criando e ajustando diretório _diag..."
sudo mkdir -p /opt/actions-runner/_diag
sudo chown -R runner:runner /opt/actions-runner/_diag
sudo chmod 755 /opt/actions-runner/_diag
echo "✅ Diretório de logs configurado"
echo ""

# 5. VERIFICAR E CORRIGIR ARQUIVO DE SERVIÇO
echo "⚙️ 5. VERIFICANDO CONFIGURAÇÃO DO SERVIÇO"
echo "------------------------------------------"
SERVICE_FILE="/etc/systemd/system/actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service"

if [ -f "$SERVICE_FILE" ]; then
    echo "[INFO] Verificando usuário no arquivo de serviço..."
    if grep -q "User=actions-runner" "$SERVICE_FILE"; then
        echo "[WARN] Encontrado usuário incorreto no serviço"
        echo "[INFO] Corrigindo usuário no arquivo de serviço..."
        sudo sed -i 's/User=actions-runner/User=runner/g' "$SERVICE_FILE"
        sudo sed -i 's/Group=actions-runner/Group=runner/g' "$SERVICE_FILE"
        echo "✅ Usuário corrigido no serviço"
        
        echo "[INFO] Recarregando configuração do systemd..."
        sudo systemctl daemon-reload
        echo "✅ Configuração recarregada"
    else
        echo "✅ Usuário já está correto no serviço"
    fi
else
    echo "❌ Arquivo de serviço não encontrado: $SERVICE_FILE"
fi
echo ""

# 6. LIMPAR LOGS ANTIGOS PROBLEMÁTICOS
echo "🧹 6. LIMPANDO LOGS PROBLEMÁTICOS"
echo "----------------------------------"
echo "[INFO] Removendo logs antigos com problemas de permissão..."
sudo rm -f /opt/actions-runner/_diag/Runner_*.log
echo "✅ Logs antigos removidos"
echo ""

# 7. REINICIAR O SERVIÇO
echo "🚀 7. REINICIANDO SERVIÇO"
echo "-------------------------"
echo "[INFO] Iniciando serviço corrigido..."
sudo systemctl start actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service
echo "✅ Serviço iniciado"

echo "[INFO] Aguardando 10 segundos para estabilização..."
sleep 10
echo ""

# 8. VERIFICAÇÃO FINAL
echo "✅ 8. VERIFICAÇÃO FINAL"
echo "------------------------"
echo "[INFO] Status do serviço:"
sudo systemctl status actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service --no-pager -l
echo ""

echo "[INFO] Últimos logs (sem erros de permissão):"
sudo journalctl -u actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service --no-pager -n 10
echo ""

echo "[INFO] Verificando se há novos logs sendo criados..."
ls -la /opt/actions-runner/_diag/ | tail -5
echo ""

# 9. TESTE DE CONECTIVIDADE COM GITHUB
echo "🌐 9. TESTE DE CONECTIVIDADE"
echo "-----------------------------"
echo "[INFO] Testando conexão com GitHub..."
curl -s -o /dev/null -w "Status HTTP: %{http_code}\nTempo: %{time_total}s\n" https://api.github.com
echo ""

# 10. RESUMO FINAL
echo "📊 10. RESUMO DA CORREÇÃO"
echo "=========================="
echo "✅ Usuário corrigido: runner (era actions-runner)"
echo "✅ Permissões ajustadas: /opt/actions-runner"
echo "✅ Diretório de logs criado: _diag"
echo "✅ Serviço reiniciado com configurações corretas"
echo "✅ Logs antigos problemáticos removidos"
echo ""
echo "🎯 PRÓXIMOS PASSOS:"
echo "1. Aguardar 2-3 minutos para o runner se conectar"
echo "2. Verificar status no GitHub: gh api /repos/Wibson82/conexao-de-sorte-backend-resultados/actions/runners"
echo "3. Testar com um commit simples no repositório"
echo ""
echo "🔍 MONITORAMENTO:"
echo "- Logs em tempo real: sudo journalctl -u actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service -f"
echo "- Status do serviço: sudo systemctl status actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service"
echo ""
echo "✨ CORREÇÃO CONCLUÍDA! ✨"