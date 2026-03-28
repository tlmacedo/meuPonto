# Cronograma Detalhado do Projeto "MeuPonto"

## 1. Visão Geral do Projeto

**Nome do Projeto:** MeuPonto (Nome de código: MeuPonto)
**Objetivo Principal:** Desenvolver um aplicativo Android nativo, "Meu Ponto CLT", utilizando Kotlin e Jetpack Compose, que permita aos usuários registrar e gerenciar seus horários de trabalho de forma precisa, flexível e segura, com suporte a múltiplos empregos, jornadas versionadas e funcionalidades avançadas como captura de localização e foto de comprovante.
**Tecnologias Chave:** Kotlin, Jetpack Compose, Hilt, Jetpack Compose Navigation, Kotlin Coroutines e Flow, Room Database, WorkManager, Retrofit, OkHttp, Timber, AndroidX Test.
**Raiz do Projeto:** `br.com.tlmacedo.meuponto`
**Base de Código Inicial:** Os arquivos `PROJETO_*.txt` fornecidos serão utilizados como base para a estrutura inicial, módulos de injeção de dependência, utilitários e configurações de banco de dados.

## 2. Fases de Desenvolvimento

O projeto será dividido em fases iterativas, com marcos e entregáveis claros.

### Fase 0: Preparação e Configuração Inicial (1-2 dias)

*   **Descrição:** Criação do projeto Android Studio, configuração do sistema de build (Gradle com Kotlin DSL e Version Catalogs), importação da base de código fornecida e validação do ambiente de desenvolvimento.
*   **Marcos:**
    *   Projeto Android Studio criado e configurado.
    *   `libs.versions.toml` populado com dependências iniciais.
    *   Estrutura de pacotes `br.com.tlmacedo.meuponto` replicada.
    *   `AndroidManifest.xml` e arquivos de recursos (`strings.xml`, `themes.xml`, `file_paths.xml`, `data_extraction_rules.xml`, `backup_rules.xml`, `ic_launcher.xml`, `ic_launcher_round.xml`) copiados e configurados.
    *   Aplicação compila e executa com `MainActivity` vazia.
*   **Entregáveis:**
    *   Repositório Git inicializado com a estrutura base.
    *   Projeto Android Studio configurado e funcional.
    *   Documentação inicial de setup do ambiente.
*   **Perguntas a serem respondidas antes de iniciar:**
    1.  Confirma que a raiz do projeto será `br.com.tlmacedo.meuponto`?
    2.  Deseja que o nome do aplicativo visível ao usuário seja "Meu Ponto CLT" (conforme `strings.xml` nos arquivos fornecidos)?
    3.  Os arquivos `PROJETO_MANIFEST.txt`, `PROJETO_RESOURCES.txt` e `PROJETO_OUTROS.txt` (para `MeuPontoApplication.kt` e `ExampleInstrumentedTest.kt`) devem ser copiados diretamente para o projeto, ou há alguma revisão/adaptação específica que gostaria de fazer antes?
    4.  As dependências implícitas nos arquivos `PROJETO_DI.txt` e `PROJETO_DATA.txt` (Room, Hilt, Coroutines, Flow, Timber, Retrofit, OkHttp, etc.) devem ser adicionadas ao `libs.versions.toml` e ao `build.gradle.kts` do módulo `app` desde o início? (Sugestão: Sim, para que o código base compile).

### Fase 1: Módulo de Autenticação (10-15 dias)

*   **Descrição:** Implementação completa das funcionalidades de cadastro, login (com "Lembrar-me" e biometria) e recuperação de senha.
*   **Marcos:**
    *   Telas de Cadastro, Login e Recuperação de Senha implementadas com Jetpack Compose.
    *   Validações de campos funcionando.
    *   Integração com `PreferencesDataStore` para "Lembrar-me".
    *   Integração com `BiometricPrompt` para autenticação biométrica.
    *   Fluxo de navegação entre as telas de autenticação.
    *   Testes unitários para `AuthRepository` e `AuthUseCases`.
