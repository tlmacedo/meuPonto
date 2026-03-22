Plano de Desenvolvimento — MeuPonto

Versão: 1.0.0 | Repositório: https://github.com/tlmacedo/meuPonto | Atualizado em: Março de 2026

Sumário


Problemas Encontrados
Melhorias Recomendadas
Convenções Obrigatórias
Plano de Conclusão por Fases
Checklist de Pull Request
Arquitetura de Referência


1. Problemas Encontrados

1.1 Uso de e.printStackTrace() em produção

Onde: ComprovanteImageStorage, ImageOrientationCorrector, ExifDataWriter, ImageHashCalculator, ImageCompressor, ImageProcessor, PhotoCaptureManager, ImageResizer, FotoStorageManager.Problema: e.printStackTrace() imprime no stderr sem nenhuma estrutura, não é capturado por ferramentas de monitoramento como Firebase Crashlytics e não permite rastreamento em produção.Solução: Substituir por Timber.e(e, "descrição do contexto") em todos os blocos catch. ImageTrashManager já usa Timber corretamente e deve ser o padrão.

1.2 Parâmetro titulo x title em PlaceholderScreen

Onde: MeuPontoNavHost.kt define uma PlaceholderScreen privada com parâmetro titulo, enquanto PlaceholderScreen.kt define a pública com parâmetro title.Problema: Há duas implementações do mesmo componente no projeto. A versão privada dentro do NavHost é redundante e pode causar confusão. Chamadas dentro do arquivo usam a versão local sem aproveitar a pública.Solução: Remover a PlaceholderScreen privada de MeuPontoNavHost.kt e padronizar o uso da pública definida em PlaceholderScreen.kt.

1.3 FotoStorageManager injeta FotoComprovanteDao mas não o usa

Onde: FotoStorageManager.kt declara FotoComprovanteDao como dependência injetada via construtor, mas nenhum método da classe faz uso desse DAO.Problema: Dependência desnecessária aumenta acoplamento e confunde sobre as responsabilidades da classe.Solução: Remover FotoComprovanteDao do construtor de FotoStorageManager. A persistência de metadados no banco deve ser responsabilidade de um repositório ou use case, não do storage manager.

1.4 AusenciaEntity contém campo @Deprecated como parte da estrutura principal

Onde: AusenciaEntity.kt, campo subTipoFolga.Problema: Campos deprecated em entidades Room geram ruído e podem causar confusão em futuras migrações. O campo continua sendo populado com null explicitamente nas funções de mapeamento.Solução: Manter o campo apenas até confirmar que todos os usuários migraram para versão superior à 15. Após isso, remover via migração de banco com recriação da tabela.

1.5 MIGRATION_25_26 usa 'CREDITO' como default inválido para TipoAjusteSaldo

Onde: Migration_24_25.kt define o default de tipo como 'CREDITO', mas Migration_25_26.kt usa o mesmo valor como fallback.Problema: O enum TipoAjusteSaldo no domínio usa MANUAL como valor padrão (conforme AjusteSaldoEntity), não CREDITO. Há divergência entre o que o banco espera e o que o domínio define.Solução: Unificar o valor padrão. Se MANUAL é o correto pelo domínio, ambas as migrações devem usar MANUAL. Se CREDITO for um valor válido do enum, garantir que ele exista no enum e seja documentado.

1.6 columnExists duplicada nas migrações

Onde: Migration_3_4.kt define columnExists como método privado local. Migration_2_3.kt define columnExists como função top-level no mesmo pacote. Outras migrações (ex: Migration_5_6.kt, Migration_6_7.kt) chamam a versão top-level.Problema: Definição duplicada com implementações ligeiramente diferentes pode causar comportamento inconsistente e dificulta manutenção.Solução: Manter apenas a versão top-level em Migration_2_3.kt (que já é acessível no pacote) e remover a privada de Migration_3_4.kt.

1.7 Route.kt e MeuPontoDestinations.kt coexistem com responsabilidades sobrepostas

Onde: Route.kt (sealed class) e MeuPontoDestinations.kt (object com constantes) definem rotas de navegação para as mesmas telas.Problema: BottomNavItem referencia Route, enquanto o NavHost usa MeuPontoDestinations. Duas fontes de verdade para rotas de navegação.Solução: Migrar BottomNavItem para usar MeuPontoDestinations e remover Route.kt, ou o inverso. Escolher uma única fonte de verdade e deprecar a outra formalmente antes de remover.

