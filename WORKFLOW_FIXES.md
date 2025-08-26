# 🔧 Correções dos Erros do Workflow CI/CD

## 📋 Resumo dos Problemas Identificados

Este documento detalha os problemas encontrados no log do workflow e suas respectivas soluções.

## 🔴 Problema Principal: Registry não encontrado

### Erro:
```
ERROR: failed to push ghcr.io/conexao-de-sorte/resultados-microservice:main: denied: not_found: owner not found
```

### Causa:
O repositório `conexao-de-sorte/resultados-microservice` não existe no GitHub Container Registry (GHCR) ou não tem as permissões corretas.

### ✅ Soluções:

1. **Verificar se o repositório existe:**
   - Acessar https://github.com/conexao-de-sorte/resultados-microservice
   - Se não existir, criar o repositório ou ajustar o nome da imagem no workflow

2. **Configurar permissões do GHCR:**
   - Ir em `Settings → Actions → General`
   - Em "Workflow permissions", selecionar "Read and write permissions"
   - Marcar "Allow GitHub Actions to create and approve pull requests"

3. **Verificar permissões do token:**
   - O `GITHUB_TOKEN` deve ter permissões para `packages:write`
   - Verificar se o repositório tem acesso ao GHCR habilitado

4. **✅ Solução Aplicada - Nome da imagem corrigido:**
   ```yaml
   env:
     REGISTRY: ghcr.io
     IMAGE_NAME: ${{ github.repository }}
   ```
   
   Agora o workflow usa automaticamente o nome do repositório GitHub atual (`Wibson82/conexao-de-sorte-backend-resultados`).

## ⚠️ Variáveis não definidas no Dockerfile

### Erro:
```
- UndefinedVar: Usage of undefined variable '$BUILD_DATE' (line 122)
- UndefinedVar: Usage of undefined variable '$VCS_REF' (line 123) 
- UndefinedVar: Usage of undefined variable '$VERSION' (line 121)
```

### ✅ Solução Aplicada:
Corrigido as build-args no workflow para usar variáveis do GitHub Actions:

```yaml
build-args: |
  SERVICE_NAME=${{ env.SERVICE_NAME }}
  BUILD_DATE=${{ github.event.head_commit.timestamp || github.run_id }}
  VCS_REF=${{ github.sha }}
  VERSION=1.0.0
```

## ⚠️ Parâmetro Maven Deprecated

### Warning:
```
[WARNING] Parameter 'optimize' (user property 'maven.compiler.optimize') is deprecated: This property is a no-op in javac.
```

### ✅ Solução Aplicada:
Removido o parâmetro `-Dmaven.compiler.optimize=true` do Dockerfile, pois é deprecated e não tem efeito no javac moderno.

## 🔴 Erro do Cosign na Assinatura de Imagem

### Erro:
```
Error: signing [sha256:...]: accessing entity: GET https://index.docker.io/v2/library/sha256/manifests/...: UNAUTHORIZED: authentication required
```

### Causa:
O Cosign estava tentando assinar a imagem usando apenas o digest SHA256, sem o nome completo do registry e repositório.

### ✅ Solução Aplicada:

1. **Adicionado login no GHCR antes da assinatura**
2. **Corrigido formato da referência da imagem:**
   ```bash
   # Antes (incorreto)
   cosign sign --yes ${{ needs.build-image.outputs.image-digest }}
   
   # Depois (correto)
   IMAGE_WITH_DIGEST="${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}@${{ needs.build-image.outputs.image-digest }}"
   cosign sign --yes "$IMAGE_WITH_DIGEST"
   ```

## ⚠️ Warning do Google Guice com Java 24

### Warning:
```
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::staticFieldBase has been called by com.google.inject.internal.aop.HiddenClassDefiner
```

### Causa:
O Google Guice 5.1.0 (usado pelo Maven) ainda utiliza APIs deprecated do `sun.misc.Unsafe` que serão removidas em versões futuras do Java.

### 💡 Soluções Recomendadas:

1. **Aguardar atualização do Maven:**
   - Este é um problema conhecido do Maven com Java 24
   - Aguardar versão do Maven que use Guice atualizado

2. **Suprimir warnings (temporário):**
   ```dockerfile
   ENV JAVA_OPTS="$JAVA_OPTS -XX:+IgnoreUnrecognizedVMOptions --add-opens=java.base/sun.misc=ALL-UNNAMED"
   ```

3. **Usar versão anterior do Java (se necessário):**
   - Considerar Java 21 LTS se os warnings forem problemáticos
   - Java 24 ainda está em preview/early access

## 📊 Status das Correções

- ✅ **Build-args corrigidas** - Variáveis agora são definidas corretamente
- ✅ **Parâmetro deprecated removido** - Warning do Maven eliminado
- ⏳ **GHCR configuração** - Requer ação manual no GitHub
- ℹ️ **Warning Guice** - Problema conhecido, não crítico

## 🚀 Próximos Passos

1. **Configurar GHCR** seguindo as instruções acima
2. **Testar o workflow** após as correções
3. **Monitorar** se os warnings do Guice afetam a funcionalidade
4. **Considerar migração** para Java 21 LTS se necessário

---

**Nota:** As correções aplicadas resolvem os problemas técnicos identificados. O erro principal (GHCR) requer configuração manual no GitHub.