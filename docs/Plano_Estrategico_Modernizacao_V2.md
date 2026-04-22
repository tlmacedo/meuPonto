# 🚀 Plano Estratégico: Modernização, IA e Robustez (Fase 2)

Este documento detalha o planejamento para elevar o nível técnico e a experiência do usuário do aplicativo **MeuPonto**, integrando tecnologias de ponta, incluindo Inteligência Artificial.

---

## 📅 Fase 6: Praticidade e Acesso Rápido (Em Andamento 🚧)
*Foco: Reduzir o tempo que o usuário gasta para realizar ações críticas.*

### 6.1. Widgets de Tela Inicial (Jetpack Glance)
- [x] **Infraestrutura:** Adicionadas dependências Glance e Serialization.
- [x] **Implementação:** Criado `MeuPontoWidget` com suporte a Material 3 e cores dinâmicas.
- [x] **Dados Reais:** Integrado via `WidgetUpdateWorker` para exibir saldo do dia e próximo ponto em tempo real.

### 6.2. Atalhos de Aplicativo (App Shortcuts)
- [x] **Atalhos Estáticos:** Criados atalhos para "Registrar Agora" e "Histórico" via `shortcuts.xml`.

### 6.3. Versão para Smartwatch (Wear OS)
- [ ] Criar módulo `:wear` para relógios.
- [ ] Sincronização de dados via DataLayer entre celular e relógio.
- [ ] Interface simplificada para batida de ponto rápida.

---

## 🎨 Fase 7: Design Premium e Imersão
*Foco: Polimento visual e transições de alto nível.*

### 7.1. Animações de Elementos Compartilhados (Shared Elements)
- [ ] Transição suave da miniatura do comprovante para a tela de visualização cheia.
- [ ] Transição do card de resumo para a tela de detalhes do dia.

### 7.2. Otimização para Telas Grandes (Tablets e Dobráveis)
- [ ] Implementar **Canônico Layout (List-Detail)**: Lista à esquerda e detalhes à direita em telas > 600dp.
- [ ] Adaptação do `RegistrarPontoModal` para não ocupar a tela inteira em tablets.

---

## 🛡️ Fase 8: Segurança e Integridade de Dados
*Foco: Proteção total das informações do trabalhador.*

### 8.1. Bloqueio por Biometria (App Lock)
- [ ] Opção nas configurações para exigir Digital/Face ID ao abrir o app.
- [ ] Proteção específica para ações sensíveis (excluir registros ou limpar lixeira).

### 8.2. Registro via NFC (Opcional)
- [ ] Permitir que o usuário "encoste" o celular em uma tag NFC configurada para bater o ponto automaticamente.

---

## ⚙️ Fase 9: Robustez Técnica e Performance
*Foco: Garantir que o app nunca falhe e seja extremamente rápido.*

### 9.1. Suíte de Testes de Unidade (Banco de Horas)
- [ ] Criar testes para cobrir 100% dos cenários de cálculo (horas noturnas, feriados, DSR, tolerâncias).
- [ ] Testar persistência e migração de banco de dados.

### 9.2. Baseline Profiles
- [ ] Gerar perfis de compilação para eliminar "engasgos" e reduzir o tempo de abertura em até 30%.

---

## 🧠 Anexo: Integração de Inteligência Artificial (IA)
*Análise de viabilidade e casos de uso da IA no MeuPonto.*

### Como seria a implementação?
Utilizaremos o **Google Gemini Nano** (processamento local no celular para privacidade) ou a **API do Gemini** (via Vertex AI) para análises mais complexas.

### O que a IA faria?
1. **Analista de Tendências:** A IA analisaria o histórico e diria: *"Você costuma fazer 15min de hora extra nas terças-feiras. Se continuar assim, fechará o mês com 4h de saldo positivo."*
2. **Detector de Anomalias:** Identificar se o usuário esqueceu de bater o ponto baseado na localização e horários habituais.
3. **Assistente de Voz Natural:** *"Ei MeuPonto, bati a saída agora, anota aí"*.
4. **Chatbot de Dúvidas Trabalhistas:** Um chat integrado que responde dúvidas sobre a CLT baseando-se no contrato configurado no app (ex: *"Quanto eu recebo se trabalhar no feriado?"*).

### Por que seria útil?
- Transforma um app de "registro passivo" em um **consultor ativo**.
- Aumenta a retenção do usuário, que passa a abrir o app para receber insights e não apenas "bater o cartão".

### Como implementar (Passo a passo):
1. **Configuração:** Adicionar o SDK `GoogleAI` ao `build.gradle`.
2. **Contexto:** Enviar o resumo de horas (sem dados sensíveis) como contexto para o modelo.
3. **Interface:** Criar uma aba "Insights" com um resumo gerado pela IA.

---
*Este plano serve como guia para a evolução contínua do MeuPonto nos próximos meses.*
