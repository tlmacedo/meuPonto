# 🚀 EVOLUÇÃO DO PROJETO - MeuPonto

Este documento registra o histórico de marcos, decisões técnicas e a evolução contínua do aplicativo MeuPonto.

---

## 📌 Marco Atual: Refatoração de Segurança e Dados (v2.6.0)
**Data:** 18/02/2026

Nesta fase, o foco saiu da interface para a robustez do sistema, tratando débitos técnicos acumulados e garantindo a privacidade dos dados do usuário.

### ✅ Conquistas Recentes
- **Saneamento de Logs:** Remoção de logs sensíveis que expunham IDs e cálculos em produção. Substituição planejada para Timber.
- **Segurança de Rede:** Configuração de logging restrito apenas para builds de debug e planejamento de SSL Pinning.
- **Exportação de Código:** Criação de scripts para backup e auditoria do código-fonte organizados por camadas (Clean Architecture).
- **Roadmap 2.0:** Consolidação de todas as pendências em um plano de ação prioritário.

---

## 📈 Histórico de Versões

### [v2.5.0] - 17/02/2026
- **Funcionalidade:** Implementação de tolerâncias flexíveis (globais e por dia da semana).
- **UI:** Finalização da `EditPontoScreen` com lógica de tipos de ponto automáticos.
- **Arquitetura:** Melhoria na reatividade da `HomeScreen` com o `HomeViewModel`.

### [v2.0.0] - Janeiro/2026
- **Funcionalidade:** Suporte a Múltiplos Empregos (Multi-job).
- **Infraestrutura:** Migração para Hilt (Injeção de Dependências) e implementação do Audit Log.
- **UX:** Redesign completo para Material 3.

---

## 🛠️ Decisões Técnicas Estratégicas

1.  **Consolidação do Banco de Dados (V17 -> V1):**
    - *Motivo:* O excesso de migrações durante o desenvolvimento estava aumentando a complexidade e o risco de bugs na inicialização.
    - *Solução:* Exportar dados reais para JSON, resetar o esquema para V1 e reimportar.
2.  **Clean Architecture:**
    - Manter a separação estrita entre `domain` (regras), `data` (persistência) e `presentation` (UI) para facilitar testes unitários.
3.  **Segurança First:**
    - Decisão de não armazenar geolocalização em texto plano para conformidade com LGPD/Privacidade.

---

## 💡 Próximos Passos (Curto Prazo)
- [ ] Executar o backup JSON e consolidar o banco de dados.
- [ ] Implementar o cálculo de saldo acumulado na `HistoryScreen`.
- [ ] Finalizar a criptografia de campos sensíveis no SQLite.
- [ ] Iniciar o sistema de notificações para lembretes de batida de ponto.

---

## 📝 Lições Aprendidas
- **Logs:** Nunca usar `Log.d` diretamente para dados do domínio; o custo de limpeza posterior é alto.
- **Migrations:** Em fase de desenvolvimento ativo, é melhor consolidar o banco periodicamente do que acumular dezenas de migrações pequenas.
