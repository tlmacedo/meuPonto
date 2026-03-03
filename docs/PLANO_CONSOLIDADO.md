

```markdown
# MeuPonto — Plano Consolidado (2026)

Plano unificado com Correções Necessárias, Ideias de Melhorias e Novas Funcionalidades.  
Fonte: auditoria técnica completa do app MeuPonto.

---

## Sumário

- [1. Correções Necessárias](#1-correções-necessárias)
  - [1.1 Críticas (imediatas)](#11-críticas-imediatas)
  - [1.2 Alta prioridade](#12-alta-prioridade)
  - [1.3 Média prioridade](#13-média-prioridade)
  - [1.4 Baixa prioridade](#14-baixa-prioridade)
- [2. Ideias de Melhorias (Arquitetura, UX e Qualidade)](#2-ideias-de-melhorias-arquitetura-ux-e-qualidade)
- [3. Novas Funcionalidades](#3-novas-funcionalidades)
- [4. Resumo por Impacto (priorização de entrega)](#4-resumo-por-impacto-priorização-de-entrega)
- [5. Anexos](#5-anexos)
  - [5.1 Checklist rápido](#51-checklist-rápido)
  - [5.2 Ordem de execução recomendada](#52-ordem-de-execução-recomendada)
  - [5.3 Arquivos principais a modificar](#53-arquivos-principais-a-modificar)
  - [5.4 Exemplos de implementação](#54-exemplos-de-implementação)

---

## 1. Correções Necessárias

Auditoria de segurança, código e planejamento — itens consolidados.

### 1.1 Críticas (imediatas)
- Remover todos os logs sensíveis em produção (substituir `android.util.Log*` por Timber condicionado a DEBUG).
- Desabilitar network logging (`HttpLoggingInterceptor`) em produção (usar `Level.NONE` em release).
- Remover método temporário de correção executado em `init()`:
  - `HomeViewModel.reverterFechamentosIncorretos()` — risco de destruição indevida de dados.
- Configurar validação SSL/TLS (TLS moderno + Certificate Pinning ou Network Security Config estrita).

### 1.2 Alta prioridade
- Criptografar dados sensíveis (latitude/longitude/endereço) — SQLCipher/EncryptedFile/Google Tink.
- Remover 5 métodos deprecados em `PontoRepository*`:
  - `buscarPontosPorData`, `buscarUltimoPontoDoDia`, `observarPontosPorData`, `observarTodos`, `observarPontosPorPeriodo`.
- Bloquear inserção de dados de teste em produção:
  - `DatabaseModule.inserirDadosIniciais()` apenas em `BuildConfig.DEBUG`.
- Criar validação centralizada (email, telefone, NSR, localização etc.).

### 1.3 Média prioridade
- Consolidar migrações do banco (1→16) ou criar migration 16→17 que recrie/compacte.
- Remover campo deprecado `subTipoFolga` de `AusenciaEntity` (migration).
- Melhorar tratamento de erros:
  - Exceções de domínio específicas (ex.: `ValidacaoException`, `EmpregoNaoEncontradoException`).
  - Mapeamento consistente para UI + logging estruturado.
- TODOs pendentes:
  - Captura de foto com `FileProvider` (AusenciaFormScreen).
  - Cálculo de saldo acumulado do banco de horas (HistoryScreen).

### 1.4 Baixa prioridade
- Persistir tipo de ponto (ENTRADA/SAÍDA) explicitamente no modelo/DB (evitar inferência por índice).
- Padronizar mapeamentos (mappers/extensões) entre entidades e domínio.
- Adicionar bateria de testes unitários (domínio/data).

---

## 2. Ideias de Melhorias (Arquitetura, UX e Qualidade)

Modernizações tecnológicas e aprimoramentos de experiência sem alterar regras de negócio centrais.

- Paging 3 para listas grandes e scroll infinito (melhor performance e UX).
- Single Source of Truth com StateFlow compartilhado entre telas (menos cargas duplicadas e inconsistências).
- WorkManager para tarefas de segundo plano (sincronização, lembretes, backups).
- Material Design 3 (Material You) — componentes atualizados, temas dinâmicos e acessibilidade.
- Gradle Version Catalog (libs.versions.toml) — gestão de dependências centralizada.
- Dark Mode adaptativo por horário (com override manual).
- Onboarding interativo (fluxo guiado: configurar emprego, primeiro ponto, banco de horas, navegação, dicas).
- Modo Simplificado (esconde configurações avançadas para usuários casuais).
- Gestos de navegação (swipe/long-press/pinch/refresh) para reduzir cliques.
- Animações e transições (skeleton screens, bounce/confirm, expand/collapse) para feedback de qualidade.
- Acessibilidade aprimorada: TalkBack, navegação por teclado, fonte dinâmica, alto contraste.
- Suporte a múltiplos idiomas (PT-BR, EN-US/UK, ES-ES/LATAM).

---

## 3. Novas Funcionalidades

- Geofencing para registro automático (entrada/saída por raio configurado, com log de auditoria e confirmação opcional).
- Biometria (Face/Fingerprint) no registro e em ações críticas (excluir/editar/relatórios/exportações/configurações).
- Widget de início (registrar ponto 1-clique + próximo tipo, horário atual, nome do emprego e saldo do dia).
- Modo offline robusto (fila de operações, indicador de estado, cache de dados da API, sincronização ao reconectar).
- Dashboard com gráficos e métricas (tendências, atrasos, extras, saldo do banco de horas).
- Comparação vs meta mensal (barra de progresso, alertas de risco, comparação com mês anterior).
- Exportação em múltiplos formatos (PDF, imagem, Excel/CSV, link online).
- Comparativo semanal/mensal (lado a lado; tendência de horas; média móvel de saldos; destaques).
- Lembretes inteligentes de ponto (com base no histórico e padrões do usuário).
- Alertas de limite do banco de horas (níveis: +40h, +60h, -10h, -20h com recomendações).
- Notificação de novos feriados (alerta 7 dias antes; planejamento antecipado).
- Log de localização em mapa (pins, filtro por período; cores para entrada/saída; auditoria).
- Assinatura digital em editar/excluir (canvas, evidência vinculada ao AuditLog).
- Multi-device sync (celular/tablet; tempo real; resolução automática de conflitos).
- Compartilhamento de relatórios (envio por email/WhatsApp, templates prontos para RH/contadores).
- Integração com calendário (Google Calendar etc.) — bidirecional com feriados/folgas/ausências.
- Reconhecimento de padrões (IA simples) — horários médios, dias com mais atrasos/saídas cedo; sugestões/alertas.
- Múltiplos empregos com jornada compartilhada (% por emprego; cálculo automático; relatório consolidado).
- Sistema de provas e documentos (atestados, declarações, justificativas com foto/descrição; auditoria robusta).
- Integração com clock externo (REP) — QR/NFC; sincronização bidirecional; validação de formato.
- Modo Kiosque para empresa (tablet único para registro com autenticação; seleção de usuário biometria/PIN).
- Integração com Google Tasks/To-Do (planejamento e registro de ponto por tarefa).
- Integração com Google Maps/Calendar (detectar eventos/viagens que afetem jornada).
- Integração com apps de contabilidade/ERP (exportação padronizada; automação e conformidade).
- Comparação anônima com colegas (benchmark de produtividade/pontualidade por setor; identificação de outliers).

---

## 4. Resumo por Impacto (priorização de entrega)

Alinha melhorias e novas funcionalidades mais visíveis/valiosas primeiro.

### Alto Impacto (implementar primeiro)
1. Geofencing para registro automático  
2. Dashboard com gráficos e métricas  
3. Material Design 3 (Material You)  
4. Widget de início  
5. Lembretes inteligentes de ponto  
6. Exportação em múltiplos formatos  
7. Onboarding interativo  
8. Animações e transições  

### Impacto Médio (depois)
9. Reconhecimento facial/biometria  
10. Multi-device sync  
11. Modo offline robusto  
12. Previsão de banco de horas (metas/tendências)  
13. Compartilhamento de relatórios  
14. Comparativo semanal/mensal  
15. Análise de tendências de atrasos  
16. Sistema de provas e documentos  

### Baixo Impacto / Nice to Have
17. Paging 3 para carregamento infinito  
18. WorkManager para tarefas em background  
19. Modo kiosque para empresa  
20. Reconhecimento de padrões com IA  
21. Integração com clock externo  
22. Suporte a múltiplos idiomas  
23. Comparação anônima com colegas  

---

## 5. Anexos

### 5.1 Checklist rápido
- [ ] Remover `android.util.Log*` e usar Timber só em DEBUG  
- [ ] Desabilitar network logging em release  
- [ ] Remover `reverterFechamentosIncorretos()`  
- [ ] Dados de teste somente em DEBUG  
- [ ] Habilitar SSL/TLS moderno + pinning/network-security-config  
- [ ] Criptografar localização (lat/long/endereço)  
- [ ] Remover métodos deprecados (5)  
- [ ] Criar camada de validação centralizada  
- [ ] Consolidar migrações de banco  
- [ ] Remover coluna `subTipoFolga` (migration)  
- [ ] Tratar erros com exceções de domínio + logging estruturado  
- [ ] Implementar TODOs pendentes  
- [ ] Padronizar mappers  
- [ ] Persistir tipo de ponto (ENTRADA/SAÍDA)  

### 5.2 Ordem de execução recomendada
- Semana 1 (CRÍTICAS): itens 1–4  
- Semana 2 (ALTA): itens 5–8  
- Semanas 3–4 (MÉDIA): itens 9–11  
- Mês 2 (BAIXA): itens 12–14  

### 5.3 Arquivos principais a modificar
- `presentation/screen/home/HomeViewModel.kt` (CRÍTICO)  
- `di/NetworkModule.kt` (ALTA)  
- `di/DatabaseModule.kt` (ALTA)  
- `domain/repository/PontoRepository.kt` (ALTA)  
- `data/repository/PontoRepositoryImpl.kt` (ALTA)  
- `data/local/database/entity/PontoEntity.kt` (ALTA)  
- `data/local/database/entity/AusenciaEntity.kt` (MÉDIA)  
- `domain/usecase/ponto/RegistrarPontoUseCase.kt` (MÉDIA)  

### 5.4 Exemplos de implementação

Certificate Pinning (OkHttp):
```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("api.seudominio.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
    .build()

val client = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

Network logging somente em DEBUG:
```kotlin
val logging = HttpLoggingInterceptor().apply {
    level = if (BuildConfig.DEBUG)
        HttpLoggingInterceptor.Level.BODY
    else
        HttpLoggingInterceptor.Level.NONE
}
```

Timber apenas em debug (Application):
```kotlin
override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) {
        Timber.plant(Timber.DebugTree())
    }
}
```

---

### Observações Finais
- Documento consolidado a partir da auditoria técnica e backlog completo (itens 1–40).
- Recomenda-se iniciar pelas correções críticas e, em paralelo, preparar a fundação das melhorias de arquitetura (Paging 3, StateFlow compartilhado, MD3).
```