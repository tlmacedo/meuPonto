Documento gerado em 09/03/2026 às 22:33 (horário de Manaus)




---

# 📄 DOCUMENTO 2

**Nome do arquivo sugerido:** `ANALISE_PROJETO_PLANO_ACAO.md`

```markdown
# 📋 Análise do Projeto e Plano de Ação - Meu Ponto

**Projeto:** Meu Ponto - Controle e Registro de Pontos  
**Autor:** Thiago Macedo  
**Data:** 09/03/2026  
**Versão:** 1.0

---

## 🏗️ Visão Geral do Projeto

### Descrição

Aplicativo Android nativo em Kotlin para **controle e registro de ponto**, desenvolvido com arquitetura moderna e boas práticas de desenvolvimento mobile.

### Objetivo

Permitir que usuários registrem seus horários de entrada e saída do trabalho, calculem banco de horas, gerenciem múltiplos empregos e mantenham histórico completo de jornadas.

---

## 🛠️ Stack Tecnológica

| Camada | Tecnologia | Versão (estimada) |
|--------|------------|-------------------|
| **Linguagem** | Kotlin | 1.9+ |
| **UI Framework** | Jetpack Compose | 1.5+ |
| **Design System** | Material 3 (Material You) | 1.2+ |
| **Arquitetura** | Clean Architecture | - |
| **Padrão de UI** | MVVM + UiState + Actions | - |
| **Injeção de Dependência** | Hilt (Dagger) | 2.48+ |
| **Banco de Dados** | Room | 2.6+ |
| **Carregamento de Imagens** | Coil | 2.5+ |
| **Navegação** | Compose Navigation | 2.7+ |
| **Persistência de Preferências** | DataStore | 1.0+ |
| **Assincronismo** | Kotlin Coroutines + Flow | 1.7+ |

---

## 📂 Estrutura do Projeto
br.com.tlmacedo.meuponto/ │ ├── 📁 core/ │ └── util/ # Utilitários compartilhados │ ├── 📁 data/ # Camada de Dados │ ├── local/ │ │ ├── database/ │ │ │ ├── converter/ # TypeConverters (LocalDateTime, etc.) │ │ │ ├── dao/ # Data Access Objects │ │ │ ├── entity/ # Entidades Room (14 arquivos) │ │ │ └── migration/ # Migrações de schema │ │ ├── datastore/ # DataStore preferences │ │ └── preferences/ # Gerenciamento de preferências │ ├── mapper/ # Mappers entity ↔ domain │ ├── remote/ │ │ ├── api/ # Interfaces de API │ │ └── dto/ # Data Transfer Objects │ ├── repository/ # Implementações de Repository │ └── service/ # Serviços de dados │ ├── 📁 di/ # Módulos Hilt │ ├── 📁 domain/ # Camada de Domínio │ ├── model/ # Modelos de domínio │ │ ├── ausencia/ # Ausência, TipoAusencia, TipoFolga │ │ └── feriado/ # Feriado, ResultadoVerificacaoDia │ ├── repository/ # Interfaces de Repository │ ├── service/ # Interfaces de serviços de domínio │ └── usecase/ # Casos de uso │ ├── ajuste/ # Ajustes de saldo │ ├── ausencia/ # Gestão de ausências │ ├── banco/ # Banco de horas │ ├── configuracao/ # Configurações │ ├── emprego/ # CRUD de emprego │ ├── feriado/ # Gestão de feriados │ ├── foto/ # Foto de comprovante ⭐ │ ├── jornada/ # Cálculos de jornada │ ├── ponto/ # Registro de ponto │ ├── preferencias/ # Preferências do usuário │ ├── relatorio/ # Geração de relatórios │ ├── saldo/ # Cálculo de saldo │ ├── validacao/ # Validações de regras │ └── versaojornada/ # Versionamento de jornada │ ├── 📁 presentation/ # Camada de Apresentação │ ├── components/ # Componentes reutilizáveis │ │ ├── foto/ # ComprovanteImagePicker ⭐ │ │ └── settings/ # Componentes de configurações │ ├── navigation/ # Configuração de rotas │ ├── screen/ # Telas do app │ │ ├── ausencias/ # Gestão de ausências │ │ │ └── components/ │ │ ├── editponto/ # Edição de ponto │ │ ├── history/ # Histórico de pontos │ │ ├── home/ # Tela principal │ │ │ └── components/ │ │ └── settings/ # Configurações │ │ ├── backup/ # Backup/restore │ │ ├── design/ # Tema e aparência │ │ ├── empregos/ # Gestão de empregos │ │ │ └── editar/ │ │ ├── feriados/ # Gestão de feriados │ │ │ ├── components/ │ │ │ ├── editar/ │ │ │ └── lista/ │ │ ├── global/ # Configurações globais │ │ ├── home/ # Home das configurações │ │ ├── horarios/ # Horários padrão │ │ ├── jornada/ # Configuração de jornada │ │ ├── main/ # Principal de settings │ │ │ └── components/ │ │ ├── sobre/ # Sobre o app │ │ └── versoes/ # Versões de jornada │ └── theme/ # Tema Material 3 │ └── 📁 util/ # Utilitários gerais




---

## 📊 Modelo de Dados

### Entidades Principais
┌─────────────────────────────────────────────────────────────────────────────┐ │ EMPREGO │ │ id | nome | ativo | criadoEm | atualizadoEm │ ├─────────────────────────────────────────────────────────────────────────────┤ │ ↓ 1:1 │ ├─────────────────────────────────────────────────────────────────────────────┤ │ CONFIGURACAO_EMPREGO │ │ id | empregoId | habilitarNsr | tipoNsr | habilitarLocalizacao │ │ localizacaoAutomatica | exibirLocalizacaoDetalhes | fotoObrigatoria ⭐ │ │ exibirDuracaoTurno | exibirDuracaoIntervalo | criadoEm | atualizadoEm │ ├─────────────────────────────────────────────────────────────────────────────┤ │ ↓ 1:N │ ├─────────────────────────────────────────────────────────────────────────────┤ │ VERSAO_JORNADA │ │ id | empregoId | titulo | vigenciaInicio | vigenciaFim | cargaHorariaDiaria│ │ toleranciaEntrada | toleranciaSaida | bancoHorasHabilitado | ... │ ├─────────────────────────────────────────────────────────────────────────────┤ │ ↓ 1:N │ ├─────────────────────────────────────────────────────────────────────────────┤ │ HORARIO_DIA_SEMANA │ │ id | versaoJornadaId | diaSemana | entradaManha | saidaManha │ │ entradaTarde | saidaTarde | isDiaUtil | ... │ ├─────────────────────────────────────────────────────────────────────────────┤ │ │ │ ↓ 1:N (do Emprego) │ ├─────────────────────────────────────────────────────────────────────────────┤ │ PONTO │ │ id | empregoId | dataHora | data | hora | horaConsiderada │ │ nsr | observacao | isEditadoManualmente | latitude | longitude | endereco │ │ marcadorId | justificativaInconsistencia | fotoComprovantePath ⭐ │ │ criadoEm | atualizadoEm │ └─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐ │ OUTRAS ENTIDADES │ ├─────────────────────────────────────────────────────────────────────────────┤ │ AUSENCIA │ Férias, folgas, atestados, licenças │ │ FERIADO │ Feriados nacionais, estaduais, municipais │ │ FECHAMENTO_PERIODO│ Fechamento de ciclos de banco de horas │ │ AJUSTE_SALDO │ Ajustes manuais no banco de horas │ │ MARCADOR │ Tags/categorias para pontos │ │ AUDIT_LOG │ Log de auditoria de alterações │ │ CONFIG_PONTES_ANO │ Configuração de pontes facultativas │ └─────────────────────────────────────────────────────────────────────────────┘




### Modelo de Domínio Principal (Ponto)

```kotlin
data class Ponto(
    val id: Long = 0,
    val empregoId: Long,
    val dataHora: LocalDateTime,           // Hora REAL batida
    val horaConsiderada: LocalTime,        // Hora CONSIDERADA (com tolerância)
    val nsr: String? = null,               // Número Sequencial de Registro
    val observacao: String? = null,
    val isEditadoManualmente: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val endereco: String? = null,
    val marcadorId: Long? = null,
    val justificativaInconsistencia: String? = null,
    val fotoComprovantePath: String? = null,  // ⭐ Foto do comprovante
    val criadoEm: LocalDateTime,
    val atualizadoEm: LocalDateTime
) {
    // Propriedades derivadas
    val data: LocalDate
    val hora: LocalTime
    val horaEfetiva: LocalTime
    val temAjusteTolerancia: Boolean
    val temLocalizacao: Boolean
    val temFotoComprovante: Boolean
    // ... formatadores e métodos auxiliares
}
✅ Funcionalidades Implementadas
🟢 Completas e Funcionais