1.8 Logs de debug com android.util.Log em código de produção

Onde: ComprovanteImagePicker.kt usa android.util.Log.d(...) em múltiplos pontos.Problema: Logs com android.util.Log não são controlados por build type e podem expor informações sensíveis em produção.Solução: Substituir por Timber.d(...). Os logs existentes são úteis para debug e devem ser mantidos via Timber, que respeita configuração por build type.

1.9 PreferencesDataStore e PreferenciasGlobaisDataStore com escopos sobrepostos

Onde: Ambas as classes gerenciam preferências de tema, notificações e configurações gerais em arquivos DataStore diferentes.Problema: PreferencesDataStore gerencia ConfiguracaoJornada, tema e notificações. PreferenciasGlobaisDataStore gerencia as mesmas preocupações (tema, notificações, backup) com mais detalhes. Há risco de inconsistência se os dois forem consultados ao mesmo tempo.Solução: Consolidar as preferências em PreferenciasGlobaisDataStore como única fonte de verdade. Migrar qualquer uso de PreferencesDataStore para o consolidado e marcar o antigo como deprecated para remoção futura.

1.10 saveFromUri e saveFromBitmap em ComprovanteImageStorage não usam Dispatchers

Onde: ComprovanteImageStorage.kt, métodos saveFromUri, saveFromBitmap, saveBitmap, loadBitmap, loadThumbnail, delete, etc.Problema: Operações de I/O (leitura e escrita de arquivos, compressão de bitmap) são realizadas de forma síncrona sem garantia de execução em uma thread de I/O. Se chamados da thread principal causarão NetworkOnMainThreadException ou ANR.Solução: Adicionar suspend nos métodos públicos e executar com withContext(Dispatchers.IO), ou garantir via documentação que o chamador é responsável pelo dispatcher. A abordagem FotoStorageManager já faz isso corretamente via withContext(Dispatchers.IO).

2. Melhorias Recomendadas

2.1 Extrair formatação de tamanho de arquivo para extensão reutilizável

Onde: AdaptiveCompressionResult, SavePhotoResult, StorageStats, TrashItem, TrashStats, FotoStorageManager, ComprovanteImageStorage.Situação: A lógica de formatação de bytes (B, KB, MB, GB) está duplicada em pelo menos 6 lugares.Ação: Criar extensão Long.formatarTamanho(): String em util/FileExtensions.kt e substituir todas as ocorrências.

2.2 ImageTrashManager não tem integração com o banco de dados

Onde: ImageTrashManager.kt.Situação: O gerenciador controla a lixeira de imagens no sistema de arquivos, mas não registra as exclusões na tabela audit_logs nem consulta a tabela pontos para validar se o arquivo ainda é referenciado.Ação: Injetar AuditLogDao e registrar AcaoAuditoria ao mover para lixeira e ao restaurar. Também injetar PontoDao para validar se o ponto correspondente foi restaurado via soft delete antes de permitir restauração da imagem.

2.3 FotoViewModel não trata o estado de carregamento na exclusão

Onde: FotoViewModel.kt, função deletePhoto.Situação: O isLoading é setado para true e depois para false após o resultado, mas o DeleteConfirmationDialog não observa esse estado — apenas chama onConfirm() e onDismiss() sequencialmente sem feedback visual de processamento.Ação: Adicionar observação do isLoading no diálogo de confirmação de exclusão, ou exibir um indicador de progresso enquanto a operação ocorre.

2.4 strings.xml com acentuação incorreta e ausência de plurais

Onde: strings.xml.Situação: Strings como Inicio, Historico, Configuracoes, Relatorios, Saida Almoco, Retorno Almoco, ponto excluido, ponto atualizado estão sem acentuação. Além disso, mensagens de contagem (ex: "X arquivo(s) removido(s)") devem usar recursos plurals.Ação: Corrigir todos os textos sem acento. Criar entradas <plurals> para mensagens de contagem variável.

2.5 FotoFullScreenViewer não reseta zoom ao fechar e reabrir

Onde: FotoFullScreenViewer.kt.Situação: O estado de scale, offsetX e offsetY é preservado em remember, mas como o composable é chamado via composição condicional, ao fechar e reabrir a foto o zoom pode persistir incorretamente dependendo do ciclo de vida do composable pai.Ação: Usar key(fotoPath) para forçar recomposição quando o caminho da foto muda, garantindo que o estado de zoom seja resetado a cada nova abertura.

