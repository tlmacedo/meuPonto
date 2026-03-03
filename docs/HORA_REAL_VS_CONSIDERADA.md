# Sistema de Hora Real vs Hora Considerada

Este documento detalha como o sistema MeuPonto lida com horários de ponto, separando a **hora real** registrada da **hora considerada** para cálculos.

## 📋 Conceitos Fundamentais

### Hora Real (`dataHora`)
- **Definição**: O horário exato em que o ponto foi registrado
- **Uso**: Exibição ao usuário, auditoria, logs
- **Imutável**: Nunca é alterada após o registro

### Hora Considerada (`horaConsiderada`)
- **Definição**: O horário ajustado após aplicar tolerâncias e regras de negócio
- **Uso**: Todos os cálculos de horas trabalhadas, saldos, intervalos
- **Calculável**: Pode ser igual à hora real (se não houver ajustes) ou diferente (se houver tolerância aplicada)

### Hora Efetiva (`dataHoraEfetiva`)
- **Definição**: Propriedade computada que retorna horaConsiderada se existir, senão hora real
- **Uso**: Cálculos padronizados que sempre usam o horário correto
- **Fórmula**: `horaConsiderada ?: dataHora`

## 🏗️ Arquitetura

### Modelo Ponto

```kotlin
data class Ponto(
    val dataHora: LocalDateTime,              // Hora real (registro)
    val horaConsiderada: LocalDateTime? = null, // Hora ajustada (opcional)
    // ...
) {
    val dataHoraEfetiva: LocalDateTime        // Propriedade computada
        get() = horaConsiderada ?: dataHora
    
    val horaEfetiva: LocalTime
        get() = dataHoraEfetiva.toLocalTime()
    
    val temAjusteTolerancia: Boolean
        get() = horaConsiderada != null && horaConsiderada != dataHora
}
```

## 🔄 Fluxo de Cálculo

### 1. Registro do Ponto

```
Usuário bate ponto → dataHora = hora atual (ex: 08:05)
                  → horaConsiderada = null
```

### 2. Aplicação de Tolerância

Quando há tolerância configurada para intervalo:

```
Cenário: Intervalo mínimo = 60 min, Tolerância = +15 min

1. Saída almoço: 12:00 (hora real)
2. Retorno almoço: 13:05 (hora real)
3. Duração real: 65 minutos
4. Dentro da tolerância? Sim (65 ≤ 60 + 15)
5. Ajuste: horaConsiderada do retorno = 13:00 (12:00 + 60 min)
6. Cálculo: 13:00 - 12:00 = 60 minutos exatos
```

### 3. Cálculos que Usam Hora Considerada

Todos os cálculos de métricas usam `dataHoraEfetiva`:

✅ **Usam Hora Considerada:**
- Horas trabalhadas (`horasTrabalhadas`)
- Duração de turnos/intervalos
- Tempo de intervalo (considerado)
- Saldo do dia (`saldoDia`)
- Banco de horas
- Histórico de horas
- Tempo em andamento (turno aberto)

❌ **Usam Hora Real:**
- Exibição do horário na UI (`horaFormatada`)
- Validações de registro futuro/antigo
- Ordenação dos pontos
- Auditoria e logs

## 📊 Exemplo Prático

### Cenário: Trabalhador com tolerância de intervalo

```
Jornada: 08:00 - 17:00 (8h com 1h de intervalo)
Intervalo mínimo: 60 min
Tolerância intervalo: +15 min
```

#### Registros (Hora Real):
```
1. Entrada:     07:58  (dataHora)
2. Saída almoço: 12:03  (dataHora)
3. Retorno:     13:12  (dataHora) ← +12 min de tolerância
4. Saída:       17:02  (dataHora)
```

#### Cálculo de Tolerância:
```
Pausa real: 13:12 - 12:03 = 69 minutos
Limite: 60 + 15 = 75 minutos
69 ≤ 75? Sim ✓
Ajuste: horaConsiderada do retorno = 13:03 (12:03 + 60 min)
```

#### Registros (Hora Considerada):
```
1. Entrada:     07:58  (dataHoraEfetiva = 07:58)
2. Saída almoço: 12:03  (dataHoraEfetiva = 12:03)
3. Retorno:     13:03  (dataHoraEfetiva = 13:03) ← ajustado!
4. Saída:       17:02  (dataHoraEfetiva = 17:02)
```

#### Cálculos com Hora Considerada:
```
Turno manhã:   12:03 - 07:58 = 4h 05min
Intervalo:     13:03 - 12:03 = 1h 00min (exato!)
Turno tarde:   17:02 - 13:03 = 3h 59min
Total:         8h 04min
Jornada:       8h 00min
Saldo:         +00h 04min ✓
```

#### Cálculos com Hora Real (SE FOSSEM USADOS):
```
Turno manhã:   12:03 - 07:58 = 4h 05min
Intervalo:     13:12 - 12:03 = 1h 09min
Turno tarde:   17:02 - 13:12 = 3h 50min
Total:         8h 00min (exato, mas por acaso)
Jornada:       8h 00min
Saldo:         00h 00min
```

## 🔧 Regras de Negócio

### Quando a Hora Considerada é Aplicada?

