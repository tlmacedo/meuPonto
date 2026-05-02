# 📌 MeuPonto - Master Plan & Documentação Consolidada

> **Versão:** 2.0.0
> **Status:** Documento Mestre de Referência
> **Última Atualização:** Maio de 2024 (Consolidação Geral)

---

## 📖 1. Visão Geral do Projeto
O **MeuPonto** é um aplicativo Android nativo para gestão de jornada de trabalho (CLT), focado em precisão, segurança e automação. Utiliza as tecnologias mais modernas do ecossistema Android para oferecer uma experiência premium ao trabalhador.

### 🛠️ Stack Tecnológica
- **Linguagem:** Kotlin
- **UI:** Jetpack Compose (Material Design 3 & Dynamic Colors)
- **Arquitetura:** Clean Architecture + MVVM
- **Injeção de Dependência:** Hilt
- **Banco de Dados:** Room (com suporte a Migrations)
- **Background:** WorkManager
- **Navegação:** Jetpack Navigation (Compose)
- **IA/OCR:** Google ML Kit (Document Scanner & Text Recognition)
- **Monitoramento:** Firebase Crashlytics & Analytics

---

## 🏛️ 2. Arquitetura e Convenções

### 2.1 Estrutura de Camadas
- `app/core/`: Utilitários agnósticos de framework.
- `app/data/`: Implementações de persistência (Room, DataStore) e Repositórios.
- `app/domain/`: Regras de negócio puras (Models, Interfaces de Repositório, Use Cases).
- `app/presentation/`: UI (Screens, ViewModels, Components, Navigation).
- `app/util/`: Extensões e helpers específicos do projeto.
- `app/worker/`: Tarefas de plano de fundo.

### 2.2 Regras de Ouro (MANDATÓRIO)
1.  **Logging:** Usar exclusivamente `Timber`. **PROIBIDO** `e.printStackTrace()` ou `Log.d`.
2.  **Threads:** Operações de I/O devem usar `withContext(Dispatchers.IO)`. Repositórios e Use Cases devem garantir o dispatcher correto.
3.  **Strings:** 100% das strings na UI devem estar em `strings.xml` com acentuação correta. Usar `<plurals>` para contagens.
4.  **Resultados:** Operações assíncronas devem retornar a sealed class `Result<T>`.
5.  **Imutabilidade:** Preferir `val`. `StateFlow` nunca deve expor `MutableStateFlow` publicamente.
6.  **KDoc:** Documentação obrigatória em classes e funções públicas (`@param`, `@return`, `@author`, `@since`).
7.  **Arquivo:** A primeira linha de todo arquivo `.kt` deve ser `// Arquivo: {path_do_projeto}`.

---

## 🗺️ 3. Roadmap de Desenvolvimento (Fases)

### Fase 1: Estabilização e Higienização (CONCLUÍDA ✅)
- [x] Correção de `BuildConfig.DEBUG` para logs de rede e dados de seed.
- [x] Substituição massiva de `e.printStackTrace()` por `Timber.e()`.
- [x] Padronização de `LocationUtils` e `MinutosExtensions`.
- [x] Correção de acentuação no `strings.xml`.
- [x] Implementação de `Result<T>` como padrão de retorno.

### Fase 2: Inteligência e Automação (CONCLUÍDA ✅)
- [x] **OCR Avançado:** Binarização de imagem e extração contextual de horários.
- [x] **Geofencing:** Registro automático de ponto baseado em localização.
- [x] **Wear OS:** Módulo de relógio, Tiles e sincronização DataLayer.
- [x] **Widgets:** Implementação via Jetpack Glance com dados em tempo real.

### Fase 3: Experiência do Usuário (EM ANDAMENTO 🚧)
- [x] **Onboarding:** Fluxo de 7 telas para configuração inicial (DataStore + Room).
- [x] **Histórico:** Filtros avançados, ordenação e visualização de comprovantes.
- [x] **Ausências:** Gestão de férias, atestados e folgas com cálculo de saldo.
- [ ] **Acessibilidade:** Auditoria final TalkBack em todas as telas.

### Fase 4: Gestão de Ativos e Backup (CONCLUÍDA ✅)
- [x] **Lixeira:** `ImageTrashManager` com suporte a restauração e limpeza automática.
- [x] **Cloud Backup:** Integração Firebase Firestore para sincronização de dados e fotos.
- [x] **Galeria:** Visualizador de comprovantes com suporte a zoom e metadados.

### Fase 5: Relatórios e Finalização (PENDENTE ⏳)
- [ ] **Exportação PDF:** Geração de extrato mensal formatado (Native PDF API).
- [ ] **Exportação CSV:** Exportação para Excel com BOM UTF-8.
- [ ] **Compartilhamento:** Integração com Intent de compartilhamento para relatórios.
- [ ] **Baseline Profiles:** Otimização de performance de inicialização.

---

## 🚀 4. Novas Fronteiras: IA e Insights
A próxima grande evolução do MeuPonto foca em transformar o app de um registrador passivo em um consultor ativo:
1.  **Analista de Tendências:** Previsão de saldo mensal baseado em hábitos.
2.  **Detector de Esquecimento:** Notificação inteligente baseada em localização vs. horário habitual.
3.  **Chatbot CLT:** Consultor integrado para dúvidas sobre o contrato de trabalho configurado.

---

## 📦 5. Inventário de Documentos Auxiliares
*Estes arquivos podem ser encontrados na pasta `./docs/` e contêm detalhes técnicos específicos.*

- `Guia_Widget_MeuPonto.md`: Manual de instalação e uso dos widgets.
- `CronogramaDetalhadoProjeto.md`: Histórico de marcos e entregáveis por fase.
- `Relatorio_Otimizacao_V1.md`: (Anteriormente Relatorio de Otimizacao...) Análise competitiva inicial.

---
*Documento gerado automaticamente para consolidar o conhecimento do projeto.*
