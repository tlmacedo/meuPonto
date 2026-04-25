// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/ausencia/TipoAusencia.kt
package br.com.tlmacedo.meuponto.domain.model.ausencia

import br.com.tlmacedo.meuponto.domain.model.TipoDiaEspecial

/**
 * Tipos de ausência disponíveis no sistema Meu Ponto.
 *
 * Cada tipo representa uma categoria de afastamento com regras específicas
 * de impacto no banco de horas e requisitos de documentação.
 *
 * ## Resumo de Comportamentos
 *
 * | Tipo                | Zera Jornada | Impacto no Banco               | Planejada |
 * |---------------------|--------------|--------------------------------|-----------|
 * | [FERIAS]            | ✅ Sim       | Neutro (abonado)               | ✅ Sim    |
 * | [ATESTADO]          | ⚠️ Condicional* | Abona restante da jornada   | ❌ Não    |
 * | [DECLARACAO]        | ❌ Não       | Abona apenas tempo parcial     | ❌ Não    |
 * | [FALTA_JUSTIFICADA] | ✅ Sim       | Neutro (abonado)               | ✅ Sim    |
 * | [FOLGA]             | ❌ Não       | ⬇️ Desconta do banco           | ✅ Sim    |
 * | [FALTA_INJUSTIFICADA] | ❌ Não     | ⬇️ Gera débito (penalidade)    | ❌ Não    |
 *
 * ### *Comportamento do ATESTADO:
 * - **Sem registros de ponto no dia:** Zera a jornada completamente (abonado)
 * - **Com registros de ponto no dia:** Abona as horas restantes até completar a jornada
 *   - Ex: Jornada 8h, trabalhou 3h antes de passar mal → abona 5h
 *
 * ## Exemplos de Uso
 *
 * ```kotlin
 * // Férias de 15 dias
 * Ausencia.criarFerias(
 *     empregoId = 1L,
 *     dataInicio = LocalDate.of(2025, 7, 1),
 *     dataFim = LocalDate.of(2025, 7, 15),
 *     periodoAquisitivo = "2024/2025"
 * )
 *
 * // Atestado - passou mal no trabalho às 11h
 * Ausencia.criarAtestado(
 *     empregoId = 1L,
 *     dataInicio = LocalDate.now(),
 *     motivo = "Passou mal após o almoço"
 * )
 * // Sistema detecta registros de ponto e abona apenas horas restantes
 *
 * // Declaração - consulta das 14h às 16h, abono de 1h30
 * Ausencia.criarDeclaracao(
 *     empregoId = 1L,
 *     data = LocalDate.now(),
 *     horaInicio = LocalTime.of(14, 0),
 *     duracaoDeclaracaoMinutos = 120,
 *     duracaoAbonoMinutos = 90,
 *     motivo = "Consulta cardiologista"
 * )
 *
 * // Falta justificada - casamento (3 dias, abonado)
 * Ausencia.criarFaltaJustificada(
 *     empregoId = 1L,
 *     dataInicio = LocalDate.of(2025, 5, 10),
 *     dataFim = LocalDate.of(2025, 5, 12),
 *     motivo = "Casamento"
 * )
 *
 * // Folga - compensação de banco de horas (desconta do banco)
 * Ausencia.criarFolga(
 *     empregoId = 1L,
 *     dataInicio = LocalDate.of(2025, 2, 28),
 *     observacao = "Compensação antes do fechamento"
 * )
 *
 * // Falta injustificada - penalidade
 * Ausencia.criarFaltaInjustificada(
 *     empregoId = 1L,
 *     dataInicio = LocalDate.now()
 * )
 * ```
 *
 * @property descricao Nome amigável para exibição na UI
 * @property emoji Ícone visual representativo
 * @property zeraJornada Se `true`, o dia é abonado completamente
 * @property requerDocumento Se `true`, recomenda-se anexar comprovante
 * @property permiteAnexo Se `true`, permite upload de imagem
 * @property isPlanejada Se `true`, é uma ausência programada antecipadamente
 * @property explicacaoImpacto Texto explicativo do impacto no banco de horas
 * @property exemploUso Exemplos práticos de quando usar este tipo
 * @property labelObservacao Label do campo de observação no formulário
 * @property placeholderObservacao Placeholder para o campo de observação
 *
 * @author Thiago
 * @since 4.0.0
 * @updated 5.5.0 - Remoção de SubTipoFolga, revisão completa da semântica
 */