*   **Entregáveis:**
    *   Módulo de Autenticação funcional e testado.
    *   UI responsiva para diferentes tamanhos de tela.
    *   Documentação de APIs e Use Cases de autenticação.
*   **Potenciais Riscos:**
    *   Complexidade na integração de biometria em diferentes versões do Android.
    *   Gerenciamento seguro de tokens de autenticação.
*   **Estratégias de Mitigação:**
    *   Utilizar a biblioteca `androidx.biometric` para compatibilidade.
    *   Armazenar tokens em `EncryptedSharedPreferences` ou `Security-Sensitive DataStore`.

### Fase 2: Módulo de Configurações - Gerenciamento de Empregos e Jornadas (15-20 dias)

*   **Descrição:** Desenvolvimento das telas e lógica para cadastro e gerenciamento de empregos, versões de jornada, regras de jornada e horários por dia da semana.
*   **Marcos:**
    *   Telas de listagem e detalhe de Empregos.
    *   Telas de listagem e detalhe de Versões de Jornada.
    *   Telas de configuração de Regras da Jornada e Horários por Dia da Semana.
    *   Persistência de dados no Room Database (`EmpregoDao`, `VersaoJornadaDao`, `HorarioDiaSemanaDao`, etc.).
    *   Validações de sobreposição de jornadas e consistência de horários.
    *   Testes unitários para Use Cases e Repositórios relacionados.
*   **Entregáveis:**
    *   Módulo de Configurações funcional.
    *   UI intuitiva para entrada de dados complexos.
    *   Modelos de dados (entidades Room) estáveis.
*   **Potenciais Riscos:**
    *   Complexidade das regras de validação de jornada.
    *   Gerenciamento de estados de UI para formulários aninhados.
*   **Estratégias de Mitigação:**
    *   Definir Use Cases claros para cada validação.
    *   Utilizar `ViewModel`s com `StateFlow` para gerenciar o estado dos formulários.

### Fase 3: Módulo de Controle de Horários - Registro de Ponto (10-15 dias)

*   **Descrição:** Implementação da funcionalidade principal de registro de ponto, incluindo captura de localização, NSR e foto de comprovante.
*   **Marcos:**
    *   Tela principal com botão de registro de ponto.
    *   Lógica de sugestão de tipo de registro (Entrada, Saída Intervalo, etc.).
    *   Integração com `LocationService` para captura de localização.
    *   Integração com `ComprovanteImageStorage` e `ImageOrientationCorrector` para foto de comprovante.
    *   Campos condicionais para NSR e Justificativa.
    *   Persistência de registros de ponto (`PontoDao`, `FotoComprovanteDao`).
*   **Entregáveis:**
    *   Módulo de Registro de Ponto funcional.
    *   Integração com APIs de câmera e localização.
    *   Testes de integração para o fluxo de registro.
*   **Potenciais Riscos:**
    *   Gerenciamento de permissões de localização e câmera.
    *   Processamento de imagem (redimensionamento, correção de orientação) em background.
    *   Precisão da localização em diferentes cenários.
*   **Estratégias de Mitigação:**
    *   Utilizar APIs de permissão do AndroidX com explicações claras.
    *   Executar operações de imagem em `Dispatchers.IO` e WorkManager.
    *   Implementar lógica de fallback para localização (ex: usar última localização conhecida).

### Fase 4: Visualização e Relatórios de Horários (10-15 dias)

*   **Descrição:** Desenvolvimento das telas para visualização dos horários registrados, saldos de banco de horas e relatórios básicos.
*   **Marcos:**
    *   Tela de histórico de pontos (lista por dia/mês).
    *   Tela de detalhes do dia com todos os registros.
    *   Cálculo de saldo de horas (diário, semanal, mensal).
    *   Exibição de informações de localização em mapa (se aplicável).
    *   Filtros e ordenação para o histórico.
*   **Entregáveis:**
    *   Módulo de Visualização de Horários funcional.
    *   Componentes de UI para exibição de dados tabulares e gráficos (se houver).