Funcionalidade	Descrição
Registro de Ponto	Automático (hora atual) e manual (seleção de hora)
Navegação por Data	Anterior/próximo/calendário/hoje
Múltiplos Empregos	Seletor com suporte completo, troca rápida
NSR	Número Sequencial de Registro (numérico ou alfanumérico)
Tolerância de Horário	horaConsiderada vs hora real com cálculo automático
Banco de Horas	Acumulado com cálculos diários
Ciclos de Banco	Períodos com fechamento, alertas de vencimento
Fechamento de Ciclo	Dialog de confirmação, transporte de saldo
Ausências	Férias, folgas, atestados, licenças, faltas
Tipos de Folga	Compensação, banco de horas, etc.
Feriados	Cadastro, verificação automática, feriados trabalhados
Versões de Jornada	Histórico de mudanças de horário com vigência
Horários por Dia	Configuração diferenciada por dia da semana
Histórico de Pontos	Visualização com filtros
Edição de Ponto	Alteração com auditoria obrigatória
Exclusão de Ponto	Com motivo obrigatório para auditoria
Configurações Globais	Preferências gerais do app
Temas	Material 3 com suporte a tema dinâmico
Contador Tempo Real	Timer de jornada em andamento
🟡 Parcialmente Implementadas



Funcionalidade	Status	Pendências
Foto de Comprovante	90%	Código comentado na UI, verificar integração
Localização	~70%	Campos existem, verificar captura automática
Backup/Restore	~50%	Estrutura existe, verificar implementação completa
🔴 A Verificar/Implementar