2.6 Ausência de tratamento para SecurityException na captura de localização

Onde: LocationService.kt.Situação: A SecurityException é capturada via resumeWithException, mas o chamador (suspendCancellableCoroutine) não tem nenhum try-catch no nível de uso. O contrato não está claro: o chamador deve tratar ou o service deve absorver?Ação: Definir um contrato claro. A sugestão é que LocationService retorne null em caso de SecurityException (sem relançar), já que permissão negada é um estado esperado, não um erro fatal.

2.7 calculateInSampleSize em ImageResizer pode retornar valor subótimo

Onde: ImageResizer.kt, função calculateInSampleSize.Situação: O algoritmo calcula inSampleSize garantindo que a imagem resultante seja pelo menos tão grande quanto o requerido. No entanto, para imagens muito maiores que o alvo, pode ser mais eficiente usar um inSampleSize maior e depois escalar, em vez de carregar uma imagem ainda grande para depois escalar.Ação: Considerar adicionar um parâmetro opcional allowSmaller: Boolean = false que permita calcular o sample size sem a restrição de mínimo, útil para thumbnails onde o tamanho exato não é crítico.

2.8 cleanupOrphanImages em ComprovanteImageStorage usa lógica de caminho frágil

Onde: ComprovanteImageStorage.kt, função cleanupOrphanImages.Situação: O caminho relativo é construído removendo o prefixo do diretório raiz por string manipulation (removePrefix). Se houver diferença de trailing slash ou separador de plataforma, a comparação com validPaths falhará silenciosamente, deixando arquivos órfãos no disco.Ação: Usar File.relativeTo(base) do Kotlin para calcular caminhos relativos de forma robusta, eliminando dependência de manipulação manual de strings.

3. Convenções Obrigatórias

Estas convenções devem ser aplicadas em todo código novo e nas correções. Nenhum PR deve ser mergeado se violar estas regras.3.1 Logging


Usar exclusivamente Timber para todos os logs.
Nunca usar android.util.Log, println ou System.out.
Em BuildConfig.DEBUG, inicializar Timber.plant(Timber.DebugTree()) na Application.
Em produção, usar uma árvore de Timber configurada para enviar ao Crashlytics.
3.2 Tratamento de Erros


Nunca usar e.printStackTrace().
Preferir Timber.e(e, "contexto do erro").
Operações assíncronas devem retornar Result<T> (sealed class do domínio), nunca lançar exceção para a UI.
3.3 Threads e Coroutines


Toda operação de I/O (banco, arquivo, rede) deve rodar em Dispatchers.IO.
ViewModels usam viewModelScope. Repositórios e Use Cases usam withContext(Dispatchers.IO).
Nunca bloquear a main thread com operações síncronas de I/O.
3.4 Navegação


Usar exclusivamente MeuPontoDestinations como fonte de verdade para rotas.
Remover Route.kt após migração completa de BottomNavItem.
Nunca hardcodar strings de rota fora de MeuPontoDestinations.
3.5 Strings


Toda string exibível na UI deve estar em strings.xml com acentuação correta.
Usar recursos plurals para mensagens com contagem variável.
Nunca hardcodar texto de UI no código Kotlin ou Compose.
3.6 Componentes Compose


Cada arquivo deve conter apenas um Composable público principal.
Composables privados auxiliares devem estar no mesmo arquivo do principal.
Não duplicar implementações (ex: PlaceholderScreen em dois lugares).


4. Plano de Conclusão por Fases

FASE 1 — Correções Críticas e Padronização

Objetivo: Estabilizar o código existente antes de qualquer nova funcionalidade.Tarefas:
Substituir todos os e.printStackTrace() por Timber.e() em: ComprovanteImageStorage, ImageOrientationCorrector, ExifDataWriter, ImageHashCalculator, ImageCompressor, ImageProcessor, PhotoCaptureManager, ImageResizer, FotoStorageManager.
Substituir android.util.Log por Timber em ComprovanteImagePicker.
Remover PlaceholderScreen privada de MeuPontoNavHost.kt e unificar com a pública.
Remover FotoComprovanteDao do construtor de FotoStorageManager.
Corrigir divergência de valor default (MANUAL vs CREDITO) nas migrações 24–25 e 25–26.
Remover columnExists duplicada de Migration_3_4.kt.
Corrigir acentuação em strings.xml e adicionar entradas faltantes.
Extrair Long.formatarTamanho() para util/FileExtensions.kt e substituir duplicatas.
Adicionar suspend + withContext(Dispatchers.IO) nos métodos públicos de ComprovanteImageStorage que realizam I/O.
Corrigir cleanupOrphanImages para usar File.relativeTo().
Critério de conclusão: Nenhum e.printStackTrace(), nenhum android.util.Log fora de testes, zero duplicatas de componentes de navegação e UI.

