# 🏃 Configuração do Runner Self-hosted - Microserviço Resultados

## 📋 Problema Identificado

O workflow está falhando com o erro:
```
Requested labels: self-hosted
Job defined at: Wibson82/conexao-de-sorte-backend-resultados/.github/workflows/ci-cd.yml@refs/heads/main
Waiting for a runner to pick up this job...
```

## ✅ Solução Aplicada

O arquivo `ci-cd.yml` foi corrigido para usar labels específicos:

**ANTES:**
```yaml
runs-on: [self-hosted]
```

**DEPOIS:**
```yaml
runs-on: [self-hosted, conexao-de-sorte-backend-resultados]
```

## 🔧 Configuração Necessária do Runner

### 1. Registro do Runner no GitHub

O runner self-hosted deve ser registrado com os labels corretos:

```bash
# Comando de registro do runner
./config.sh --url https://github.com/Wibson82/conexao-de-sorte-backend-resultados \
  --token <REGISTRATION_TOKEN> \
  --labels self-hosted,conexao-de-sorte-backend-resultados \
  --name "resultados-runner" \
  --work "_work"
```

### 2. Labels Obrigatórios

O runner DEVE ter exatamente estes labels:
- `self-hosted` (automático)
- `conexao-de-sorte-backend-resultados` (específico do serviço)

### 3. Verificação dos Labels

Para verificar se o runner está registrado corretamente:

1. Acesse: `https://github.com/Wibson82/conexao-de-sorte-backend-resultados/settings/actions/runners`
2. Verifique se o runner aparece com status "Online"
3. Confirme se os labels estão corretos: `[self-hosted, conexao-de-sorte-backend-resultados]`

## 🖥️ Requisitos do Servidor

### Sistema Operacional
- Ubuntu 20.04+ (recomendado)
- macOS 10.15+
- Windows Server 2019+

### Dependências Obrigatórias
```bash
# Docker
sudo apt update
sudo apt install -y docker.io docker-compose
sudo usermod -aG docker $USER

# Java 24 (para builds locais se necessário)
wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo apt-key add -
echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
sudo apt update
sudo apt install -y temurin-24-jdk

# Azure CLI (para Key Vault)
curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash
```

### Verificação do Ambiente
```bash
# Verificar Docker
docker --version
docker compose version

# Verificar Java
java -version

# Verificar Azure CLI
az --version
```

## 🔐 Configuração Azure OIDC

### Secrets Necessários no GitHub

Os seguintes secrets devem estar configurados no repositório:

```
AZURE_CLIENT_ID=<service-principal-client-id>
AZURE_TENANT_ID=<azure-tenant-id>
AZURE_SUBSCRIPTION_ID=<azure-subscription-id>
AZURE_KEYVAULT_ENDPOINT=https://kv-conexao-de-sorte.vault.azure.net/
```

### Verificação dos Secrets
```bash
# Listar secrets do repositório
gh secret list --repo Wibson82/conexao-de-sorte-backend-resultados
```

## 🚀 Teste do Runner

### 1. Executar Workflow Manualmente
```bash
# Via GitHub CLI
gh workflow run ci-cd.yml --repo Wibson82/conexao-de-sorte-backend-resultados

# Ou via interface web
# https://github.com/Wibson82/conexao-de-sorte-backend-resultados/actions
```

### 2. Monitorar Execução
```bash
# Ver status dos workflows
gh run list --repo Wibson82/conexao-de-sorte-backend-resultados --limit 5

# Ver logs de uma execução específica
gh run view <RUN_ID> --repo Wibson82/conexao-de-sorte-backend-resultados
```

## 🔍 Troubleshooting

### Runner Não Aparece Online
1. Verificar se o serviço está rodando: `sudo systemctl status actions.runner.*`
2. Verificar logs: `journalctl -u actions.runner.* -f`
3. Reiniciar o serviço: `sudo systemctl restart actions.runner.*`

### Workflow Ainda Aguardando Runner
1. Confirmar labels no arquivo `ci-cd.yml`
2. Verificar labels do runner no GitHub
3. Verificar se o runner está online
4. Verificar se não há outros jobs em execução

### Falha na Autenticação Azure
1. Verificar se os secrets estão configurados
2. Testar autenticação manual no runner:
   ```bash
   az login --service-principal \
     --username $AZURE_CLIENT_ID \
     --password $AZURE_CLIENT_SECRET \
     --tenant $AZURE_TENANT_ID
   ```

## 📝 Checklist de Configuração

- [ ] Runner registrado com labels corretos
- [ ] Runner aparece como "Online" no GitHub
- [ ] Docker instalado e funcionando
- [ ] Java 24 instalado
- [ ] Azure CLI instalado
- [ ] Secrets do Azure configurados no GitHub
- [ ] Workflow executado com sucesso
- [ ] Container deployado e saudável

## 🔗 Links Úteis

- [GitHub Self-hosted Runners](https://docs.github.com/en/actions/hosting-your-own-runners)
- [Azure OIDC com GitHub Actions](https://docs.microsoft.com/en-us/azure/developer/github/connect-from-azure)
- [Docker Installation](https://docs.docker.com/engine/install/ubuntu/)
- [Azure CLI Installation](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli)

---

**Status**: ✅ Configuração corrigida  
**Última atualização**: $(date +'%Y-%m-%d %H:%M:%S')  
**Responsável**: Agente TRAE