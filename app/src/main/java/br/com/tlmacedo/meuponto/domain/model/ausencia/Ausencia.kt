// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/ausencia/Ausencia.kt
package br.com.tlmacedo.meuponto.domain.model.ausencia

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Modelo de domínio para Ausência.
 *
 * @author Thiago
 * @since 4.0.0
 * @updated 5.5.0 - Removido SubTipoFolga, adicionadas propriedades auxiliares
 */
data class Ausencia(
    val id: Long = 0,
    val empregoId: Long,
    val tipo: TipoAusencia,
    val tipoFolga: TipoFolga? = null,
    val dataInicio: LocalDate,
    val dataFim: LocalDate = dataInicio,
    val descricao: String? = null,
    val observacao: String? = null,

    // Campos específicos para DECLARACAO
    val horaInicio: LocalTime? = null,
    val duracaoDeclaracaoMinutos: Int? = null,
    val duracaoAbonoMinutos: Int? = null,

    // Campo para FERIAS
    val periodoAquisitivo: String? = null,

    // Anexo de imagem
    val imagemUri: String? = null,

    // Campos de controle
    val ativo: Boolean = true,
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        private val localeBR = Locale("pt", "BR")
        private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", localeBR)
        private val dateFormatterShort = DateTimeFormatter.ofPattern("dd/MM", localeBR)
        private val dateFormatterFull = DateTimeFormatter.ofPattern("dd 'de' MMMM", localeBR)

        /**
         * Cria ausência de férias.
         */
        fun criarFerias(
            empregoId: Long,
            dataInicio: LocalDate,
            dataFim: LocalDate,
            periodoAquisitivo: String
        ): Ausencia = Ausencia(
            empregoId = empregoId,
            tipo = TipoAusencia.FERIAS,
            dataInicio = dataInicio,
            dataFim = dataFim,
            descricao = TipoAusencia.FERIAS.descricao,
            periodoAquisitivo = periodoAquisitivo
        )

        /**
         * Cria ausência de atestado.
         */
        fun criarAtestado(
            empregoId: Long,
            dataInicio: LocalDate,
            dataFim: LocalDate = dataInicio,
            motivo: String,
            imagemUri: String? = null
        ): Ausencia = Ausencia(
            empregoId = empregoId,
            tipo = TipoAusencia.ATESTADO,
            dataInicio = dataInicio,
            dataFim = dataFim,
            descricao = TipoAusencia.ATESTADO.descricao,
            observacao = motivo,
            imagemUri = imagemUri
        )

        /**
         * Cria ausência de declaração.
         */
        fun criarDeclaracao(
            empregoId: Long,
            data: LocalDate,
            horaInicio: LocalTime,
            duracaoDeclaracaoMinutos: Int,
            duracaoAbonoMinutos: Int,
            motivo: String,
            imagemUri: String? = null
        ): Ausencia = Ausencia(
            empregoId = empregoId,
            tipo = TipoAusencia.DECLARACAO,
            dataInicio = data,
            dataFim = data,
            descricao = TipoAusencia.DECLARACAO.descricao,
            observacao = motivo,
            horaInicio = horaInicio,
            duracaoDeclaracaoMinutos = duracaoDeclaracaoMinutos,
            duracaoAbonoMinutos = duracaoAbonoMinutos,
            imagemUri = imagemUri
        )

        /**
         * Cria falta justificada.
         */
        fun criarFaltaJustificada(
            empregoId: Long,
            dataInicio: LocalDate,
            dataFim: LocalDate = dataInicio,
            motivo: String,
            imagemUri: String? = null
        ): Ausencia = Ausencia(
            empregoId = empregoId,
            tipo = TipoAusencia.FALTA_JUSTIFICADA,
            dataInicio = dataInicio,
            dataFim = dataFim,
            descricao = TipoAusencia.FALTA_JUSTIFICADA.descricao,
            observacao = motivo,
            imagemUri = imagemUri
        )

        /**
         * Cria folga (desconta do banco de horas).
         */
        fun criarFolga(
            empregoId: Long,
            dataInicio: LocalDate,
            dataFim: LocalDate = dataInicio,
            observacao: String? = null
        ): Ausencia = Ausencia(
            empregoId = empregoId,
            tipo = TipoAusencia.FOLGA,
            dataInicio = dataInicio,
            dataFim = dataFim,
            descricao = TipoAusencia.FOLGA.descricao,
            observacao = observacao
        )

        /**
         * Cria falta injustificada.
         */
        fun criarFaltaInjustificada(
            empregoId: Long,
            dataInicio: LocalDate,
            dataFim: LocalDate = dataInicio,
            observacao: String? = null
        ): Ausencia = Ausencia(
            empregoId = empregoId,
            tipo = TipoAusencia.FALTA_INJUSTIFICADA,
            dataInicio = dataInicio,
            dataFim = dataFim,
            descricao = TipoAusencia.FALTA_INJUSTIFICADA.descricao,
            observacao = observacao
        )

        /**
         * Cria folga Day-off (não desconta do banco).
         */
        fun criarFolgaDayOff(
            empregoId: Long,
            dataInicio: LocalDate,
            dataFim: LocalDate = dataInicio,
            observacao: String? = null
        ): Ausencia = Ausencia(
            empregoId = empregoId,
            tipo = TipoAusencia.FOLGA,
            tipoFolga = TipoFolga.DAY_OFF,
            dataInicio = dataInicio,
            dataFim = dataFim,
            descricao = "Folga (Day-off)",
            observacao = observacao
        )

        /**
         * Cria folga para compensação de banco de horas.
         */
        fun criarFolgaCompensacao(
            empregoId: Long,
            dataInicio: LocalDate,
            dataFim: LocalDate = dataInicio,
            observacao: String? = null
        ): Ausencia = Ausencia(
            empregoId = empregoId,
            tipo = TipoAusencia.FOLGA,
            tipoFolga = TipoFolga.COMPENSACAO,
            dataInicio = dataInicio,
            dataFim = dataFim,
            descricao = "Folga (Compensação)",
            observacao = observacao
        )

        /**
         * Cria folga (mantido para compatibilidade, usa COMPENSACAO por padrão).
         */
        fun criarFolga(
            empregoId: Long,
            dataInicio: LocalDate,
            dataFim: LocalDate = dataInicio,
            observacao: String? = null,
            tipoFolga: TipoFolga = TipoFolga.COMPENSACAO
        ): Ausencia = Ausencia(
            empregoId = empregoId,
            tipo = TipoAusencia.FOLGA,
            tipoFolga = tipoFolga,
            dataInicio = dataInicio,
            dataFim = dataFim,
            descricao = if (tipoFolga == TipoFolga.DAY_OFF) "Folga (Day-off)" else "Folga (Compensação)",
            observacao = observacao
        )

    }

    // ========================================================================
    // PROPRIEDADES CALCULADAS
    // ========================================================================

    /**
     * Descrição completa do tipo de ausência (inclui subtipo para folga).
     */
    val tipoDescricaoCompleta: String
        get() = when {
            tipo == TipoAusencia.FOLGA && tipoFolga != null ->
                "Folga (${tipoFolga.descricaoCurta})"
            else -> tipo.descricao
        }

    /**
     * Verifica se esta ausência zera a jornada.
     * Para FOLGA, depende do tipoFolga.
     */
    val zeraJornadaEfetiva: Boolean
        get() = when {
            tipo == TipoAusencia.FOLGA -> tipoFolga?.zeraJornada ?: false
            else -> tipo.zeraJornada
        }

    /**
     * Verifica se esta ausência desconta do banco.
     */
    val descontaDoBancoEfetivo: Boolean
        get() = when {
            tipo == TipoAusencia.FOLGA -> tipoFolga == TipoFolga.COMPENSACAO
            else -> tipo.descontaDoBanco
        }

    /**
     * Quantidade de dias da ausência.
     */
    val quantidadeDias: Int
        get() = ChronoUnit.DAYS.between(dataInicio, dataFim).toInt() + 1

    /**
     * Hora de fim da declaração (calculada).
     */
    val horaFimDeclaracao: LocalTime?
        get() = horaInicio?.plusMinutes(duracaoDeclaracaoMinutos?.toLong() ?: 0)

    /**
     * Verifica se é uma ausência de dia único.
     */
    val isDiaUnico: Boolean
        get() = dataInicio == dataFim

    /**
     * Verifica se é um período (mais de um dia).
     */
    val isPeriodo: Boolean
        get() = dataInicio != dataFim

    /**
     * Emoji do tipo de ausência.
     */
    val emoji: String
        get() = tipo.emoji

    /**
     * Descrição do tipo de ausência.
     */
    val tipoDescricao: String
        get() = tipo.descricao

    /**
     * Indica se a ausência é justificada.
     */
    val isJustificada: Boolean
        get() = tipo.isJustificada

    // ========================================================================
    // FORMATAÇÕES
    // ========================================================================

    /**
     * Formata o período da ausência.
     * - Dia único: "21/02/2026"
     * - Período: "21/02 - 25/02/2026"
     */
    fun formatarPeriodo(): String {
        return if (isDiaUnico) {
            dataInicio.format(dateFormatter)
        } else {
            "${dataInicio.format(dateFormatterShort)} - ${dataFim.format(dateFormatter)}"
        }
    }

    /**
     * Formata o período de forma completa.
     * - Dia único: "21 de fevereiro"
     * - Período: "21 de fevereiro - 25 de fevereiro"
     */
    fun formatarPeriodoCompleto(): String {
        return if (isDiaUnico) {
            dataInicio.format(dateFormatterFull)
        } else {
            "${dataInicio.format(dateFormatterFull)} - ${dataFim.format(dateFormatterFull)}"
        }
    }

    /**
     * Formata a duração em texto legível.
     */
    fun formatarDuracao(): String {
        return when {
            quantidadeDias == 1 -> "1 dia"
            else -> "$quantidadeDias dias"
        }
    }

    // ========================================================================
    // MÉTODOS
    // ========================================================================

    /**
     * Verifica se a ausência está ativa em uma data específica.
     */
    fun isAtivaNaData(data: LocalDate): Boolean {
        return ativo && !data.isBefore(dataInicio) && !data.isAfter(dataFim)
    }

    /**
     * Verifica se a ausência contém uma data específica.
     */
    fun contemData(data: LocalDate): Boolean {
        return !data.isBefore(dataInicio) && !data.isAfter(dataFim)
    }

    /**
     * Verifica se há sobreposição com outro período.
     */
    fun sobrepoeComPeriodo(inicio: LocalDate, fim: LocalDate): Boolean {
        return !dataFim.isBefore(inicio) && !dataInicio.isAfter(fim)
    }
}