1. **Tolerância de Intervalo**
   - Se o tempo real de pausa > intervalo mínimo
   - E tempo real de pausa ≤ intervalo mínimo + tolerância
   - Ajusta a hora de retorno para: saída + intervalo mínimo

2. **Tolerância de Entrada/Saída**
   - Se implementado, pode ajustar pontos de entrada/saída
   - Atualmente não implementado no sistema

3. **Edição Manual**
   - Usuário pode editar manualmente um ponto
   - A hora real permanece para auditoria
   - Hora considerada pode ser ajustada

### Quando a Hora Considerada NÃO é Aplicada?

1. **Intervalo muito longo** (excede tolerância)
   - Mantém hora real
   - Pode gerar débito se intervalo mínimo não foi respeitado

2. **Intervalo muito curto** (abaixo do mínimo)
   - Mantém hora real
   - Gera aviso de inconsistência

3. **Não há tolerância configurada**
   - Hora considerada = hora real
   - Nenhum ajuste aplicado

## 💾 Persistência no Banco de Dados

### Tabela `pontos`

```sql
CREATE TABLE pontos (
    id INTEGER PRIMARY KEY,
    dataHora TEXT NOT NULL,              -- Hora real (obrigatória)
    horaConsiderada TEXT,                 -- Hora ajustada (opcional)
    -- outros campos...
);
```

### Estratégia de Migração

O campo `horaConsiderada` foi adicionado na versão 6.0:
- Pontos existentes: `horaConsiderada = NULL`
- Novos pontos: `horaConsiderada` preenchido após cálculo de tolerância
- Cálculos usam `COALESCE(horaConsiderada, dataHora)`

## 🎯 Regras de Uso no Código

### ✅ CORRETO: Usar dataHoraEfetiva para cálculos

```kotlin
// Cálculo de duração
val duracao = Duration.between(
    entrada.dataHoraEfetiva,  // ✓ Usa hora considerada
    saida.dataHoraEfetiva
)

// Cálculo de saldo
val trabalhado = pontos.sumOf { 
    it.dataHoraEfetiva  // ✓ Usa hora considerada
}
```

### ❌ INCORRETO: Usar dataHora para cálculos

```kotlin
// Cálculo de duração
val duracao = Duration.between(
    entrada.dataHora,  // ✗ Usa hora real (errado para cálculos)
    saida.dataHora
)

// Cálculo de saldo
val trabalhado = pontos.sumOf { 
    it.dataHora  // ✗ Usa hora real (errado para cálculos)
}
```

### ✅ CORRETO: Usar dataHora para exibição

```kotlin
// Exibir horário na UI
Text(ponto.horaFormatada)  // ✓ Usa hora real para mostrar ao usuário

// Ordenação
val ordenados = pontos.sortedBy { it.dataHora }  // ✓ Usa hora real
```

## 📌 Pontos Importantes

1. **Single Source of Truth**
   - `dataHoraEfetiva` é a propriedade que deve ser usada em TODOS os cálculos
   - Isso garante consistência em todo o sistema

2. **Rastreabilidade**
   - A hora real (`dataHora`) nunca é alterada
   - Permite auditoria completa de todos os registros

3. **Transparência**
   - O usuário pode ver se houve ajuste (`temAjusteTolerancia`)
   - A hora real é sempre exibida na interface
   - Ajustes são aplicados de forma previsível

4. **Performance**
   - `horaConsiderada` é calculada uma vez e armazenada
   - Não precisa ser recalculada a cada acesso

## 🔍 Validação e Testes

### Casos de Teste

1. **Sem tolerância**
   - Hora real = hora considerada
   - Cálculos usam hora real diretamente

2. **Com tolerância aplicada**
   - Hora considerada diferente da real
   - Cálculos usam hora ajustada

3. **Fora da tolerância**
   - Intervalo muito longo
   - Mantém hora real

4. **Edição manual**
   - Hora alterada pelo usuário
   - Hora considerada pode ser recalculada

## 📝 Checklist de Implementação

Para garantir que todos os cálculos usam hora considerada:

- [x] Modelo `Ponto` tem propriedade `dataHoraEfetiva`
- [x] `ResumoDia` usa `dataHoraEfetiva` para cálculos de intervalos
- [x] `CalcularSaldoDiaUseCase` usa `horaEfetiva`
- [x] `CalcularResumoDiaUseCase` usa pontos com `horaConsiderada` preenchida
- [x] `ToleranciaUtils` preenche `horaConsiderada` nos pontos
- [x] Validações usam `dataHora` (hora real)
- [x] Exibição usa `hora` (hora real)
- [x] Ordenação usa `dataHora` (hora real)

## 🔄 Futuras Melhorias

1. **Tolerância de Entrada**
   - Aplicar tolerância também para pontos de entrada/saída
   - Ajustar para horário ideal se dentro da tolerância

2. **Histórico de Ajustes**
   - Registrar quando e por que `horaConsiderada` foi alterada
   - Log de auditoria mais detalhado

3. **Configuração de Tolerância por Turno**
   - Tolerâncias diferentes para manhã/tarde
   - Regras mais flexíveis

---

**Autor:** Thiago Macedo
**Data:** 27/02/2026
**Versão:** 7.0.0