# ✅ ROADMAP - MeuPonto v2.6.0

## 📅 Informações de Controle
- **Última atualização:** 18/02/2026
- **Versão Atual:** v2.6.0
- **Status Geral:** 🏗️ Refatoração de Segurança e Consolidação de Banco

## 📊 Resumo Executivo

| Fase | Descrição | Status | Progresso |
|------|-----------|--------|-----------|
| **Fase 0** | **Segurança & Estabilidade (Crítico)** | 🟨 Em Andamento | ~50% |
| **Fase 1** | Infraestrutura (DB v1.0 Consolidação) | 🟨 Em Andamento | ~80% |
| **Fase 2** | Core Business (Regras de Negócio) | ✅ Concluído | 100% |
| **Fase 3** | Múltiplos Empregos | ✅ Concluído | 100% |
| **Fase 4** | Configurações Completas | ✅ Concluído | 100% |
| **Fase 5** | Interface & UX (Home/Edição) | ✅ Concluído | 100% |
| **Fase 6** | Notificações & Alertas | ⬜ Pendente | 0% |
| **Fase 7** | Histórico & Relatórios | 🟨 Em Andamento | ~40% |
| **Fase 8** | Planejamento Avançado & OCR | 🟨 Em Andamento | ~30% |

---

## 🔴 FASE 0 - Segurança & Estabilidade (CRÍTICO) 🟨

### 0.1 Saneamento de Logs 🟨
- [x] Remover logs sensíveis no `HomeViewModel`.
- [ ] Substituir `android.util.Log` por `Timber` estruturado em todo o projeto.
- [ ] Configurar árvore de logs para release (sem logs em produção).

### 0.2 Segurança de Rede 🟨
- [x] Logging condicional no `NetworkModule` (Apenas em DEBUG).
- [ ] Implementar SSL Pinning para BrasilAPI.
- [ ] Adicionar Network Security Config no Manifest.

### 0.3 Proteção de Dados Local ⬜
- [ ] Implementar criptografia de localização (Lat/Long) no banco de dados.
- [ ] Proteger preferências sensíveis no DataStore.

---

## 🔷 FASE 1 - Infraestrutura & Consolidação do Banco 🟨

### 1.1 Consolidação de Migrações (V17 -> V1) 🟨
- [ ] Criar UseCases de Exportação/Importação JSON para dados reais.
- [ ] Resetar banco de dados para a Versão 1 (Limpeza de débitos técnicos).
- [ ] Validar integridade dos dados após restauração.

### 1.2 Limpeza de Código Morto ⬜
- [ ] Remover métodos deprecados no `PontoRepository`.
- [ ] Padronizar Mappers de entidades para domínio.

---

## 🔷 FASE 7 - Histórico & Relatórios 🟨

### 7.1 Histórico de Registros 🟨
- [x] UI base da `HistoryScreen`.
- [ ] Implementar cálculo de saldo acumulado na visualização.
- [ ] Adicionar filtros por tipo de registro e período.

### 7.2 Exportação de Dados 🟨
- [x] Exportação de código organizada por camadas (Script).
- [ ] Exportação de relatórios em CSV/PDF para o usuário.

---

## 🔷 FASE 8 - Planejamento & OCR 🟨

### 8.1 OCR & Inteligência ⬜
- [ ] Captura de foto do comprovante com `FileProvider`.
- [ ] Integração com ML Kit para reconhecimento automático de data/hora no ticket.

---

## 💡 Sugestões de Implementações Futuras

1.  **Widget de Tela Inicial:** Botão de "Ponto Rápido" e contador de jornada atual sem abrir o app.
2.  **Modo Quiosque / NFC:** Registro de ponto por aproximação em tags NFC configuradas no local de trabalho.
3.  **Análise de Saúde Ocupacional:** Gráficos de descanso semanal e alertas de excesso de jornada.
4.  **Integração com Calendário:** Sincronia de folgas e feriados com o Google Calendar.
5.  **Relatório PDF Assinável:** Geração de espelho de ponto pronto para assinatura digital.

---

## 📖 Legenda Status
- ⬜ Pendente
- 🟨 Em Andamento
- ✅ Concluído
- ❌ Erro / Bloqueado
