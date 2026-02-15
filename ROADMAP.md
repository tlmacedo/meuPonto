# ✅ CHECKLIST COMPLETO - MeuPonto v2.0

## 📅 Informações de Controle
- **Última atualização:** 18/02/2025
- **Versão Atual:** v2.0.0-alpha
- **Status Geral:** 🏗️ Infraestrutura e Core Business

## 📊 Resumo Executivo

| Fase | Descrição | Status | Progresso |
|------|-----------|--------|-----------|
| **Fase 1** | Infraestrutura (DB, Entidades, Audit Log) | ✅ Concluído | 100% |
| **Fase 2** | Core Business (Validações, Saldo Dinâmico) | ✅ Concluído | 100% |
| **Fase 3** | Múltiplos Empregos | ✅ Concluído | 100% |
| **Fase 4** | Configurações Completas | 🟨 Em Andamento | ~40% |
| **Fase 5** | Interface & UX | ⬜ Pendente | 0% |
| **Fase 6** | Notificações | ⬜ Pendente | 0% |
| **Fase 7** | Extras & Polish | ⬜ Pendente | 0% |

---

## 🔷 FASE 1 - Infraestrutura do Banco de Dados ✅ CONCLUÍDA

### 1.1 Novas Entidades

- [x] **`EmpregoEntity`** - Tabela de empregos
    - `id`, `nome`, `ativo`, `arquivado`, `criadoEm`, `atualizadoEm`
- [x] **`ConfiguracaoEmpregoEntity`** - Configurações por emprego
- [x] **`HorarioDiaSemanaEntity`** - Horários por dia da semana
- [x] **`AjusteSaldoEntity`** - Ajustes manuais de banco de horas
- [x] **`FechamentoPeriodoEntity`** - Registros de fechamento (zerar saldo)
- [x] **`MarcadorEntity`** - Tags/etiquetas
- [x] **`AuditLogEntity`** - Histórico de alterações

### 1.2 Alterações em Entidades Existentes

- [x] **`PontoEntity`** - Adicionar campos:
    - [x] `empregoId`, `nsr`, `latitude`, `longitude`, `endereco`, `marcadorId`, `justificativaInconsistencia`, `horaConsiderada`

### 1.3 Migrations

- [x] **Migration 1→2**: Criar todas as novas tabelas e colunas, vincular dados existentes ao emprego padrão.

### 1.4 DAOs Novos

- [x] `EmpregoDao`, `ConfiguracaoEmpregoDao`, `HorarioDiaSemanaDao`, `AjusteSaldoDao`, `FechamentoPeriodoDao`, `MarcadorDao`, `AuditLogDao`

### 1.5 Repositories

- [x] Todos os repositories implementados (Interfaces + Impls)

### 1.6 Audit Log Service

- [x] `AuditLogService` - Intercepta INSERT/UPDATE/DELETE e registra
- [ ] Job para limpeza de logs > 1 ano

---

## 🔷 FASE 2 - Core Business (Validações e Cálculos) ✅ CONCLUÍDA

### 2.1 Modelos de Domínio ✅

- [x] `Emprego`, `ConfiguracaoEmprego`, `HorarioDiaSemana`, `AjusteSaldo`, `Marcador`
- [x] `Inconsistencia` (enum), `ResultadoValidacao` (data class)

### 2.2 Use Cases de Validação ✅

- [x] **`ValidarSequenciaPontoUseCase`** - Sequência entrada/saída
- [x] **`ValidarHorarioPontoUseCase`** - Registro futuro/retroativo/curto
- [x] **`ValidarJornadaDiariaUseCase`** - Intrajornada, CLT, limites diários
- [x] **`ValidarPontoCompletoUseCase`** - Orquestrador de validações
- [x] **`ValidarInterjornadaUseCase`** - Mínimo 11h entre jornadas

### 2.3 Use Cases de Saldo (Dinâmico) ✅

- [x] **`CalcularSaldoPeriodoUseCase`**, **`CalcularSaldoDiaUseCase`**, **`CalcularSaldoSemanalUseCase`**, **`CalcularSaldoMensalUseCase`**
- [ ] **`CalcularIntervaloEfetivoUseCase`** - Tolerância de intervalo

### 2.4 Use Cases de Ajuste ✅

- [x] **`RegistrarAjusteSaldoUseCase`**, **`ExcluirAjusteUseCase`**, **`ListarAjustesUseCase`**
- [x] **`RegistrarFechamentoPeriodoUseCase`**, **`ExecutarFechamentoAutomaticoUseCase`**

---

## 🔷 FASE 3 - Múltiplos Empregos ✅ CONCLUÍDA

### 3.1 Use Cases ✅

- [x] `CriarEmpregoUseCase`, `AtualizarEmpregoUseCase`, `DesativarEmpregoUseCase`, `ArquivarEmpregoUseCase`, `ExcluirEmpregoUseCase`
- [x] `ListarEmpregosUseCase`, `ObterEmpregoAtivoUseCase`, `TrocarEmpregoAtivoUseCase`, `CopiarConfiguracaoEmpregoUseCase`

