# 🗺️ Plano de Execução: Otimização e Modernização MeuPonto

Este documento detalha as 5 fases de otimização do aplicativo MeuPonto, servindo como um checklist de progresso.

---

## 🏗️ Fase 1: Estabilização e Infraestrutura Crítica (Concluída ✅)
*Foco: Segurança de dados, resiliência a falhas e base para crescimento.*

- [x] **1.1. Robustez do CloudBackup**
    - [x] Implementar lógica de `uploadBackup` no `CloudBackupRepository`.
    - [x] Adicionar política de retentativas (Exponential Backoff) no `CloudBackupWorker`.
    - [x] Implementar `sincronizarFotos` com restrição de rede (apenas Wi-Fi configurado via WorkManager Constraints).
- [x] **1.2. Resiliência do Scanner de Documentos**
    - [x] Tratar ausência do Google Play Services no `DocumentScannerWrapper` via `isScannerAvailable()`.
    - [x] Implementar fallback amigável ou instrução de instalação (Implementado no wrapper).
- [x] **1.3. Monitoramento e Erros**
    - [x] Integrar Firebase Crashlytics.
    - [x] Implementar tratamento global de `SecurityException` no `LocationService`.
    - [x] Criar sistema de logs críticos com feedback visual (Notificações via `SistemaNotificacaoService`).

---

## 🧠 Fase 2: Inteligência e Processamento Avançado (Concluída ✅)
*Foco: Automação do registro e precisão cirúrgica na extração de dados.*

- [x] **2.1. Upgrade do OCR (OcrService)**
    - [x] Implementar binarização e correção de perspectiva no `ImageProcessor`.
    - [x] Adicionar Contexto Temporal (priorizar horários baseados no turno).
    - [x] Implementar validação de NSR (Número Sequencial de Registro) para evitar duplicidade.
- [x] **2.2. Inteligência Geográfica**
    - [x] Implementar Cache de Geocodificação Reversa (endereços frequentes).
    - [x] Vincular `registroAutomaticoGeofencing` à ativação real do serviço de background.
- [x] **2.3. Gestão de Imagens Recortadas**
    - [x] Definir ciclo de vida para imagens temporárias do OCR para evitar lixo no storage.

---

## 👥 Fase 3: Onboarding e Regras de Negócio (Em Andamento 🚧)
*Foco: Experiência do usuário e personalização do contrato de trabalho.*

- [x] **3.1. Fluxo de Boas-Vindas (Onboarding)**
    - [x] Implementar detecção inteligente de primeira execução (DataStore + Banco de Dados).
    - [x] Criar sequência de 7 telas (Bem-vindo, Empresa, Dias, Opções Registro, RH/Banco, Sincronização, Permissões).
    - [x] Persistência automática das configurações iniciais em múltiplas entidades (Emprego, Config, Versão).
- [ ] **3.2. Interface de Configurações de Jornada**
    - [ ] Tela para editar Carga Horária, Intervalo Mínimo e Tolerâncias.
    - [ ] Integração total com `PreferencesDataStore`.
- [ ] **3.3. Configuração de Localização**
    - [ ] Criar seletor de local de trabalho no mapa.
    - [ ] Configuração visual do raio de Geofencing.

---

## 🖼️ Fase 4: Gestão de Ativos e Utilidades
*Foco: Organização de comprovantes e controle de armazenamento.*

- [ ] **4.1. Gerenciador de Comprovantes**
    - [ ] Criar galeria integrada ao banco de dados (Room).
    - [ ] Implementar filtros (Data, Emprego, Status de Associação).
    - [ ] Implementar exclusão em lote para limpeza de storage.
- [ ] **4.2. Painel de Sincronização**
    - [ ] Criar dashboard com status do último backup (Local vs Nuvem).
    - [ ] Botão de "Sincronizar Agora" com feedback de progresso.
- [ ] **4.3. Refinamento de Captura**
    - [ ] Adicionar detecção de bordas no scanner.
    - [ ] Implementar corte manual opcional após a foto.

---

## ✨ Fase 5: Modernização Visual e Recursos Pro
*Foco: Estética Material 3, acessibilidade e performance.*

- [ ] **5.1. Padronização Material Design 3**
    - [ ] Aplicar Cores Dinâmicas em todos os componentes.
    - [ ] Implementar Shimmers para estados de carregamento.
- [ ] **5.2. Sincronização em Tempo Real**
    - [ ] (Opcional) Migrar para Firebase Realtime Database para múltiplos dispositivos.
- [ ] **5.3. Acessibilidade e Polimento**
    - [ ] Revisar suporte ao TalkBack em todas as telas.
    - [ ] Otimizar performance de transições e animações Compose.

---
*Documento gerado em: $(date)*