FASE 2 — Consolidação de Arquitetura

Objetivo: Eliminar sobreposições e definir fronteiras claras entre camadas.Tarefas:
Migrar BottomNavItem para usar MeuPontoDestinations e remover Route.kt.
Consolidar PreferencesDataStore em PreferenciasGlobaisDataStore como única fonte de verdade. Criar migration de DataStore se necessário.
Integrar ImageTrashManager com AuditLogDao para registrar movimentações de lixeira.
Implementar verificação cruzada entre lixeira de imagens e soft delete de pontos (isDeleted).
Criar contrato claro de tratamento de SecurityException em LocationService.
Adicionar key(fotoPath) em FotoFullScreenViewer para reset de estado de zoom.
Implementar feedback de loading no DeleteConfirmationDialog do FotoViewModel.
Critério de conclusão: Uma única fonte de verdade para rotas, uma única fonte de verdade para preferências, lixeira integrada ao audit log.

FASE 3 — Funcionalidades Placeholder

Objetivo: Implementar as telas que atualmente exibem "Em Desenvolvimento".As seguintes rotas estão registradas no NavHost mas redirecionam para PlaceholderScreen:
Aparência (/aparencia): Implementar seleção de tema (claro, escuro, sistema), usando PreferenciasGlobaisDataStore. Integrar com TemaEscuro enum já existente.
Notificações (/notificacoes): Implementar configuração de lembretes de ponto, alertas de feriado e alertas de banco de horas. Campos já existem em PreferenciasGlobais.
Privacidade & Segurança (/privacidade): Implementar configuração de autenticação biométrica, política de retenção de dados e opção de exclusão de conta.
Backup & Dados (/backup): Implementar exportação do banco Room para JSON/CSV, importação e backup automático. Infraestrutura de fotoBackupNuvemHabilitado já existe em ConfiguracaoEmprego.
Marcadores (/marcadores): Implementar CRUD de marcadores. DAO e Entity já estão completos (MarcadorDao, MarcadorEntity).
Ajustes de Saldo (/emprego/{id}/ajustes): Implementar tela de ajustes manuais no banco de horas. DAO e Entity já estão completos (AjusteSaldoDao, AjusteSaldoEntity).
Horários por Dia (/horarios_trabalho): Implementar configuração de horários por dia da semana. DAO e Entity já estão completos (HorarioDiaSemanaDao, HorarioDiaSemanaEntity).
Configuração de Jornada (/configuracao_jornada): Implementar tela legacy ou redirecionar para fluxo por emprego.
Critério de conclusão: Nenhuma tela exibe PlaceholderScreen para funcionalidades que têm infraestrutura de dados completa.

FASE 4 — Sincronização e Backup

Objetivo: Implementar a infraestrutura de sincronização com nuvem, cujos campos já existem no banco mas não têm implementação.Tarefas:
Implementar WorkManager worker para backup automático de fotos (campo fotoBackupNuvemHabilitado já existe em ConfiguracaoEmpregoEntity).
Implementar sincronização de FotoComprovanteEntity com serviço de nuvem (campo sincronizadoNuvem, sincronizadoEm, cloudFileId já existem).
Implementar restrição de Wi-Fi para backup (fotoBackupApenasWifi já existe).
Implementar exportação do banco de dados como backup local (CSV ou JSON).
Implementar importação de backup para restauração de dados.
Critério de conclusão: Fotos são sincronizadas em background respeitando configurações de rede. Backup local pode ser exportado e reimportado.

FASE 5 — Testes

Objetivo: Garantir cobertura de testes para as camadas críticas.Tarefas:
Testes unitários para Use Cases do domínio: Especialmente os de cálculo de saldo, banco de horas e jornada.
Testes unitários para ImageProcessor: Cobrir pipeline de processamento com bitmaps mockados.
Testes unitários para ImageTrashManager: Cobrir movimentação, restauração e limpeza automática.
Testes unitários para ImageHashCalculator: Cobrir cálculo de MD5 e verificação de integridade.
Testes de migração de banco: Para cada Migration_X_Y, verificar que os dados são preservados corretamente usando MigrationTestHelper.
Testes de integração para Repositórios: Usar banco em memória (Room.inMemoryDatabaseBuilder) para testar DAOs críticos como PontoDao e FotoComprovanteDao.
Critério de conclusão: Cobertura mínima de 70% nas camadas domain e data. Todas as migrações com teste automatizado.

