# Solução Rápida - Runner Offline srv649924

## Status Atual
- **Runner:** srv649924
- **Status:** OFFLINE ❌
- **Labels:** ✅ Corretos (self-hosted, Linux, X64, conexao, conexao-de-sorte-backend-resultados)
- **Problema:** Workflow aguardando runner disponível

## Solução Imediata

### 1. Conectar ao Servidor
```bash
# SSH para o servidor do runner
ssh usuario@srv649924
# ou
ssh usuario@IP_DO_SERVIDOR
```

### 2. Verificar Status do Serviço
```bash
# Verificar se o serviço está rodando
sudo systemctl status actions-runner

# Verificar logs do runner
sudo journalctl -u actions-runner -f --no-pager
```

### 3. Reativar o Runner
```bash
# Iniciar o serviço
sudo systemctl start actions-runner

# Habilitar para inicialização automática
sudo systemctl enable actions-runner

# Verificar se está ativo
sudo systemctl is-active actions-runner
```

### 4. Verificação Rápida
```bash
# Verificar se o processo está rodando
ps aux | grep actions-runner

# Verificar conectividade com GitHub
curl -I https://api.github.com
```

## Comandos de Emergência

### Se o serviço não iniciar:
```bash
# Parar completamente
sudo systemctl stop actions-runner

# Reiniciar manualmente
cd /opt/actions-runner
sudo -u actions-runner ./run.sh
```

### Se houver problemas de token:
```bash
# Reconfigurar o runner
cd /opt/actions-runner
sudo -u actions-runner ./config.sh remove --token NOVO_TOKEN
sudo -u actions-runner ./config.sh --url https://github.com/Wibson82/conexao-de-sorte-backend-resultados --token NOVO_TOKEN --labels conexao,conexao-de-sorte-backend-resultados
```

## Verificação Final

1. **Status no GitHub:**
   ```bash
   gh api repos/Wibson82/conexao-de-sorte-backend-resultados/actions/runners --jq '.runners[] | select(.name == "srv649924") | {name: .name, status: .status}'
   ```

2. **Testar Workflow:**
   - Fazer um push ou executar workflow manualmente
   - Verificar se o job é executado no runner

## Tempo Estimado
- **Reativação:** 2-5 minutos
- **Verificação:** 1-2 minutos
- **Total:** 3-7 minutos

## Contatos de Emergência
- **DevOps:** [inserir contato]
- **Infraestrutura:** [inserir contato]

---
**Criado em:** $(date)
**Status:** Aguardando execução