enum class TipoAusencia(
    val descricao: String,
    val emoji: String,
    val zeraJornada: Boolean,
    val requerDocumento: Boolean = false,
    val permiteAnexo: Boolean = false,
    val isPlanejada: Boolean = false,
    val explicacaoImpacto: String,
    val exemploUso: String,
    val labelObservacao: String,
    val placeholderObservacao: String
) {
    /**
     * Período de férias remuneradas.
     *
     * - **Planejada:** ✅ Sim
     * - **Impacto:** Jornada zerada, dia totalmente abonado
     * - **Registro de ponto:** Bloqueado durante o período
     * - **Se trabalhar:** Horas contadas como extra
     *
     * ### Quando usar:
     * - Férias anuais
     * - Férias coletivas
     * - Recesso remunerado
     */
    FERIAS(
        descricao = "Férias",
        emoji = "🏖️",
        zeraJornada = true,
        requerDocumento = false,
        permiteAnexo = false,
        isPlanejada = true,
        explicacaoImpacto = "Jornada zerada. Se trabalhar, as horas serão contadas como extra.",
        exemploUso = "Férias anuais, férias coletivas, recesso remunerado.",
        labelObservacao = "Observação / comentário",
        placeholderObservacao = "Ex: 10/11/2021 a 09/11/2022"
    ),

    /**
     * Afastamento por motivo de saúde, emergência ou falecimento.
     *
     * - **Planejada:** ❌ Não (imprevistos)
     * - **Impacto condicional:**
     *   - Sem registros de ponto → Zera jornada (dia abonado)
     *   - Com registros de ponto → Abona horas restantes até completar jornada
     * - **Documentação:** Requer atestado médico ou documento comprobatório
     *
     * ### Quando usar:
     * - Faltou o dia inteiro por doença
     * - Passou mal e foi mandado para casa
     * - Saiu para fazer exames urgentes
     * - Falecimento de parente (saiu no meio do expediente)
     * - Acompanhamento de dependente em emergência
     *
     * ### Comportamento inteligente:
     * O sistema verifica se há registros de ponto no dia:
     * - **Sem registros:** Considera dia inteiro abonado (zeraJornada = true)
     * - **Com registros:** Calcula horas trabalhadas e abona o restante
     *
     * Exemplo: Jornada 8h, trabalhou das 08:00 às 11:00 (3h), passou mal
     * → Sistema abona 5h automaticamente, banco fica neutro.
     */
    ATESTADO(
        descricao = "Atestado",
        emoji = "🏥",
        zeraJornada = false,  // Condicional: verificado em runtime
        requerDocumento = true,
        permiteAnexo = true,
        isPlanejada = false,
        explicacaoImpacto = "Sem registros: jornada zerada. Com registros: abona horas restantes até completar a jornada do dia.",
        exemploUso = "Doença, emergência médica, passou mal no trabalho, falecimento, exames urgentes.",
        labelObservacao = "Motivo do atestado",
        placeholderObservacao = "Ex: Gripe, emergência, falecimento, procedimento médico..."
    ),

    /**
     * Declaração de comparecimento que abona período específico.
     *
     * - **Planejada:** ❌ Não (compromissos pontuais)
     * - **Impacto:** Abona APENAS o tempo especificado, restante deve ser cumprido
     * - **Documentação:** Requer declaração de comparecimento
     *
     * ### Campos obrigatórios:
     * - `horaInicio`: Hora de início do compromisso
     * - `duracaoDeclaracaoMinutos`: Tempo total ausente
     * - `duracaoAbonoMinutos`: Tempo efetivamente abonado (≤ duração)
     *
     * ### Quando usar:
     * - Consulta médica rápida (sem atestado de dia inteiro)
     * - Audiência judicial
     * - Prova de concurso/vestibular
     * - Reunião escolar
     * - Cartório/órgãos públicos
     *
     * ### Diferença para Atestado:
     * - **Atestado:** Abona TODO o tempo não trabalhado do dia
     * - **Declaração:** Abona APENAS o período especificado no documento
     */
    DECLARACAO(
        descricao = "Declaração",
        emoji = "📄",
        zeraJornada = false,
        requerDocumento = true,
        permiteAnexo = true,
        isPlanejada = false,
        explicacaoImpacto = "Abona apenas o tempo especificado. O restante da jornada deve ser cumprido.",
        exemploUso = "Consulta médica, audiência, prova de concurso, reunião escolar, cartório.",
        labelObservacao = "Motivo da declaração",
        placeholderObservacao = "Ex: Consulta dermatologista, audiência, prova ENEM..."
    ),

    /**
     * Falta planejada e permitida pela empresa - totalmente abonada.
     *
     * - **Planejada:** ✅ Sim (acordada previamente)
     * - **Impacto:** Jornada zerada, dia totalmente abonado
     * - **Registro de ponto:** Não necessário
     *
     * ### Faltas justificadas por lei (CLT Art. 473):
     * - Casamento: até 3 dias consecutivos
     * - Nascimento de filho: 1 dia (pai)
     * - Falecimento de cônjuge/pais/filhos/irmãos: até 2 dias
     * - Doação voluntária de sangue: 1 dia por ano
     * - Alistamento eleitoral: até 2 dias
     * - Serviço militar obrigatório
     * - Vestibular: dias de prova
     *
     * ### Benefícios da empresa:
     * - Day-off de aniversário
     * - Folga por meta atingida
     * - Bônus especial
     *
     * ### Diferença para Folga:
     * - **Falta Justificada:** Abonada, NÃO desconta do banco
     * - **Folga:** Desconta do banco de horas
     */
    FALTA_JUSTIFICADA(
        descricao = "Falta Justificada",
        emoji = "📝",
        zeraJornada = true,
        requerDocumento = false,
        permiteAnexo = true,
        isPlanejada = true,
        explicacaoImpacto = "Jornada zerada. Falta planejada e permitida. Não gera débito no banco.",
        exemploUso = "Casamento, day-off aniversário, doação de sangue, nascimento de filho, alistamento.",
        labelObservacao = "Motivo da falta",
        placeholderObservacao = "Ex: Casamento, day-off aniversário, doação de sangue..."
    ),

    /**
     * Folga planejada para compensar/reduzir saldo do banco de horas.
     *
     * - **Planejada:** ✅ Sim (acordada com a empresa)
     * - **Impacto:** Jornada NORMAL, desconta horas do banco
     * - **Uso típico:** Reduzir banco positivo antes do fechamento
     *
     * ### Quando usar:
     * - Compensação de horas extras acumuladas
     * - Redução de saldo antes do fechamento do período
     * - Emenda de feriado usando banco de horas
     *
     * ### Como funciona:
     * A jornada do dia (ex: 8h) é descontada do saldo do banco.
     * - Banco antes: +20h
     * - Folga de um dia (8h de jornada)
     * - Banco depois: +12h
     *
     * ### Diferença para Falta Justificada:
     * - **Folga:** Desconta do banco (reduz saldo)
     * - **Falta Justificada:** Não desconta (abonada pela empresa)
     */
    FOLGA(
        descricao = "Folga",
        emoji = "😴",
        zeraJornada = false,
        requerDocumento = false,
        permiteAnexo = false,
        isPlanejada = true,
        explicacaoImpacto = "⚠️ Jornada normal. Desconta as horas do banco. Use para reduzir saldo positivo.",
        exemploUso = "Compensação de banco de horas, emenda de feriado, redução de saldo antes do fechamento.",
        labelObservacao = "Observação",
        placeholderObservacao = "Ex: Compensação do mês, emenda de feriado..."
    ),

    /**
     * Falta sem justificativa aceita - gera penalidade.
     *
     * - **Planejada:** ❌ Não
     * - **Impacto:** Jornada NORMAL, gera DÉBITO no banco (penalidade)
     * - **Consequência:** Deve ser compensado com horas extras
     *
     * ### Quando usar:
     * - Funcionário faltou sem avisar
     * - Justificativa não foi aceita pela empresa
     * - Abandono de posto
     *
     * ### ⚠️ Consequências:
     * - Gera débito equivalente à jornada do dia
     * - Débito deve ser compensado
     * - Pode haver desconto em folha se não compensado
     *
     * ### Exemplo:
     * - Jornada do dia: 8h
     * - Resultado: -8h no banco de horas
     */
    FALTA_INJUSTIFICADA(
        descricao = "Falta Injustificada",
        emoji = "❌",
        zeraJornada = false,
        requerDocumento = false,
        permiteAnexo = false,
        isPlanejada = false,
        explicacaoImpacto = "⚠️ Jornada normal. Gera débito no banco de horas (penalidade). Deve ser compensado.",
        exemploUso = "Falta sem aviso, justificativa não aceita, abandono.",
        labelObservacao = "Motivo (opcional)",
        placeholderObservacao = "Informe o motivo, se desejar..."
    );

    // ============================================================================
    // PROPRIEDADES CALCULADAS
    // ============================================================================

    /**
     * Preposição para frases de retorno (ex: "retorno das férias", "retorno do atestado").
     */
    val preposicao: String
        get() = when (this) {
            FERIAS -> "das férias"
            ATESTADO -> "do atestado"
            DECLARACAO -> "da declaração"
            FALTA_JUSTIFICADA -> "da falta"
            FOLGA -> "da folga"
            FALTA_INJUSTIFICADA -> "da falta"
        }

    /**
     * Indica se a ausência é considerada justificada.
     *
     * Para [ATESTADO], sempre é justificada, mas o abono é condicional
     * (depende se há registros de ponto no dia).
     */
    val isJustificada: Boolean
        get() = zeraJornada || this == ATESTADO

    /**
     * Indica se a ausência diminui o saldo do banco de horas.
     * - [FOLGA]: Desconta do banco (planejada)
     * - [FALTA_INJUSTIFICADA]: Gera débito (penalidade)
     */
    val descontaDoBanco: Boolean
        get() = this == FOLGA || this == FALTA_INJUSTIFICADA

    /**
     * Resumo visual do impacto no banco de horas.
     */
    val impactoResumido: String
        get() = when (this) {
            FERIAS, FALTA_JUSTIFICADA -> "✅ Abonado"
            ATESTADO -> "✅ Abona horas restantes"
            DECLARACAO -> "⏱️ Abono parcial"
            FOLGA -> "⏰ Desconta do banco"
            FALTA_INJUSTIFICADA -> "❌ Gera débito"
        }

    /**
     * Cor indicativa para UI.
     */
    val corIndicativa: TipoAusenciaCor
        get() = when (this) {
            FERIAS, FALTA_JUSTIFICADA, ATESTADO -> TipoAusenciaCor.VERDE
            DECLARACAO -> TipoAusenciaCor.AZUL
            FOLGA -> TipoAusenciaCor.AMARELO
            FALTA_INJUSTIFICADA -> TipoAusenciaCor.VERMELHO
        }

    /**
     * Indica se usa período de dias (data inicial + final).
     * Todos exceto [DECLARACAO], que usa intervalo de horas em dia único.
     */
    val usaPeriodo: Boolean
        get() = this != DECLARACAO

    /**
     * Indica se usa intervalo de horas (apenas [DECLARACAO]).
     */
    val usaIntervaloHoras: Boolean
        get() = this == DECLARACAO

    /**
     * Indica se bloqueia registro de ponto no período.
     * Apenas [FERIAS] bloqueia completamente.
     */
    val bloqueiaRegistroPonto: Boolean
        get() = this == FERIAS

    /**
     * Indica se o abono é condicional (depende de registros existentes).
     * Apenas [ATESTADO] tem esse comportamento.
     */
    val abonoCondicional: Boolean
        get() = this == ATESTADO

    /**
     * Converte para [TipoDiaEspecial] usado no cálculo de jornada.
     *
     * @param tipoFolga Subtipo de folga (obrigatório quando tipo == FOLGA)
     */
    fun toTipoDiaEspecial(tipoFolga: TipoFolga? = null): TipoDiaEspecial = when (this) {
        FERIAS -> TipoDiaEspecial.FERIAS
        ATESTADO -> TipoDiaEspecial.ATESTADO
        DECLARACAO -> TipoDiaEspecial.NORMAL
        FALTA_JUSTIFICADA -> TipoDiaEspecial.FALTA_JUSTIFICADA
        FOLGA -> when (tipoFolga) {
            TipoFolga.DAY_OFF -> TipoDiaEspecial.FALTA_JUSTIFICADA // Zera jornada
            TipoFolga.COMPENSACAO, null -> TipoDiaEspecial.FOLGA   // Mantém jornada
        }

        FALTA_INJUSTIFICADA -> TipoDiaEspecial.FALTA_INJUSTIFICADA
    }

    companion object {
        /**
         * Tipos que sempre abonam o dia inteiro (jornada zerada).
         */
        fun tiposAbonados(): List<TipoAusencia> = entries.filter { it.zeraJornada }

        /**
         * Tipos que descontam do banco de horas.
         */
        fun tiposQueDescontam(): List<TipoAusencia> = entries.filter { it.descontaDoBanco }

        /**
         * Tipos planejados (acordados previamente).
         */
        fun tiposPlanejados(): List<TipoAusencia> = entries.filter { it.isPlanejada }

        /**
         * Tipos não planejados (imprevistos).
         */
        fun tiposImprevistos(): List<TipoAusencia> = entries.filter { !it.isPlanejada }

        /**
         * Tipos que requerem/recomendam documento.
         */
        fun tiposComDocumento(): List<TipoAusencia> = entries.filter { it.requerDocumento }

        /**
         * Tipos que permitem anexar imagem.
         */
        fun tiposComAnexo(): List<TipoAusencia> = entries.filter { it.permiteAnexo }

        /**
         * Converte de [TipoDiaEspecial] para [TipoAusencia].
         */
        fun fromTipoDiaEspecial(tipo: TipoDiaEspecial): TipoAusencia? = when (tipo) {
            TipoDiaEspecial.FERIAS -> FERIAS
            TipoDiaEspecial.ATESTADO -> ATESTADO
            TipoDiaEspecial.FALTA_JUSTIFICADA -> FALTA_JUSTIFICADA
            TipoDiaEspecial.FOLGA -> FOLGA
            TipoDiaEspecial.FALTA_INJUSTIFICADA -> FALTA_INJUSTIFICADA
            else -> null
        }
    }
}

/**
 * Cores indicativas para UI de ausências.
 */
enum class TipoAusenciaCor {
    /** Situação positiva - abonado */
    VERDE,

    /** Situação neutra - abono parcial */
    AZUL,

    /** Situação de atenção - desconta do banco */
    AMARELO,

    /** Situação negativa - gera débito/penalidade */
    VERMELHO
}
