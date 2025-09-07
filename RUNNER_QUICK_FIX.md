# 🚨 RUNNER QUICK FIX - srv649924

## 🔍 DIAGNÓSTICO COMPLETO REALIZADO
**Problema Principal Identificado:**
- ❌ Runner tentando usar usuário 'actions-runner' **INEXISTENTE**
- ✅ Usuário correto disponível: **'runner'**
- 🔧 Serviço: **actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service**
- 📁 Diretório: **/opt/actions-runner**

## ⚡ Status Atual (Pós-Diagnóstico)
- 🔄 Runner está **ATIVO** mas com **ERROS DE PERMISSÃO**
- ❌ Falha: `Access to the path '/opt/actions-runner/_diag/Runner_*.log' is denied`
- ❌ Erro: `chown: invalid user: 'actions-runner:actions-runner'`
- 🎯 **SOLUÇÃO CRIADA**: Script de correção automática

## 🛠️ SOLUÇÃO DEFINITIVA

### 📋 Script de Correção Automática
**Arquivo:** `RUNNER_FIX_PERMISSIONS.sh`

**O que o script faz:**
1. ✅ Para o serviço problemático
2. ✅ Corrige proprietário: `actions-runner` → `runner`
3. ✅ Ajusta todas as permissões do diretório `/opt/actions-runner`
4. ✅ Corrige configuração do serviço systemd
5. ✅ Remove logs antigos problemáticos
6. ✅ Reinicia o serviço com configurações corretas
7. ✅ Verifica conectividade com GitHub

**Como executar no servidor srv649924:**
```bash
# Tornar executável e executar
chmod +x RUNNER_FIX_PERMISSIONS.sh
./RUNNER_FIX_PERMISSIONS.sh
```

## 🚀 Comandos Corretos para o Projeto

### Verificar Status do Runner Resultados
```bash
# Status específico do runner resultados
sudo systemctl status actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service

# Verificar se está ativo
sudo systemctl is-active actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service
```

### Gerenciar o Runner Resultados
```bash
# Parar o runner (se necessário)
sudo systemctl stop actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service

# Iniciar o runner
sudo systemctl start actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service

# Reiniciar o runner
sudo systemctl restart actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service

# Habilitar inicialização automática
sudo systemctl enable actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service
```

### Verificar Logs do Runner Resultados
```bash
# Logs em tempo real
sudo journalctl -u actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service -f

# Últimas 50 linhas de log
sudo journalctl -u actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service -n 50

# Logs de hoje
sudo journalctl -u actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service --since today
```

## 🔧 Comandos de Diagnóstico

### Verificar Todos os Runners
```bash
# Listar todos os runners ativos
sudo systemctl list-units --type=service | grep actions.runner

# Status de todos os runners
sudo systemctl status 'actions.runner.*'
```

### Verificar Conectividade
```bash
# Testar conexão com GitHub
curl -s -o /dev/null -w "%{http_code}" https://api.github.com

# Verificar DNS
nslookup github.com

# Verificar portas
netstat -tuln | grep :443
```

## 📊 Verificação no GitHub

### Via CLI do GitHub
```bash
# Status específico do runner resultados
gh api repos/Wibson82/conexao-de-sorte-backend-resultados/actions/runners \
  --jq '.runners[] | select(.name == "srv649924") | {name: .name, status: .status, labels: [.labels[].name]}'
```

### Resultado Esperado
```json
{
  "name": "srv649924",
  "status": "online",
  "labels": [
    "self-hosted",
    "Linux",
    "X64",
    "conexao",
    "conexao-de-sorte-backend-resultados"
  ]
}
```

## ⚡ Teste Rápido do Workflow

### Forçar Execução do Workflow
```bash
# No diretório do projeto
cd /caminho/para/conexao-de-sorte-backend-resultados

# Commit vazio para testar
git commit --allow-empty -m "test: verificar runner resultados online"
git push

# OU executar manualmente via GitHub CLI
gh workflow run "Resultados - CI/CD Pipeline" --ref main
```

## 🛠️ Comandos de Manutenção

### Reiniciar Runner se Necessário
```bash
# Reinicialização completa
sudo systemctl restart actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service

# Verificar se reiniciou corretamente
sudo systemctl status actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service
```

### Monitoramento Contínuo
```bash
# Monitorar logs em tempo real
sudo journalctl -u actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service -f --no-pager

# Verificar uso de recursos
top -p $(pgrep -f "Wibson82-conexao-de-sorte-backend-resultados")
```

## 📋 Checklist de Verificação

- [ ] ✅ Runner está **ACTIVE RUNNING**
- [ ] ✅ Conectividade com GitHub OK
- [ ] ✅ Labels corretos no GitHub
- [ ] ✅ Workflow executa sem aguardar
- [ ] ✅ Logs sem erros críticos

## 🎯 Comandos Essenciais (Resumo)

```bash
# 1. Verificar status
sudo systemctl status actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service

# 2. Reiniciar se necessário
sudo systemctl restart actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service

# 3. Verificar no GitHub
gh api repos/Wibson82/conexao-de-sorte-backend-resultados/actions/runners --jq '.runners[] | select(.name == "srv649924")'

# 4. Testar workflow
git commit --allow-empty -m "test: runner" && git push
```

---
**Status:** Runner específico identificado e comandos atualizados.
**Última atualização:** $(date '+%Y-%m-%d %H:%M:%S')
**Serviço:** `actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service`