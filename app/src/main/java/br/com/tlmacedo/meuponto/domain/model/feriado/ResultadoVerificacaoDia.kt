// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/feriado/ResultadoVerificacaoDia.kt
package br.com.tlmacedo.meuponto.domain.model.feriado

import java.time.LocalDate

/**
 * Resultado da verificação de um dia específico.
 *
 * Contém informações sobre feriados e eventos que afetam o dia.
 *
 * @property data Data verificada
 * @property feriado Feriado encontrado (se houver)
 * @property isPonte Se é um dia de ponte
 * @property isFimDeSemana Se é fim de semana
 * @property isDiaUtil Se é dia útil de trabalho
 * @property permiteRegistroPonto Se permite registro de ponto
 * @property cargaHorariaEsperadaMinutos Carga horária esperada para o dia (0 se folga)
 *
 * @author Thiago
 * @since 3.0.0
 */
data class ResultadoVerificacaoDia(
    val data: LocalDate,
    val feriado: Feriado? = null,
    val isPonte: Boolean = false,
    val isFimDeSemana: Boolean = false,
    val isDiaUtil: Boolean = true,
    val permiteRegistroPonto: Boolean = true,
    val cargaHorariaEsperadaMinutos: Int = 0,
    val mensagem: String? = null
) {
    /**
     * Verifica se o dia é um feriado (qualquer tipo).
     */
    val isFeriado: Boolean
        get() = feriado != null

    /**
     * Verifica se o dia é folga (feriado ou fim de semana).
     */
    val isFolga: Boolean
        get() = isFeriado || isFimDeSemana || isPonte

    /**
     * Retorna o emoji apropriado para o dia.
     */
    val emoji: String
        get() = when {
            feriado != null -> feriado.tipo.emoji
            isFimDeSemana -> "🛋️"
            else -> "📅"
        }

    /**
     * Retorna descrição do tipo de dia.
     */
    val descricaoTipoDia: String
        get() = when {
            feriado != null -> feriado.tipo.descricao
            isFimDeSemana -> "Fim de Semana"
            else -> "Dia Útil"
        }
}
