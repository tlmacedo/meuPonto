# 📱 MeuPonto

### Aplicativo Android para Controle de Banco de Horas

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Latest-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

**Controle seu banco de horas de forma simples e eficiente, sem depender do RH.**

[Funcionalidades](#-funcionalidades) •
[Tecnologias](#-tecnologias) •
[Instalação](#-instalação) •
[Arquitetura](#-arquitetura) •
[Contribuição](#-contribuição)

</div>

---

## 📋 Sobre o Projeto

O **MeuPonto** é um aplicativo Android desenvolvido para trabalhadores que desejam ter controle pessoal sobre seu banco de horas. Com ele, você pode registrar suas batidas de ponto diárias, acompanhar saldos de horas (positivas ou negativas), gerenciar faltas, feriados, férias e horas extras — tudo isso de forma independente e sem depender do RH da sua empresa.

### 🎯 Problema que Resolve

- ❌ Trabalhadores não têm acesso fácil ao seu saldo de banco de horas
- ❌ Dependência do RH para obter relatórios de ponto
- ❌ Dificuldade em acompanhar horas extras e compensações
- ❌ Falta de controle sobre a própria jornada de trabalho

### ✅ Solução

- ✔️ App pessoal para registro e acompanhamento de ponto
- ✔️ Cálculo automático de saldo de horas
- ✔️ Geração de relatórios para conferência
- ✔️ Sincronização em nuvem para backup e múltiplos dispositivos

---

## ✨ Funcionalidades

### 📍 Registro de Ponto
- [x] Bater ponto com um toque
- [x] Suporte a múltiplas batidas por dia (entrada, almoço, retorno, saída)
- [x] Edição e exclusão de registros
- [  ] Captura de foto do comprovante
- [  ] Registro de localização (GPS)
- [x] Observações em cada registro

### ⏰ Configuração de Jornada
- [x] Definição de horários de trabalho
- [x] Configuração de carga horária diária/semanal
- [x] Tolerância para batidas
- [x] Dias de trabalho personalizáveis
- [x] Intervalo de almoço configurável

### 📊 Lançamentos Especiais
- [x] Faltas (justificadas/injustificadas)
- [x] Feriados nacionais e locais
- [x] Férias
- [x] Horas extras pagas
- [x] Horas compensadas
- [x] Abonos e atestados médicos
- [x] Folgas programadas

### 💰 Saldo e Histórico
- [x] Saldo diário, semanal e mensal
- [x] Saldo total acumulado
- [x] Histórico navegável por período
- [x] Visualização em formato calendário
- [  ] Opção de reset mensal

### 📄 Relatórios e Exportação
- [  ] Extrato em PDF
- [  ] Exportação para Excel/CSV
- [  ] Compartilhamento via e-mail/WhatsApp
- [  ] Filtros por período

### ☁️ Sincronização
- [  ] Backup automático na nuvem (Firebase)
- [  ] Sincronização entre dispositivos
- [  ] Autenticação segura

### 🔔 Notificações
- [  ] Lembretes para bater ponto
- [  ] Alertas de saldo negativo
- [  ] Notificações personalizáveis

---

## 🛠️ Tecnologias

### Stack Principal

| Tecnologia | Versão | Descrição |
|------------|--------|-----------|
| **Kotlin** | 2.1.0 | Linguagem de programação |
| **Jetpack Compose** | BOM 2024.12.01 | UI declarativa moderna |
| **Material 3** | Latest | Design System |
| **Hilt** | 2.54 | Injeção de dependências |
| **Room** | 2.6.1 | Persistência local (SQLite) |
| **Coroutines** | 1.9.0 | Programação assíncrona |
| **Flow** | - | Streams reativos |
| **Navigation Compose** | 2.8.5 | Navegação entre telas |
| **DataStore** | 1.1.1 | Preferências do usuário |

### Backend & Cloud

| Tecnologia | Descrição |
|------------|-----------|
| **Firebase Auth** | Autenticação de usuários |
| **Firebase Firestore** | Banco de dados na nuvem |

---

## 🏗️ Arquitetura

O projeto segue os princípios da **Clean Architecture** combinada com o padrão **MVVM**.
```
══════════════════════════════════════════════════════════════════════════════════════════════════
ARQUITETURA E ESTRUTURA DO PROJETO
══════════════════════════════════════════════════════════════════════════════════════════════════
┌────────────────────────────────────────────────────────────────────────────────────────────────┐
│ DIAGRAMA DA ARQUITETURA                                                                        │
├────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                                │
│   ┌────────────────────────────────────────────────────────────────────────────────────────┐   │
│   │                              PRESENTATION LAYER                                        │   │
│   │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐    │   │
│   │  │     Screens     │  │   ViewModels    │  │    UI States    │  │   Components    │    │   │
│   │  │   (Composable)  │  │  (@HiltViewModel│  │  (data class)   │  │  (Composable)   │    │   │
│   │  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘  └─────────────────┘    │   │
│   │           │                    │                    │                                  │   │
│   │           └────────────────────┼────────────────────┘                                  │   │
│   │                                │                                                       │   │
│   │                          observa/coleta                                                │   │
│   │                                │                                                       │   │
│   └────────────────────────────────┼───────────────────────────────────────────────────────┘   │
│                                    │                                                           │
│                                    ▼                                                           │
│   ┌────────────────────────────────────────────────────────────────────────────────────────┐   │
│   │                                DOMAIN LAYER                                            │   │
│   │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────────────────┐     │   │
│   │  │    Use Cases    │  │     Models      │  │      Repository Interfaces          │     │   │
│   │  │  (classes com   │  │   (Entities/    │  │      (interface XxxRepository)      │     │   │
│   │  │  operator invoke│  │   data class)   │  │                                     │     │   │
│   │  └────────┬────────┘  └─────────────────┘  └──────────────────┬──────────────────┘     │   │
│   │           │                                                   │                        │   │
│   │           └───────────────────────────────────────────────────┘                        │   │
│   │                                    │                                                   │   │
│   │                             usa interface                                              │   │
│   │                                    │                                                   │   │
│   └────────────────────────────────────┼───────────────────────────────────────────────────┘   │
│                                        │                                                       │
│                                        ▼                                                       │
│   ┌────────────────────────────────────────────────────────────────────────────────────────┐   │
│   │                                 DATA LAYER                                             │   │
│   │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐    │   │
│   │  │   Repository    │  │   Local Source  │  │  Remote Source  │  │   DTOs/Mappers  │    │   │
│   │  │      Impl       │  │   (Room DAOs)   │  │   (Firebase)    │  │                 │    │   │
│   │  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘  └─────────────────┘    │   │
│   │           │                    │                    │                                  │   │
│   │           └────────────────────┼────────────────────┘                                  │   │
│   │                                │                                                       │   │
│   │                                ▼                                                       │   │
│   │                    ┌─────────────────────┐                                             │   │
│   │                    │  SQLite / Firebase  │                                             │   │
│   │                    └─────────────────────┘                                             │   │
│   └────────────────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                                │
└────────────────────────────────────────────────────────────────────────────────────────────────┘
```
---

### 📁 Estrutura de Pacotes
```
┌────────────────────────────────────────────────────────────────────────────────────────────────┐
│ ESTRUTURA COMPLETA DE DIRETÓRIOS                                                               │
├────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                                │
│  br.com.tlmacedo.meuponto/                                                                     │
│  │                                                                                             │
│  ├── MeuPontoApplication.kt              # Application class com Hilt                          │
│  │                                                                                             │
│  ├── di/                                 # INJEÇÃO DE DEPENDÊNCIA                              │
│  │   ├── AppModule.kt                    # Módulo geral do app                                 │
│  │   ├── DatabaseModule.kt               # Provê Room Database e DAOs                          │
│  │   ├── FirebaseModule.kt               # Provê Firebase Auth e Firestore                     │
│  │   └── RepositoryModule.kt             # Binds de Repository interfaces                      │
│  │                                                                                             │
│  ├── data/                               # CAMADA DE DADOS                                     │
│  │   ├── local/                          # Fontes de dados locais                              │
│  │   │   ├── database/                   # Room Database                                       │
│  │   │   │   ├── AppDatabase.kt          # Database principal                                  │
│  │   │   │   ├── dao/                    # Data Access Objects                                 │
│  │   │   │   │   ├── PontoDao.kt                                                               │
│  │   │   │   │   ├── ConfiguracaoDao.kt                                                        │
│  │   │   │   │   ├── LancamentoDao.kt                                                          │
│  │   │   │   │   └── FeriadoDao.kt                                                             │
│  │   │   │   ├── entity/                 # Entidades do Room                                   │
│  │   │   │   │   ├── PontoEntity.kt                                                            │
│  │   │   │   │   ├── ConfiguracaoEntity.kt                                                     │
│  │   │   │   │   ├── LancamentoEntity.kt                                                       │
│  │   │   │   │   └── FeriadoEntity.kt                                                          │
│  │   │   │   └── converter/              # Type Converters para Room                           │
│  │   │   │       └── DateConverter.kt                                                          │
│  │   │   └── preferences/                # DataStore Preferences                               │
│  │   │       └── UserPreferencesDataStore.kt                                                   │
│  │   │                                                                                         │
│  │   ├── remote/                         # Fontes de dados remotas                             │
│  │   │   ├── firebase/                   # Serviços Firebase                                   │
│  │   │   │   ├── FirebaseAuthService.kt                                                        │
│  │   │   │   └── FirestoreService.kt                                                           │
│  │   │   └── dto/                        # Data Transfer Objects                               │
│  │   │       ├── PontoDto.kt                                                                   │
│  │   │       ├── ConfiguracaoDto.kt                                                            │
│  │   │       └── UserDto.kt                                                                    │
│  │   │                                                                                         │
│  │   ├── repository/                     # Implementações dos Repositories                     │
│  │   │   ├── PontoRepositoryImpl.kt                                                            │
│  │   │   ├── ConfiguracaoRepositoryImpl.kt                                                     │
│  │   │   ├── LancamentoRepositoryImpl.kt                                                       │
│  │   │   ├── FeriadoRepositoryImpl.kt                                                          │
│  │   │   ├── AuthRepositoryImpl.kt                                                             │
│  │   │   └── SyncRepositoryImpl.kt                                                             │
│  │   │                                                                                         │
│  │   └── mapper/                         # Mappers Entity <-> Domain <-> DTO                   │
│  │       ├── PontoMapper.kt                                                                    │
│  │       ├── ConfiguracaoMapper.kt                                                             │
│  │       └── LancamentoMapper.kt                                                               │
│  │                                                                                             │
│  ├── domain/                             # CAMADA DE DOMÍNIO                                   │
│  │   ├── model/                          # Modelos de domínio                                  │
│  │   │   ├── Ponto.kt                                                                          │
│  │   │   ├── TipoPonto.kt                # enum class                                          │
│  │   │   ├── Configuracao.kt                                                                   │
│  │   │   ├── HorarioTrabalho.kt                                                                │
│  │   │   ├── Lancamento.kt                                                                     │
│  │   │   ├── TipoLancamento.kt           # enum class                                          │
│  │   │   ├── Feriado.kt                                                                        │
│  │   │   ├── Saldo.kt                                                                          │
│  │   │   ├── RegistroDia.kt                                                                    │
│  │   │   ├── StatusDia.kt                # enum class                                          │
│  │   │   ├── User.kt                                                                           │
│  │   │   └── DataResult.kt               # sealed class para resultados                        │
│  │   │                                                                                         │
│  │   ├── repository/                     # Interfaces dos Repositories                         │
│  │   │   ├── PontoRepository.kt                                                                │
│  │   │   ├── ConfiguracaoRepository.kt                                                         │
│  │   │   ├── LancamentoRepository.kt                                                           │
│  │   │   ├── FeriadoRepository.kt                                                              │
│  │   │   ├── AuthRepository.kt                                                                 │
│  │   │   └── SyncRepository.kt                                                                 │
│  │   │                                                                                         │
│  │   └── usecase/                        # Casos de uso                                        │
│  │       ├── ponto/                                                                            │
│  │       │   ├── RegistrarPontoUseCase.kt                                                      │
│  │       │   ├── ObterPontosDoDiaUseCase.kt                                                    │
│  │       │   ├── ObterPontosPorPeriodoUseCase.kt                                               │
│  │       │   ├── EditarPontoUseCase.kt                                                         │
│  │       │   └── ExcluirPontoUseCase.kt                                                        │
│  │       ├── saldo/                                                                            │
│  │       │   ├── CalcularSaldoDiarioUseCase.kt                                                 │
│  │       │   ├── CalcularSaldoSemanalUseCase.kt                                                │
│  │       │   ├── CalcularSaldoMensalUseCase.kt                                                 │
│  │       │   ├── CalcularSaldoTotalUseCase.kt                                                  │
│  │       │   └── ObterHistoricoSaldoUseCase.kt                                                 │
│  │       ├── lancamento/                                                                       │
│  │       │   ├── RegistrarLancamentoUseCase.kt                                                 │
│  │       │   ├── ObterLancamentosUseCase.kt                                                    │
│  │       │   ├── EditarLancamentoUseCase.kt                                                    │
│  │       │   └── ExcluirLancamentoUseCase.kt                                                   │
│  │       ├── configuracao/                                                                     │
│  │       │   ├── ObterConfiguracaoUseCase.kt                                                   │
│  │       │   ├── SalvarConfiguracaoUseCase.kt                                                  │
│  │       │   └── ObterHorariosTrabalhoUseCase.kt                                               │
│  │       ├── feriado/                                                                          │
│  │       │   ├── ObterFeriadosUseCase.kt                                                       │
│  │       │   ├── AdicionarFeriadoUseCase.kt                                                    │
│  │       │   └── RemoverFeriadoUseCase.kt                                                      │
│  │       ├── relatorio/                                                                        │
│  │       │   ├── GerarRelatorioPdfUseCase.kt                                                   │
│  │       │   └── GerarRelatorioExcelUseCase.kt                                                 │
│  │       ├── auth/                                                                             │
│  │       │   ├── LoginEmailUseCase.kt                                                          │
│  │       │   ├── LoginGoogleUseCase.kt                                                         │
│  │       │   ├── CadastrarUsuarioUseCase.kt                                                    │
│  │       │   ├── LogoutUseCase.kt                                                              │
│  │       │   ├── ObterUsuarioAtualUseCase.kt                                                   │
│  │       │   └── RecuperarSenhaUseCase.kt                                                      │
│  │       └── sync/                                                                             │
│  │           ├── SincronizarDadosUseCase.kt                                                    │
│  │           └── RestaurarBackupUseCase.kt                                                     │
│  │                                                                                             │
│  ├── presentation/                       # CAMADA DE APRESENTAÇÃO                              │
│  │   ├── navigation/                     # Navegação                                           │
│  │   │   ├── AppNavigation.kt            # NavHost principal                                   │
│  │   │   ├── NavRoutes.kt                # Sealed class com rotas                              │
│  │   │   └── BottomNavBar.kt             # Barra de navegação inferior                         │
│  │   │                                                                                         │
│  │   ├── theme/                          # Design System                                       │
│  │   │   ├── Color.kt                                                                          │
│  │   │   ├── Type.kt                                                                           │
│  │   │   ├── Shape.kt                                                                          │
│  │   │   └── Theme.kt                                                                          │
│  │   │                                                                                         │
│  │   ├── components/                     # Componentes reutilizáveis                           │
│  │   │   ├── common/                     # Componentes genéricos                               │
│  │   │   │   ├── LoadingIndicator.kt                                                           │
│  │   │   │   ├── ErrorMessage.kt                                                               │
│  │   │   │   ├── ConfirmDialog.kt                                                              │
│  │   │   │   ├── DatePickerDialog.kt                                                           │
│  │   │   │   ├── TimePickerDialog.kt                                                           │
│  │   │   │   ├── EmptyStateView.kt                                                             │
│  │   │   │   └── TopAppBarCustom.kt                                                            │
│  │   │   ├── ponto/                      # Componentes de ponto                                │
│  │   │   │   ├── PontoCard.kt                                                                  │
│  │   │   │   ├── BaterPontoButton.kt                                                           │
│  │   │   │   ├── SaldoIndicator.kt                                                             │
│  │   │   │   ├── SaldoCard.kt                                                                  │
│  │   │   │   └── RegistroDiaItem.kt                                                            │
│  │   │   ├── lancamento/                 # Componentes de lançamento                           │
│  │   │   │   ├── LancamentoCard.kt                                                             │
│  │   │   │   └── TipoLancamentoSelector.kt                                                     │
│  │   │   └── charts/                     # Gráficos                                            │
│  │   │       ├── SaldoLineChart.kt                                                             │
│  │   │       └── HorasBarChart.kt                                                              │
│  │   │                                                                                         │
│  │   └── screens/                        # Telas do app                                        │
│  │       ├── splash/                                                                           │
│  │       │   └── SplashScreen.kt                                                               │
│  │       ├── auth/                                                                             │
│  │       │   ├── LoginScreen.kt                                                                │
│  │       │   ├── CadastroScreen.kt                                                             │
│  │       │   ├── RecuperarSenhaScreen.kt                                                       │
│  │       │   ├── AuthViewModel.kt                                                              │
│  │       │   ├── AuthUiState.kt                                                                │
│  │       │   └── AuthUiEvent.kt                                                                │
│  │       ├── home/                                                                             │
│  │       │   ├── HomeScreen.kt                                                                 │
│  │       │   ├── HomeViewModel.kt                                                              │
│  │       │   ├── HomeUiState.kt                                                                │
│  │       │   └── HomeAction.kt                                                                 │
│  │       ├── historico/                                                                        │
│  │       │   ├── HistoricoScreen.kt                                                            │
│  │       │   ├── HistoricoViewModel.kt                                                         │
│  │       │   ├── HistoricoUiState.kt                                                           │
│  │       │   ├── DetalhesDiaScreen.kt                                                          │
│  │       │   └── DetalhesDiaViewModel.kt                                                       │
│  │       ├── lancamentos/                                                                      │
│  │       │   ├── LancamentosScreen.kt                                                          │
│  │       │   ├── LancamentosViewModel.kt                                                       │
│  │       │   ├── NovoLancamentoScreen.kt                                                       │
│  │       │   └── NovoLancamentoViewModel.kt                                                    │
│  │       ├── relatorios/                                                                       │
│  │       │   ├── RelatoriosScreen.kt                                                           │
│  │       │   └── RelatoriosViewModel.kt                                                        │
│  │       ├── graficos/                                                                         │
│  │       │   ├── GraficosScreen.kt                                                             │
│  │       │   └── GraficosViewModel.kt                                                          │
│  │       └── configuracoes/                                                                    │
│  │           ├── ConfiguracoesScreen.kt                                                        │
│  │           ├── ConfiguracoesViewModel.kt                                                     │
│  │           ├── HorariosTrabalhoScreen.kt                                                     │
│  │           ├── HorariosTrabalhoViewModel.kt                                                  │
│  │           ├── NotificacoesScreen.kt                                                         │
│  │           ├── NotificacoesViewModel.kt                                                      │
│  │           ├── BackupScreen.kt                                                               │
│  │           └── BackupViewModel.kt                                                            │
│  │                                                                                             │
│  ├── util/                               # UTILITÁRIOS                                         │
│  │   ├── Constants.kt                    # Constantes do app                                   │
│  │   ├── DateTimeUtils.kt                # Funções de data/hora                                │
│  │   ├── FormatUtils.kt                  # Formatação de valores                               │
│  │   ├── Extensions.kt                   # Extension functions                                 │
│  │   └── ValidationUtils.kt              # Validações                                          │
│  │                                                                                             │
│  ├── worker/                             # WORKERS (Background)                                │
│  │   ├── SyncWorker.kt                   # Sincronização em background                         │
│  │   └── NotificationWorker.kt           # Agendamento de notificações                         │
│  │                                                                                             │
│  ├── receiver/                           # BROADCAST RECEIVERS                                 │
│  │   ├── BootReceiver.kt                 # Reagendar notificações após boot                    │
│  │   └── NotificationReceiver.kt         # Ações das notificações                              │
│  │                                                                                             │
│  └── widget/                             # WIDGETS                                             │
│      ├── SaldoWidget.kt                                                                        │
│      ├── SaldoWidgetReceiver.kt                                                                │
│      ├── BatidaRapidaWidget.kt                                                                 │
│      └── BatidaRapidaWidgetReceiver.kt                                                         │
│                                                                                                │
└────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 🚀 Instalação

### Pré-requisitos

- **Android Studio** Ladybug (2024.2.1) ou superior
- **JDK 17** ou superior
- **Android SDK** com API 26+ (Android 8.0)
- **Git**

### Passos

1. **Clone o repositório**
   ```bash
   git clone https://github.com/tlmacedo/meuponto.git
   cd meuponto
   ```
2. Abra no Android Studio
   - File > Open > selecione a pasta do projeto
3. Sincronize o Gradle
   - O Android Studio deve sincronizar automaticamente
4. Configure o Firebase (opcional)
   - Crie um projeto no Firebase Console
   - Baixe o arquivo google-services.json
   - Coloque em app/google-services.json
   - Execute o app
     ```bash
     ./gradlew installDebug
     ```

## 📖 Uso
### Batendo Ponto

1. Abra o app
2. Na tela inicial, toque no botão "Bater Ponto"
3. O tipo de batida é detectado automaticamente:
    - 1ª batida → Entrada
    - 2ª batida → Saída Almoço
    - 3ª batida → Retorno Almoço
    - 4ª batida → Saída

### Visualizando Saldo
- O saldo do dia aparece na tela inicial
- Acesse Histórico para ver saldos anteriores
- Formato: +02:30 (crédito) ou -01:15 (débito)

## 📝 Roadmap
- Fase 1 - Setup do projeto e arquitetura base
- Fase 2 - Registro de ponto funcional
- Fase 3 - Configuração de jornada
- Fase 4 - Histórico e cálculo de saldo
- Fase 5 - Lançamentos especiais
- Fase 6 - Relatórios e exportação
- Fase 7 - Sincronização Firebase
- Fase 8 - Notificações

---

### 🤝 Contribuição
Contribuições são bem-vindas! Siga os passos:

1. Fork o projeto
2. Crie uma branch (git checkout -b feature/minha-feature)
3. Commit suas mudanças (git commit -m "feat(escopo): descrição")
4. Push para a branch (git push origin feature/minha-feature)
5. Crie um pull request (https://github.com/tlmacedo/meuponto/pulls)

### Padrão de Commits


| Tipo     | Descrição                  |
|----------|----------------------------|
| feat     | Nova funcionalidade        |
| fix      | Correção de bug            |
| refactor | Refatoração de código      |
| docs     | Alterações na documentação |
| style    | Formatação                 |
| test     | Testes                     |
| chore    | Tarefas de build/configs   |


---

## 📄 Licença
Este projeto está sob a licença MIT. Veja o arquivo LICENSE [blocked] para mais detalhes.

---

## 👤 Autor
### Thiago Macedo

- GitHub: @tlmacedo

---

### ⭐ Se este projeto te ajudou, considere dar uma estrela!

Made with ❤️ in Manaus, Brasil 🇧🇷