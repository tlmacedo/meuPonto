// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/ausencia/TipoFolga.kt
package br.com.tlmacedo.meuponto.domain.model.ausencia

/**
 * Subtipo de folga que define o impacto no banco de horas.
 *
 * @author Thiago
 * @since 6.0.0
 */
enum class TipoFolga(
    val descricao: String,
    val descricaoCurta: String,
    val emoji: String,
    val zeraJornada: Boolean,
    val explicacao: String
) {
    /**
     * Folga concedida pela empresa (day-off).
     * Não desconta do banco de horas.
     *
     * Exemplos:
     * - Day-off por meta atingida
     * - Folga concedida por bom desempenho
     * - Compensação oferecida pela empresa
     */
    DAY_OFF(
        descricao = "Day-off",
        descricaoCurta = "Day-off",
        emoji = "🎁",
        zeraJornada = true,
        explicacao = "Folga concedida pela empresa. Não desconta do banco de horas."
    ),

    /**
     * Folga para compensação de banco de horas.
     * Desconta do saldo positivo do banco.
     *
     * Exemplos:
     * - Compensação de horas extras acumuladas
     * - Redução de saldo antes do fechamento
     * - Emenda de feriado usando banco
     */
    COMPENSACAO(
        descricao = "Compensação de banco",
        descricaoCurta = "Compensação",
        emoji = "⏰",
        zeraJornada = false,
        explicacao = "Desconta as horas do banco. Use para reduzir saldo positivo."
    );

    companion object {
        /**
         * Valor padrão para folgas existentes (migração).
         * Assume compensação para manter comportamento anterior.
         */
        val PADRAO = COMPENSACAO
    }
}