Funcionalidade	Verificação Necessária
Relatórios	Existe usecase/relatorio/ - verificar se há UI
Exportação	PDF, CSV de relatórios
Notificações	Lembretes de ponto
Widget	Acesso rápido na home screen
Sincronização	Existe pasta remote/ - verificar se há backend
💡 O Que Eu Sei Sobre o Projeto
Arquitetura
Clean Architecture bem implementada com separação clara:
domain/ contém regras de negócio puras
data/ lida com persistência e APIs
presentation/ contém UI e ViewModels
MVVM com padrão moderno de UiState + Action + Event
Hilt para injeção de dependência
Room com migrations para versionamento de schema
Jetpack Compose com Material 3
Padrões de Código
Código bem documentado com KDoc
Versionamento de features (@since, @updated)
Separação de concerns consistente
Uso de sealed classes/interfaces para estados e ações
Computed properties para derivar dados do estado
Pontos Fortes
Flexibilidade: Suporte a múltiplos empregos com configurações independentes
Precisão: Distinção entre hora real e hora considerada (tolerância)
Auditoria: Log de alterações com motivo obrigatório
Escalabilidade: Versões de jornada permitem histórico de mudanças
UX: Contador em tempo real, navegação intuitiva
Compliance: NSR para atendimento a regulamentações (REP)
Complexidades Identificadas
Ciclos de banco de horas: Sistema robusto mas complexo
Versões de jornada: Permite mudanças retroativas com vigência
Cálculos de tolerância: Lógica sofisticada para ajustar horários
Tipos de ausência: Diferentes impactos no cálculo (zera jornada, desconta banco, etc.)
🎯 Objetivos e Direção do Projeto
Objetivo Imediato
Finalizar funcionalidade de foto de comprovante
Garantir estabilidade das features existentes
Documentar o projeto para manutenção futura
Objetivos de Curto Prazo
Relatórios exportáveis (PDF/CSV)
Notificações/lembretes de ponto
Backup completo com fotos
Melhorias de UX baseadas em uso
Objetivos de Longo Prazo
Widget para tela inicial
Sincronização com backend/nuvem
Versão multiplataforma (KMP?)
Integração com sistemas de RH
📝 Sugestões de Melhorias
🔴 Prioridade Alta



#	Sugestão	Justificativa
1	Finalizar foto de comprovante	Feature 90% pronta, pequeno esforço para completar
2	Verificar migrations	Garantir que campos novos estão nas migrations
3	Testar fluxos críticos	Registro, edição, exclusão, fechamento de ciclo
🟡 Prioridade Média



#	Sugestão	Justificativa
4	Adicionar toggle de foto nas configurações	Permitir ao usuário habilitar/desabilitar
5	Indicadores visuais de foto/localização	Melhorar UX mostrando quando há dados extras
6	Limpeza de fotos órfãs	Evitar acúmulo de arquivos não utilizados
7	Compressão de imagens	Economizar espaço (max 1MB por foto)
🟢 Prioridade Baixa



#	Sugestão	Justificativa
8	Relatórios PDF/CSV	Exportação para uso externo
9	Notificações de lembrete	Ajudar usuário a não esquecer de bater ponto
10	Widget	Acesso rápido sem abrir o app
11	Testes automatizados	Garantir qualidade em manutenções
📋 Backlog de Tarefas
Sprint Atual: Foto de Comprovante



