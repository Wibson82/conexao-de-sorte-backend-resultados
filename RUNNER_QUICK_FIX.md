# ✅ SOLUÇÃO: Runner srv649924 Offline

## 🔍 Diagnóstico Confirmado
- **Problema:** Runner `srv649924` está **OFFLINE**
- **Causa:** Serviço `actions-runner` parado no servidor
- **Impacto:** Workflow aguarda runner disponível indefinidamente
- **Labels:** ✅ Corretos (self-hosted, conexao-de-sorte-backend-resultados)

## 🚀 Solução Passo-a-Passo

### Passo 1: Conectar ao Servidor
```bash
# Conectar via SSH ao servidor do runner
ssh usuario@srv649924
# OU usando IP se hostname não resolver
ssh usuario@IP_DO_SERVIDOR_649924
```

### Passo 2: Verificar Status Atual
```bash
# Verificar se o serviço existe e seu status
sudo systemctl status actions-runner

# Verificar processos relacionados
ps aux | grep -i runner

# Verificar logs recentes
sudo journalctl -u actions-runner --no-pager -n 20
```

### Passo 3: Reativar o Runner
```bash
# Iniciar o serviço
sudo systemctl start actions-runner

# Verificar se iniciou corretamente
sudo systemctl status actions-runner

# Habilitar inicialização automática
sudo systemctl enable actions-runner
```

### Passo 4: Verificação Imediata
```bash
# Confirmar que está rodando
sudo systemctl is-active actions-runner
# Deve retornar: active

# Verificar conectividade com GitHub
curl -s -o /dev/null -w "%{http_code}" https://api.github.com
# Deve retornar: 200
```

## 🔧 Se o Serviço Não Iniciar

### Opção A: Reinicialização Manual
```bash
# Parar completamente
sudo systemctl stop actions-runner

# Ir para diretório do runner
cd /opt/actions-runner
# OU
cd /home/actions-runner/actions-runner

# Executar manualmente para ver erros
sudo -u actions-runner ./run.sh
```

### Opção B: Reconfiguração Completa
```bash
# Remover configuração atual
cd /opt/actions-runner
sudo -u actions-runner ./config.sh remove --token SEU_TOKEN_AQUI

# Reconfigurar com novo token
sudo -u actions-runner ./config.sh \
  --url https://github.com/Wibson82/conexao-de-sorte-backend-resultados \
  --token SEU_TOKEN_AQUI \
  --labels conexao,conexao-de-sorte-backend-resultados \
  --name srv649924

# Reinstalar como serviço
sudo ./svc.sh install
sudo ./svc.sh start
```

## 📊 Verificação Final

### No Servidor:
```bash
# Status do serviço
sudo systemctl status actions-runner

# Logs em tempo real
sudo journalctl -u actions-runner -f
```

### No GitHub (via CLI):
```bash
# Verificar status do runner
gh api repos/Wibson82/conexao-de-sorte-backend-resultados/actions/runners \
  --jq '.runners[] | select(.name == "srv649924") | {name: .name, status: .status}'
```

### Resultado Esperado:
```json
{
  "name": "srv649924",
  "status": "online"
}
```

## ⚡ Teste Rápido

1. **Executar workflow manualmente:**
   - Ir para Actions no GitHub
   - Executar "Resultados - CI/CD Pipeline"
   - Verificar se job `build-deploy-selfhosted` inicia

2. **Ou fazer push simples:**
   ```bash
   git commit --allow-empty -m "test: verificar runner online"
   git push
   ```

## 🕐 Tempo Estimado
- **Reativação simples:** 2-3 minutos
- **Reconfiguração completa:** 5-10 minutos
- **Verificação:** 1-2 minutos

## 📞 Próximos Passos
1. ✅ Conectar ao servidor srv649924
2. ✅ Executar `sudo systemctl start actions-runner`
3. ✅ Verificar status: `sudo systemctl status actions-runner`
4. ✅ Confirmar online no GitHub
5. ✅ Testar workflow

---
**Status:** Runner deve ficar ONLINE após execução dos comandos acima.
**Última atualização:** $(date '+%Y-%m-%d %H:%M:%S')