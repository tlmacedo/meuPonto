// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/TipoAjusteSaldo.kt
package br.com.tlmacedo.meuponto.domain.model

/**
 * Enum que representa os tipos de ajuste de saldo no banco de horas.
 *
 * @property descricao Descrição legível do tipo
 * @property emoji Emoji representativo para UI
 *
 * @author Thiago
 * @since 11.0.0
 */
enum class TipoAjusteSaldo(
    val descricao: String,
    val emoji: String
) {
    /** Ajuste manual feito pelo usuário */
    MANUAL("Ajuste Manual", "✏️"),

    /** Correção de erro em registro anterior */
    CORRECAO("Correção", "🔧"),

    /** Migração de dados de outro sistema/app */
    MIGRACAO("Migração", "📦"),

    /** Saldo inicial ao cadastrar emprego */
    SALDO_INICIAL("Saldo Inicial", "🏁"),

    /** Compensação de banco de horas */
    COMPENSACAO("Compensação", "⚖️"),

    /** Abono/bonificação */
    ABONO("Abono", "🎁"),

    /** Desconto/penalidade */
    DESCONTO("Desconto", "➖"),

    /** Outros ajustes não categorizados */
    OUTRO("Outro", "📝");

    companion object {
        /**
         * Retorna o tipo pelo nome, com fallback para MANUAL.
         */
        fun fromName(name: String?): TipoAjusteSaldo =
            entries.find { it.name == name } ?: MANUAL

        /**
         * Tipos disponíveis para seleção manual pelo usuário.
         */
        val disponiveisParaUsuario: List<TipoAjusteSaldo>
            get() = listOf(MANUAL, CORRECAO, COMPENSACAO, ABONO, DESCONTO, OUTRO)
    }
}