*   **Potenciais Riscos:**
    *   Complexidade dos cálculos de saldo de horas (considerando regras de jornada, banco de horas, etc.).
    *   Performance na exibição de grandes volumes de dados.
*   **Estratégias de Mitigação:**
    *   Definir Use Cases específicos para cada tipo de cálculo.
    *   Otimizar queries do Room e usar `Flow` para atualizações reativas.
    *   Implementar paginação ou carregamento sob demanda para listas longas.

### Fase 5: Refinamentos, Testes e Otimizações (7-10 dias)

*   **Descrição:** Fase dedicada a testes abrangentes (unitários, de integração, UI), otimização de performance, correção de bugs e melhorias de usabilidade.
*   **Marcos:**
    *   Cobertura de testes unitários e de integração adequada.
    *   Testes de UI (Espresso/Compose Test) para fluxos críticos.
    *   Identificação e correção de bugs.
    *   Otimização de performance (UI, banco de dados, rede).
    *   Revisão de código e refatoração.
    *   Testes em diferentes dispositivos e versões do Android.
*   **Entregáveis:**
    *   Aplicativo estável e performático.
    *   Relatório de bugs corrigidos.
    *   Relatório de cobertura de testes.
*   **Potenciais Riscos:**
    *   Descoberta de bugs críticos no final do ciclo.
    *   Problemas de performance em dispositivos mais antigos.
*   **Estratégias de Mitigação:**
    *   Testes contínuos ao longo das fases.
    *   Utilizar ferramentas de profiling (Android Profiler).
    *   Testar em emuladores e dispositivos reais variados.

### Fase 6: Preparação para Lançamento e Publicação (3-5 dias)

*   **Descrição:** Geração de builds de release, configuração de monitoramento (Crashlytics, Analytics), otimização de recursos, criação de assets da loja e publicação na Google Play Store.
*   **Marcos:**
    *   Build de release assinado e otimizado.
    *   Configuração de Firebase Crashlytics e Analytics.
    *   Criação de ícones, screenshots e descrição para a Play Store.
    *   Publicação na Google Play Store (primeiro em canal de testes, depois produção).
*   **Entregáveis:**
    *   Aplicativo publicado na Google Play Store.
    *   Dashboards de monitoramento configurados.
    *   Documentação de lançamento.
*   **Potenciais Riscos:**
    *   Rejeição do aplicativo pela Google Play Store.
    *   Problemas pós-lançamento (crashes, bugs não detectados).
*   **Estratégias de Mitigação:**
    *   Revisar diretrizes da Play Store.
    *   Lançamento gradual (rollout) para monitorar feedback inicial.
    *   Ter um plano de resposta rápida para bugs críticos.

## 3. Equipe e Recursos

*   **Desenvolvedor Android Sênior (Kotlin, Jetpack Compose):** Responsável pela arquitetura, desenvolvimento principal e revisão de código.
*   **Designer UX/UI (Opcional, mas recomendado):** Para garantir uma experiência de usuário de alta qualidade.
*   **QA (Quality Assurance):** Para planejar e executar testes, identificar bugs e garantir a qualidade do software.
*   **Ferramentas:** Android Studio, Git, Jira/Trello (para gerenciamento de tarefas), Firebase (Crashlytics, Analytics), Google Play Console.

## 4. Critérios de Sucesso e Métricas de Desempenho

*   **Funcionalidade Completa:** Todas as funcionalidades da proposta detalhada implementadas e funcionando conforme especificado.
*   **Estabilidade:** Taxa de crashes abaixo de 0.1% (monitorado via Firebase Crashlytics).
*   **Performance:** Tempos de carregamento de tela rápidos (abaixo de 500ms), fluidez da UI (60 FPS), consumo otimizado de bateria.
*   **Usabilidade:** Avaliações positivas de usuários na Play Store (média acima de 4.5 estrelas).
*   **Segurança:** Nenhuma vulnerabilidade crítica identificada em testes de segurança.
*   **Manutenibilidade:** Código bem estruturado, testável e documentado, facilitando futuras atualizações.

---