FASE 6 — Polimento e Lançamento

Objetivo: Preparar o app para publicação na Play Store.Tarefas:
Criar ícone de lançamento definitivo (substituir o placeholder ic_launcher_foreground.xml atual).
Configurar ProGuard/R8 com regras de ofuscação adequadas para Room, Retrofit e Hilt.
Implementar tela de onboarding para primeiro acesso (isPrimeiroAcesso já existe em PreferencesDataStore).
Revisar permissões no AndroidManifest e garantir que apenas as necessárias estão declaradas.
Configurar Firebase Crashlytics com a árvore de Timber para produção.
Configurar flavors de build: debug (com Timber DebugTree, dados de seed) e release (com Crashlytics, sem dados de seed).
Definir versão de produção, versionCode e versionName no build.gradle.
Criar CHANGELOG.md e documentar todas as versões do banco (1 a 26) para referência futura.
Critério de conclusão: App compilado em release, sem warnings críticos de ProGuard, instalável e funcional em dispositivo físico.

5. Checklist de Pull Request

Antes de qualquer merge para a branch principal, verificar:
Nenhum e.printStackTrace() introduzido
Nenhum android.util.Log introduzido fora de testes
Toda string exibível na UI está em strings.xml com acentuação correta
Nenhuma string de rota hardcoded fora de MeuPontoDestinations
StateFlow público não expõe MutableStateFlow
Operações de I/O executam em withContext(Dispatchers.IO) ou thread equivalente
Novos Use Cases cobertos por testes unitários
Nenhuma lógica de negócio em módulos Hilt
Resultado de operações assíncronas usa sealed class Result<T> do domínio
Nenhuma duplicata de componente Compose introduzida
Formatação de tamanho de arquivo usa Long.formatarTamanho() centralizado
Nenhum dado de seed ou desenvolvimento fora de BuildConfig.DEBUG
KDoc presente em todas as classes e funções públicas novas


6. Arquitetura de Referência

6.1 Estrutura de Pacotes

app/
├── core/               # Utilitários agnósticos de framework (LocationUtils, etc.)
├── data/
│   ├── local/
│   │   ├── database/
│   │   │   ├── dao/
│   │   │   ├── entity/
│   │   │   └── migration/
│   │   └── datastore/  # Única fonte: PreferenciasGlobaisDataStore
│   ├── remote/
│   │   ├── api/
│   │   └── dto/
│   ├── repository/     # Implementações de interfaces do domínio
│   └── service/        # LocationService, etc.
├── di/                 # Módulos Hilt (somente bindings)
├── domain/
│   ├── model/
│   ├── repository/     # Interfaces (contratos)
│   └── usecase/
├── presentation/
│   ├── components/     # Composables reutilizáveis
│   ├── navigation/     # NavHost, MeuPontoDestinations (única fonte de rotas)
│   ├── screen/         # Telas organizadas por feature
│   ├── theme/
│   └── viewmodel/
├── util/               # Extensões e helpers (MinutosExtensions, DateTimeExtensions, FileExtensions)
└── worker/             # Workers do WorkManager (a implementar na Fase 4)
6.2 Fluxo de Dados

UI (Screen/Composable)
↕ observa StateFlow / envia eventos
ViewModel
↕ chama suspend fun
Use Case (Domain)
↕ usa interface do repositório
Repository Interface (Domain)
↑ implementado por
Repository Impl (Data)
↕ acessa
DAO (Room) / API (Retrofit) / DataStore
6.3 Regras de Dependência


Presentation pode depender de Domain. Nunca de Data diretamente.
Domain não depende de nenhuma outra camada. É Kotlin puro.
Data depende de Domain (implementa interfaces). Nunca de Presentation.
DI conhece todas as camadas (é o "glue"), mas não contém lógica de negócio.
Core e Util podem ser acessados por qualquer camada.


Documento gerado com base na análise completa do código-fonte do projeto MeuPonto.
Revisitar e atualizar ao final de cada fase concluída.