### 3.2 Preferences ✅

- [x] Salvar `empregoAtivoId` no DataStore
- [x] Carregar emprego ativo no startup
- [x] `PreferencesDataStore` implementado com suporte a tema e notificações

---

## 🔷 FASE 4 - Tela de Configurações 🟨 EM ANDAMENTO

### 4.1 Estrutura de Navegação

- [ ] Definir rotas e navegação

### 4.2 ViewModels

- [ ] `ConfiguracoesViewModel`, `HorariosTrabalhoViewModel`, `ConfiguracaoGeralViewModel`

### 4.3 Use Cases de Configuração (Concluídos) ✅

- [x] `ObterConfiguracaoJornadaUseCase`, `SalvarConfiguracaoJornadaUseCase`
- [x] `ValidarConfiguracaoEmpregoUseCase`

---

## 🔷 FASE 5 - Interface & UX ⬜ PENDENTE

### 5.1 Tela Principal (Dia)
- [ ] Header com troca de emprego (dropdown)
- [ ] Navegação por data (< data >)
- [ ] Resumo do dia (Trab. | Saldo dia | Saldo total)

### 5.2 Timeline de Registros
- [ ] Layout vertical com linha conectora
- [ ] Card de Ponto (ícones, horário, NSR, localização)
- [ ] Duração entre pontos (turno/intervalo)

### 5.3 Contador em Tempo Real
- [ ] Contador HH:mm:ss quando há entrada sem saída
- [ ] Alertas visuais ao atingir limites

### 5.4 Indicadores Visuais de Inconsistência
- [ ] Ícone de alerta, cores diferenciadas, tooltip

### 5.5 Registro de Ponto
- [ ] Botão FAB/Modal com picker, NSR, marcador, justificativa

### 5.6 Componentes Reutilizáveis

- [ ] `TimelineConnector`, `PontoTimelineCard`
- [ ] `DuracaoLabel`, `IntervaloLabel`
- [ ] `ContadorTempoReal`, `InconsistenciaBadge`

---

## 🔷 FASE 6 - Sistema de Notificações ⬜ PENDENTE

### 6.1 Infraestrutura
- [ ] `NotificationManager` wrapper, `AlarmManager`, `WorkManager`

### 6.2 Tipos de Notificação
- [ ] Hora de começar, intervalo, retornar, ir para casa

---

## 🔷 FASE 7 - Extras & Polish ⬜ PENDENTE

### 7.1 Geocodificação
- [ ] Captura de localização, geocodificação reversa

### 7.2 Histórico de Alterações (UI)
- [ ] `HistoricoAlteracoesScreen`, filtros, diff, reverter

### 7.3 Onboarding
- [ ] Boas-vindas, criar emprego, configurar horários

### 7.4 Exportação/Backup
- [x] `GerarRelatorioMensalUseCase`, `GerarRelatorioPeriodoUseCase` (Lógica implementada)
- [ ] Exportar CSV/JSON, backup local

---

## 📋 Ordem de Implementação Sugerida

| Prioridade | Item | Dependência | Status |
|------------|------|-------------|--------|
| 🔴 1 | Fase 1.1-1.5 (Entidades e Migrations) | - | ✅ Concluído |
| 🔴 2 | Fase 1.6 (Audit Log básico) | 1 | ✅ Concluído |
| 🔴 3 | Fase 2.1-2.2 (Modelos e Validações) | 1 | ✅ Concluído |
| 🔴 4 | Fase 2.3-2.4 (Saldo Dinâmico e Ajustes) | 3 | ✅ Concluído |
| 🟠 5 | Fase 3 (Múltiplos Empregos) | 1 | ✅ Concluído |
| 🟠 6 | Fase 5.1-5.4 (UI Principal) | 3, 4 | ⬜ Pendente |
| 🟠 7 | Fase 5.5-5.6 (Registro e Componentes) | 6 | ⬜ Pendente |
| 🟡 8 | Fase 4 (Configurações) | 5 | 🟨 Em Andamento |
| 🟡 9 | Fase 6 (Notificações) | 6 | ⬜ Pendente |
| 🔵 10 | Fase 7 (Polimento) | 8 | ⬜ Pendente |

---

## 📖 Legenda Status
- ⬜ Pendente
- 🟨 Em Andamento
- ✅ Concluído
- ❌ Erro / Bloqueado

## 🔗 Referências
- [Jetpack Compose Navigation](https://developer.android.com/jetpack/compose/navigation)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [Material 3 Design Guidelines](https://m3.material.io/)

## 🕒 Commits Realizados
- `feat: setup initial project structure with Compose, Hilt and Clean Architecture` (17/02/2025)
- `feat: expandir infraestrutura de dados e camada de validação` (18/02/2025)
- `feat: concluir infraestrutura de dados e sistema de validação` (18/02/2025)
- `feat: implementar gestão de múltiplos empregos e lógica de relatórios` (18/02/2025)
- `feat: implementar persistência com DataStore e atualizar roadmap` (18/02/2025)
