# 🚨 Runner Self-hosted Offline - Troubleshooting

## 📊 Status Atual

**Runner Identificado:**
- **Nome:** srv649924
- **Status:** ❌ OFFLINE
- **Labels:** ✅ Corretos (`self-hosted`, `Linux`, `X64`, `conexao`, `conexao-de-sorte-backend-resultados`)

## 🔍 Diagnóstico

O runner está registrado corretamente no GitHub com todos os labels necessários, mas está **offline**. Isso significa que:

1. ✅ O registro foi feito corretamente
2. ✅ Os labels estão corretos
3. ❌ O serviço do runner não está executando

## 🛠️ Soluções Imediatas

### 1. Conectar ao Servidor (srv649924)

```bash
# SSH para o servidor
ssh usuario@srv649924
# ou
ssh usuario@IP_DO_SERVIDOR
```

### 2. Verificar Status do Serviço

```bash
# Verificar se o serviço está rodando
sudo systemctl status actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service

# Verificar todos os serviços do runner
sudo systemctl list-units --type=service | grep actions.runner
```

### 3. Iniciar o Serviço

```bash
# Iniciar o serviço
sudo systemctl start actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service

# Habilitar para iniciar automaticamente
sudo systemctl enable actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service

# Verificar status após iniciar
sudo systemctl status actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service
```

### 4. Verificar Logs do Runner

```bash
# Ver logs do serviço
sudo journalctl -u actions.runner.Wibson82-conexao-de-sorte-backend-resultados.srv649924.service -f

# Ver logs específicos do runner
cd /home/runner/actions-runner
tail -f _diag/Runner_*.log
```

### 5. Reiniciar Manualmente (se necessário)

```bash
# Ir para o diretório do runner
cd /home/runner/actions-runner

# Parar o runner atual
sudo ./svc.sh stop

# Iniciar novamente
sudo ./svc.sh start

# Verificar status
sudo ./svc.sh status
```

## 🔧 Verificações Adicionais

### 1. Conectividade de Rede

```bash
# Testar conectividade com GitHub
curl -I https://api.github.com

# Testar DNS
nslookup github.com

# Verificar portas
telnet api.github.com 443
```

### 2. Espaço em Disco

```bash
# Verificar espaço disponível
df -h

# Limpar logs antigos se necessário
sudo journalctl --vacuum-time=7d

# Limpar cache do Docker
docker system prune -f
```

### 3. Permissões

```bash
# Verificar proprietário dos arquivos
ls -la /home/runner/actions-runner/

# Corrigir permissões se necessário
sudo chown -R runner:runner /home/runner/actions-runner/
```

## 🚀 Teste de Funcionamento

### 1. Verificar Status Online

```bash
# Verificar se o runner aparece online
gh api repos/Wibson82/conexao-de-sorte-backend-resultados/actions/runners --jq '.runners[] | select(.name=="srv649924") | {name: .name, status: .status}'
```

### 2. Executar Workflow de Teste

```bash
# Executar workflow manualmente
gh workflow run ci-cd.yml --repo Wibson82/conexao-de-sorte-backend-resultados

# Monitorar execução
gh run list --repo Wibson82/conexao-de-sorte-backend-resultados --limit 1
```

## 🔄 Reconfiguração Completa (último recurso)

Se o runner continuar offline após as tentativas acima:

### 1. Remover Runner Atual

```bash
# No servidor
cd /home/runner/actions-runner
sudo ./svc.sh stop
sudo ./svc.sh uninstall
./config.sh remove --token <REMOVAL_TOKEN>
```

### 2. Obter Novo Token de Registro

```bash
# Gerar novo token de registro
gh api -X POST repos/Wibson82/conexao-de-sorte-backend-resultados/actions/runners/registration-token --jq '.token'
```

### 3. Registrar Novamente

```bash
# Registrar com o novo token
./config.sh --url https://github.com/Wibson82/conexao-de-sorte-backend-resultados \
  --token <NOVO_TOKEN> \
  --labels self-hosted,conexao-de-sorte-backend-resultados \
  --name "srv649924" \
  --work "_work"

# Instalar como serviço
sudo ./svc.sh install
sudo ./svc.sh start
```

## 📋 Checklist de Verificação

- [ ] SSH conectado ao servidor srv649924
- [ ] Serviço do runner verificado
- [ ] Serviço iniciado/reiniciado
- [ ] Logs verificados sem erros
- [ ] Conectividade de rede testada
- [ ] Espaço em disco suficiente
- [ ] Permissões corretas
- [ ] Runner aparece como "online" no GitHub
- [ ] Workflow executado com sucesso

## 🆘 Contatos de Emergência

- **Servidor:** srv649924
- **Repositório:** Wibson82/conexao-de-sorte-backend-resultados
- **Labels necessários:** `self-hosted`, `conexao-de-sorte-backend-resultados`

---

**Status:** 🔴 Runner Offline  
**Próxima ação:** Conectar ao servidor e iniciar o serviço  
**Prioridade:** ALTA - Bloqueia deploys de produção