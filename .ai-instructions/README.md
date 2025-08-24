# 🤖 Sistema Centralizado de Diretrizes para Modelos de IA

## 📋 VISÃO GERAL

Este diretório contém as diretrizes padronizadas para **TODOS** os modelos de IA que trabalham neste projeto. Cada modelo deve seguir **RIGOROSAMENTE** estas instruções.

## 🎯 ESTRUTURA DE ARQUIVOS

```
.ai-instructions/
├── README.md                    # Este arquivo
├── core-guidelines.md           # Diretrizes centrais (fonte única da verdade)
├── update-instructions.md       # Como atualizar diretrizes em todos os modelos
└── model-specific/              # Instruções específicas por modelo
    ├── copilot.md
    ├── cursor.md
    ├── windsurf.md
    ├── tabnine.md
    ├── continue.md
    ├── qodo.md
    ├── claude.md
    ├── gemini.md
    ├── augment.md
    └── codeium.md
```

## 🔄 SINCRONIZAÇÃO AUTOMÁTICA

### Quando Adicionar Novas Diretrizes:

1. **Atualize PRIMEIRO**: `core-guidelines.md`
2. **Execute o script**: `update-all-models.sh` (quando criado)
3. **Verifique**: Todos os arquivos específicos foram atualizados

### Modelos Suportados:

- ✅ **GitHub Copilot** → `.github/copilot-instructions.md`
- ✅ **Cursor AI** → `.cursor/instructions.md`
- ✅ **Windsurf** → `.windsurf/instructions.md`
- ✅ **TabNine** → `.tabnine/instructions.md`
- ✅ **Continue** → `.continue/instructions.md`
- ✅ **Qodo Gen** → `.qodo/instructions.md`
- ✅ **Claude** → `.claude/instructions.md`
- ✅ **Gemini** → `.gemini/instructions.md`
- ✅ **Augment** → `.augment/instructions.md`
- ✅ **Codeium** → `.codeium/instructions.md`

## ⚠️ REGRAS IMPORTANTES

> **🚨 FONTE ÚNICA DA VERDADE**: `AI_DEVELOPMENT_GUIDELINES.md` é o documento mestre

> **🔄 SINCRONIZAÇÃO**: Toda mudança deve ser replicada em TODOS os modelos

> **📝 PADRÃO**: Todos os arquivos seguem o mesmo formato e conteúdo

> **🎯 CONSISTÊNCIA**: Garantir que todos os modelos sigam as mesmas diretrizes

---

## 🛠️ COMO USAR

### Para Desenvolvedores:
1. Leia `AI_DEVELOPMENT_GUIDELINES.md` para diretrizes completas
2. Cada modelo de IA lerá automaticamente suas instruções específicas
3. Todas as diretrizes são **OBRIGATÓRIAS** e **NÃO NEGOCIÁVEIS**

### Para Modelos de IA:
1. Leia SEMPRE seu arquivo de instruções específico
2. Consulte `AI_DEVELOPMENT_GUIDELINES.md` para detalhes completos
3. **NUNCA** desvie das diretrizes estabelecidas

---

**Versão**: 1.0  
**Criado em**: 2024-08-24  
**Última Atualização**: 2024-08-24
