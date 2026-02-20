// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/ausencia/TipoAusencia.kt
package br.com.tlmacedo.meuponto.domain.model.ausencia

import br.com.tlmacedo.meuponto.domain.model.TipoDiaEspecial

/**
 * Tipos de ausência disponíveis no sistema.
 *
 * Define os diferentes tipos de ausência que um colaborador pode registrar,
 * cada um com comportamento específico em relação ao cálculo de jornada.
 *
 * REGRAS DE CÁLCULO:
 * - Jornada ZERADA (trabalho = hora extra): FERIAS, ATESTADO, DECLARACAO, FALTA_JUSTIFICADA
 * - Jornada NORMAL (gera débito): FOLGA, FALTA_INJUSTIFICADA
 *
 * @property descricao Nome amigável para exibição
 * @property emoji Ícone representativo
 * @property zeraJornada Se true, trabalho neste dia vira hora extra
 * @property requerDocumento Se true, pode exigir anexo de comprovante
 *
 * @author Thiago
 * @since 4.0.0
 */
enum class TipoAusencia(
    val descricao: String,
    val emoji: String,
    val zeraJornada: Boolean,
    val requerDocumento: Boolean = false
) {
    /**
     * Férias - período de descanso remunerado.
     * Jornada zerada. Trabalho neste dia conta como hora extra.
     */
    FERIAS(
        descricao = "Férias",
        emoji = "🏖️",
        zeraJornada = true,
        requerDocumento = false
    ),

    /**
     * Atestado médico - afastamento por motivo de saúde.
     * Jornada zerada. Trabalho neste dia conta como hora extra.
     */
    ATESTADO(
        descricao = "Atestado",
        emoji = "🏥",
        zeraJornada = true,
        requerDocumento = true
    ),

    /**
     * Declaração de comparecimento - ausência parcial ou total justificada.
     * Ex: consulta médica, audiência, prova de concurso.
     * Jornada zerada. Trabalho neste dia conta como hora extra.
     */
    DECLARACAO(
        descricao = "Declaração",
        emoji = "📄",
        zeraJornada = true,
        requerDocumento = true
    ),

    /**
     * Falta justificada - ausência com justificativa aceita.
     * Ex: falecimento de familiar, casamento, doação de sangue.
     * Jornada zerada. Trabalho neste dia conta como hora extra.
     */
    FALTA_JUSTIFICADA(
        descricao = "Falta Justificada",
        emoji = "📝",
        zeraJornada = true,
        requerDocumento = false
    ),

    /**
     * Folga - dia de descanso compensatório (banco de horas).
     * Jornada NORMAL. Não trabalhar gera débito no banco.
     */
    FOLGA(
        descricao = "Folga",
        emoji = "😴",
        zeraJornada = false,
        requerDocumento = false
    ),

    /**
     * Falta injustificada - ausência sem justificativa.
     * Jornada NORMAL. Não trabalhar gera débito no banco.
     */
    FALTA_INJUSTIFICADA(
        descricao = "Falta Injustificada",
        emoji = "❌",
        zeraJornada = false,
        requerDocumento = false
    );

    /**
     * Verifica se é ausência justificada/abonada.
     */
    val isJustificada: Boolean
        get() = zeraJornada

    /**
     * Converte para TipoDiaEspecial correspondente.
     */
    fun toTipoDiaEspecial(): TipoDiaEspecial = when (this) {
        FERIAS -> TipoDiaEspecial.FERIAS
        ATESTADO -> TipoDiaEspecial.ATESTADO
        DECLARACAO -> TipoDiaEspecial.ATESTADO // Usa ATESTADO como base (mesmo comportamento)
        FALTA_JUSTIFICADA -> TipoDiaEspecial.FALTA_JUSTIFICADA
        FOLGA -> TipoDiaEspecial.FOLGA
        FALTA_INJUSTIFICADA -> TipoDiaEspecial.FALTA_INJUSTIFICADA
    }

    companion object {
        /**
         * Retorna tipos que zeram a jornada (abonados).
         */
        fun tiposAbonados(): List<TipoAusencia> = entries.filter { it.zeraJornada }

        /**
         * Retorna tipos que mantêm a jornada normal (geram débito).
         */
        fun tiposNaoAbonados(): List<TipoAusencia> = entries.filter { !it.zeraJornada }

        /**
         * Retorna tipos que podem requerer documento.
         */
        fun tiposComDocumento(): List<TipoAusencia> = entries.filter { it.requerDocumento }

        /**
         * Converte de TipoDiaEspecial para TipoAusencia, se aplicável.
         */
        fun fromTipoDiaEspecial(tipo: TipoDiaEspecial): TipoAusencia? = when (tipo) {
            TipoDiaEspecial.FERIAS -> FERIAS
            TipoDiaEspecial.ATESTADO -> ATESTADO
            TipoDiaEspecial.FALTA_JUSTIFICADA -> FALTA_JUSTIFICADA
            TipoDiaEspecial.FOLGA -> FOLGA
            TipoDiaEspecial.FALTA_INJUSTIFICADA -> FALTA_INJUSTIFICADA
            else -> null // NORMAL, FERIADO, PONTE, FACULTATIVO não são ausências
        }
    }
}
