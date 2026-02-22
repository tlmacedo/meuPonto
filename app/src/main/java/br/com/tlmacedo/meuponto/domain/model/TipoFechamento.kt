// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/TipoFechamento.kt
package br.com.tlmacedo.meuponto.domain.model

/**
 * Tipo de fechamento de período para banco de horas.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 3.0.0 - Adicionado CICLO_BANCO_AUTOMATICO
 */
enum class TipoFechamento(val descricao: String, val automatico: Boolean) {
    /** Fechamento semanal */
    SEMANAL("Fechamento Semanal", automatico = false),

    /** Fechamento mensal (folha de pagamento/RH) */
    MENSAL("Fechamento Mensal (RH)", automatico = false),

    /** Fechamento do período de banco de horas (manual) */
    BANCO_HORAS("Fechamento Banco de Horas", automatico = false),

    /** Fechamento automático de ciclo do banco de horas */
    CICLO_BANCO_AUTOMATICO("Fim de Ciclo (Automático)", automatico = true);

    companion object {
        fun fromString(value: String): TipoFechamento =
            entries.find { it.name == value } ?: BANCO_HORAS
    }
}
