// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/ausencia/Ausencia.kt
package br.com.tlmacedo.meuponto.domain.model.ausencia

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Representa uma ausência registrada no sistema.
 *
 * Ausências podem ser:
 * - De um único dia ou um período (dataInicio até dataFim)
 * - De diferentes tipos com comportamentos distintos no cálculo
 * - Associadas a um emprego específico
 *
 * @property id Identificador único da ausência
 * @property empregoId ID do emprego associado
 * @property tipo Tipo da ausência (férias, atestado, folga, etc.)
 * @property dataInicio Data de início da ausência
 * @property dataFim Data de fim da ausência (igual a dataInicio se for um único dia)
 * @property descricao Descrição ou motivo da ausência
 * @property observacao Observação adicional
 * @property ativo Se a ausência está ativa (não foi cancelada)
 * @property criadoEm Data/hora de criação
 * @property atualizadoEm Data/hora da última atualização
 *
 * @author Thiago
 * @since 4.0.0
 */
data class Ausencia(
    val id: Long = 0,
    val empregoId: Long,
    val tipo: TipoAusencia,
    val dataInicio: LocalDate,
    val dataFim: LocalDate = dataInicio,
    val descricao: String? = null,
    val observacao: String? = null,
    val ativo: Boolean = true,
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(dataFim >= dataInicio) {
            "Data fim ($dataFim) deve ser maior ou igual à data início ($dataInicio)"
        }
    }

    // ========================================================================
    // PROPRIEDADES CALCULADAS
    // ========================================================================

    /**
     * Verifica se a ausência é de apenas um dia.
     */
    val isDiaUnico: Boolean
        get() = dataInicio == dataFim

    /**
     * Verifica se a ausência abrange um período (mais de um dia).
     */
    val isPeriodo: Boolean
        get() = dataInicio != dataFim

    /**
     * Quantidade de dias da ausência (inclusive).
     */
    val quantidadeDias: Int
        get() = ChronoUnit.DAYS.between(dataInicio, dataFim).toInt() + 1

    /**
     * Verifica se a ausência zera a jornada (é abonada).
     */
    val zeraJornada: Boolean
        get() = tipo.zeraJornada

    /**
     * Verifica se é ausência justificada.
     */
    val isJustificada: Boolean
        get() = tipo.isJustificada

    /**
     * Verifica se pode requerer documento comprobatório.
     */
    val requerDocumento: Boolean
        get() = tipo.requerDocumento

    /**
     * Emoji representativo do tipo.
     */
    val emoji: String
        get() = tipo.emoji

    /**
     * Descrição do tipo.
     */
    val tipoDescricao: String
        get() = tipo.descricao

    // ========================================================================
    // VERIFICAÇÕES DE DATA
    // ========================================================================

    /**
     * Verifica se uma data específica está dentro do período da ausência.
     *
     * @param data Data a verificar
     * @return true se a data está dentro do período
     */
    fun contemData(data: LocalDate): Boolean {
        return data in dataInicio..dataFim
    }

    /**
     * Verifica se a ausência ocorre em uma data específica.
     * Alias para contemData() para manter consistência com Feriado.
     */
    fun ocorreEm(data: LocalDate): Boolean = contemData(data)

    /**
     * Verifica se a ausência está em andamento (inclui hoje).
     */
    fun emAndamento(hoje: LocalDate = LocalDate.now()): Boolean {
        return hoje in dataInicio..dataFim
    }

    /**
     * Verifica se a ausência já passou.
     */
    fun jaPassou(hoje: LocalDate = LocalDate.now()): Boolean {
        return dataFim < hoje
    }

    /**
     * Verifica se a ausência é futura.
     */
    fun isFutura(hoje: LocalDate = LocalDate.now()): Boolean {
        return dataInicio > hoje
    }

    /**
     * Verifica se há sobreposição com outra ausência.
     *
     * @param outra Outra ausência para verificar
     * @return true se há sobreposição de datas
     */
    fun sobrepoe(outra: Ausencia): Boolean {
        return dataInicio <= outra.dataFim && dataFim >= outra.dataInicio
    }

    /**
     * Verifica se há sobreposição com um período.
     *
     * @param inicio Início do período
     * @param fim Fim do período
     * @return true se há sobreposição
     */
    fun sobrepoeComPeriodo(inicio: LocalDate, fim: LocalDate): Boolean {
        return dataInicio <= fim && dataFim >= inicio
    }

    // ========================================================================
    // FORMATADORES
    // ========================================================================

    /**
     * Retorna período formatado para exibição.
     * Ex: "20/02/2026" ou "20/02/2026 - 25/02/2026"
     */
    fun formatarPeriodo(): String {
        return if (isDiaUnico) {
            formatarData(dataInicio)
        } else {
            "${formatarData(dataInicio)} - ${formatarData(dataFim)}"
        }
    }

    /**
     * Retorna descrição curta para exibição em lista.
     * Ex: "Férias (5 dias)" ou "Atestado"
     */
    fun descricaoCurta(): String {
        return if (isPeriodo) {
            "${tipo.descricao} ($quantidadeDias dias)"
        } else {
            tipo.descricao
        }
    }

    private fun formatarData(data: LocalDate): String {
        return "${data.dayOfMonth.toString().padStart(2, '0')}/" +
                "${data.monthValue.toString().padStart(2, '0')}/" +
                "${data.year}"
    }

    // ========================================================================
    // COMPANION OBJECT - FACTORY METHODS
    // ========================================================================

    companion object {
        /**
         * Cria uma ausência de férias.
         */
        fun criarFerias(
            empregoId: Long,
            dataInicio: LocalDate,
            dataFim: LocalDate,
            observacao: String? = null
        ): Ausencia = Ausencia(
            empregoId = empregoId,
            tipo = TipoAusencia.FERIAS,
            dataInicio = dataInicio,
            dataFim = dataFim,
            descricao = "Férias",
            observacao = observacao
        )

        /**
         * Cria uma ausência de atestado médico.
         */
        fun criarAtestado(
            empregoId: Long,
            dataInicio: LocalDate,
            dataFim: LocalDate = dataInicio,
            descricao: String? = null,
            observacao: String? = null
        ): Ausencia = Ausencia(
            empregoId = empregoId,
            tipo = TipoAusencia.ATESTADO,
            dataInicio = dataInicio,
            dataFim = dataFim,
            descricao = descricao ?: "Atestado médico",
            observacao = observacao
        )

        /**
         * Cria uma ausência de declaração de comparecimento.
         */
        fun criarDeclaracao(
            empregoId: Long,
            data: LocalDate,
            descricao: String,
            observacao: String? = null
        ): Ausencia = Ausencia(
            empregoId = empregoId,
            tipo = TipoAusencia.DECLARACAO,
            dataInicio = data,
            dataFim = data,
            descricao = descricao,
            observacao = observacao
        )

        /**
         * Cria uma folga (compensação de banco de horas).
         */
        fun criarFolga(
            empregoId: Long,
            data: LocalDate,
            observacao: String? = null
        ): Ausencia = Ausencia(
            empregoId = empregoId,
            tipo = TipoAusencia.FOLGA,
            dataInicio = data,
            dataFim = data,
            descricao = "Folga",
            observacao = observacao
        )

        /**
         * Cria uma falta justificada.
         */
        fun criarFaltaJustificada(
            empregoId: Long,
            data: LocalDate,
            descricao: String,
            observacao: String? = null
        ): Ausencia = Ausencia(
            empregoId = empregoId,
            tipo = TipoAusencia.FALTA_JUSTIFICADA,
            dataInicio = data,
            dataFim = data,
            descricao = descricao,
            observacao = observacao
        )

        /**
         * Cria uma falta injustificada.
         */
        fun criarFaltaInjustificada(
            empregoId: Long,
            data: LocalDate,
            observacao: String? = null
        ): Ausencia = Ausencia(
            empregoId = empregoId,
            tipo = TipoAusencia.FALTA_INJUSTIFICADA,
            dataInicio = data,
            dataFim = data,
            descricao = "Falta injustificada",
            observacao = observacao
        )
    }
}
