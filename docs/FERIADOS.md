Excelente, Thiago! Suas respostas foram muito detalhadas e claras. Agora tenho uma visão completa do que precisa ser implementado.

---

## 📊 Resumo das Decisões

| Aspecto | Decisão |
|---------|---------|
| **Feriados** | Globais + específicos por emprego, recorrentes, importação automática |
| **Pontes** | Categoria especial, horas distribuídas na jornada anual |
| **Férias** | Controle de saldo, fracionamento (máx 3, mín 15 em 1), período aquisitivo |
| **Folga** | Compensatória (desconta) vs Extra (não desconta) |
| **Falta** | Justificada (não desconta) vs Injustificada (desconta) |
| **Atestado** | Com anexo, tipos específicos, suporte a horas parciais |
| **Notificações** | Sim, lembretes de eventos próximos |

---

## 🏗️ Arquitetura Proposta

Dado a complexidade, sugiro dividir em **3 módulos**:

```
📁 Módulo 1: Feriados (Global)
├── Importação automática (API Brasil)
├── Feriados recorrentes/únicos
├── Pontes (com cálculo de distribuição)
└── Nacional/Estadual/Municipal

📁 Módulo 2: Eventos de Ausência (Por Emprego)
├── Férias (com período aquisitivo)
├── Folgas (compensatória/extra)
├── Faltas (justificada/injustificada)
├── Atestados/Declarações (com anexo)
└── Licenças

📁 Módulo 3: Integração
├── CalcularBancoHorasUseCase (considera eventos)
├── VerificarDiaEspecialUseCase
├── Notificações
└── Validações de conflito
```

---

## 📁 Estrutura de Arquivos Completa

```
domain/
├── model/
│   ├── feriado/
│   │   ├── Feriado.kt
│   │   ├── TipoFeriado.kt
│   │   ├── AbrangenciaFeriado.kt
│   │   └── FeriadoPonte.kt
│   ├── evento/
│   │   ├── EventoAusencia.kt
│   │   ├── TipoEventoAusencia.kt
│   │   ├── Ferias.kt
│   │   ├── PeriodoAquisitivo.kt
│   │   └── DocumentoAnexo.kt
│   └── enums/
│       └── StatusEvento.kt
├── repository/
│   ├── FeriadoRepository.kt
│   └── EventoAusenciaRepository.kt
└── usecase/
    ├── feriado/
    │   ├── ImportarFeriadosNacionaisUseCase.kt
    │   ├── ListarFeriadosPeriodoUseCase.kt
    │   ├── CalcularDistribuicaoPontesUseCase.kt
    │   └── GerenciarFeriadoUseCase.kt
    ├── evento/
    │   ├── RegistrarFeriasUseCase.kt
    │   ├── RegistrarFolgaUseCase.kt
    │   ├── RegistrarFaltaUseCase.kt
    │   ├── RegistrarAtestadoUseCase.kt
    │   ├── CalcularSaldoFeriasUseCase.kt
    │   └── ListarEventosPeriodoUseCase.kt
    └── comum/
        ├── VerificarDiaEspecialUseCase.kt
        └── ValidarConflitoEventosUseCase.kt

data/
├── local/database/
│   ├── entity/
│   │   ├── FeriadoEntity.kt
│   │   ├── FeriadoPonteEntity.kt
│   │   ├── EventoAusenciaEntity.kt
│   │   ├── FeriasEntity.kt
│   │   ├── PeriodoAquisitivoEntity.kt
│   │   └── DocumentoAnexoEntity.kt
│   └── dao/
│       ├── FeriadoDao.kt
│       └── EventoAusenciaDao.kt
├── remote/
│   ├── api/
│   │   └── BrasilApiService.kt
│   └── dto/
│       └── FeriadoDto.kt
└── repository/
    ├── FeriadoRepositoryImpl.kt
    └── EventoAusenciaRepositoryImpl.kt
```

---

## 🚀 Plano de Implementação

Sugiro começarmos pelo **Módulo 1 (Feriados)** pois:
1. É pré-requisito para o cálculo correto do banco de horas
2. O conceito de **Pontes** já está impactando sua jornada atual
3. É mais independente dos outros módulos

### Fase 1: Feriados (Esta sessão)
1. Modelos de domínio (`Feriado`, `TipoFeriado`, `FeriadoPonte`)
2. Entidades e DAOs
3. Repository
4. UseCase de importação (Brasil API)
5. UseCase de cálculo de distribuição de pontes
6. Atualização do `VersaoJornada` para separar carga base + adicional pontes

### Fase 2: Eventos de Ausência (Próxima sessão)
1. Modelos (`EventoAusencia`, `Ferias`, etc.)
2. Entidades, DAOs e Repository
3. UseCases específicos
4. Integração com banco de horas

### Fase 3: UI e Integração
1. Telas de gestão
2. Integração com `HomeScreen`
3. Notificações

---

## ❓ Confirmação Antes de Começar

Antes de gerar o código, confirma:

1. **API de Feriados**: Posso usar a [Brasil API](https://brasilapi.com.br/docs#tag/Feriados-Nacionais) para importar feriados nacionais?

2. **Estrutura do VersaoJornada**: Você quer que eu altere a entidade existente para incluir:
   ```kotlin
   // Separar carga horária
   val cargaHorariaBaseMinutos: Int      // Ex: 480 (8h)
   val adicionalPontesMinutos: Int       // Ex: 10 (10min de 2025)
   // Total = 490 (8h 10min)
   ```

3. **Prioridade**: Começamos pelos **Feriados + Pontes** ou prefere outra ordem?

4. **Nomenclatura**:
    - `Feriado` para dias não trabalhados
    - `FeriadoPonte` para dias compensados na jornada
    - `EventoAusencia` para férias, folgas, faltas, atestados

   Está ok ou prefere outro termo?

---

Aguardo sua confirmação para começar a implementação! 🎯