ID	Tarefa	Prioridade	Estimativa	Status
FOTO-01*	Descomentar ComprovanteImagePicker na HomeScreen	Alta	15min*	⏳
FOTO-02	Verificar método atualizarFotoComprovante no Repository	Alta	30min	⏳
FOTO-03	Verificar método no PontoDao*	Alta	15min	⏳
FOTO-04	Verificar/criar migration para novos campos	Alta	30min	⏳
FOTO-05	Adicionar toggle fotoObrigatoria na configuração	Média	45min	⏳
FOTO-06	Habilitar câmera no componente	Média	30min*	⏳
FOTO-07	Testar fluxo completo	Alta*	30min	⏳
FOTO-08	Adicionar indicador de foto no IntervaloCard	Baixa	20min	⏳
FOTO-09	Adicionar visualização de foto na EditPonto	Média	60min	⏳
FOTO-10	Implementar limpeza de foto ao excluir ponto	Baixa	30min	⏳
Backlog Futuro



ID	Tarefa	Categoria	Prioridade
VERIFY-01	Verificar se localização automática funciona*	Verificação	Média
VERIFY-02	Verificar se relatórios têm UI	Verificação	Média
VERIFY-03	Verificar se backup/restore funciona	Verificação	Média
VERIFY-04	Verificar integração com backend	Verificação	Baixa
FEAT-01	Implementar notificações de lembrete	Feature*	Média
FEAT-02	Criar widget para home screen*	Feature	Baixa
FEAT-03	Exportação PDF de relatórios	Feature	Média
FEAT-04*	Exportação CSV de pontos*	Feature	Média
TECH-01	Adicionar testes unitários para UseCases	Técnico	Média
TECH-02	Adicionar testes de UI com Compose	Técnico	Baixa
📅 Cronograma Sugerido
Semana 1: Finalizar Foto



Dia	Tarefas	Tempo
Dia 1	FOTO-01 a FOTO-04 (ativar e verificar)*	1.5h
Dia 2	FOTO-05 e FOTO-06 (configurações e câmera)*	1.5h
Dia 3	FOTO-07 a FOTO-10 (testes e polimento)	2h
Semana 2: Verificações e Polimento



Dia	Tarefas	Tempo
Dia 1	VERIFY-01 a VERIFY-04*	2h
Dia 2	Correções baseadas nas verificações*	2-3h
Dia 3	Testes gerais e documentação	2h
Semana 3+: Novas Features
Relatórios e exportação
Notificações
Melhorias de UX
🔍 Comandos de Verificação
bash


# ═══════════════════════════════════════════════════════════════
# FOTO DE COMPROVANTE
# ═══════════════════════════════════════════════════════════════

# Verificar Repository de Ponto
cat app/src/main/java/br/com/tlmacedo/meuponto/domain/repository/PontoRepository.kt
cat app/src/main/java/br/com/tlmacedo/meuponto/data/repository/PontoRepositoryImpl.kt

# Verificar DAO
cat app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/dao/PontoDao.kt

# Verificar migrations
cat app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/AppDatabase.kt
ls -la app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/migration/

# Verificar UseCases de foto
ls -la app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/foto/
cat app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/foto/*.kt

# ═══════════════════════════════════════════════════════════════
# CONFIGURAÇÕES DO EMPREGO
# ═══════════════════════════════════════════════════════════════

# Verificar telas de configuração
ls -la app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/empregos/
cat app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/empregos/editar/*.kt

# ═══════════════════════════════════════════════════════════════
# VERIFICAÇÕES GERAIS
# ═══════════════════════════════════════════════════════════════

# Verificar relatórios
ls -la app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/relatorio/
ls -la app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/ | grep -i relat

# Verificar backup
ls -la app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/backup/

# Verificar localização
grep -rn "localizacao\|Localizacao\|location" --include="*.kt" app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/ | head -20

# Verificar remote/api
ls -la app/src/main/java/br/com/tlmacedo/meuponto/data/remote/api/
ls -la app/src/main/java/br/com/tlmacedo/meuponto/data/remote/dto/
📈 Métricas do Projeto



Métrica	Valor
Entidades de banco	14
Modelos de domínio	20+
UseCases	15+ categorias
Telas	10+
Componentes reutilizáveis	20+
🎯 Conclusão
O projeto Meu Ponto está em um estágio avançado de desenvolvimento, com arquitetura sólida e a maioria das funcionalidades core implementadas. Os principais pontos de ação imediata são:

Finalizar foto de comprovante (90% pronto)
Verificar integrações (Repository, DAO, migrations)
Adicionar UI de configuração para features existentes
O código demonstra maturidade técnica e boas práticas. Com as tarefas listadas neste documento, o projeto estará pronto para uma versão de produção estável.

📞 Próximos Passos Imediatos
Ao retornar ao desenvolvimento:

Abrir HomeScreen.kt
Descomentar o bloco do ComprovanteImagePicker (linhas ~164-171)
Compilar e verificar se há erros
Testar com fotoObrigatoria = true em um emprego de teste
Seguir o checklist de tarefas FOTO-*