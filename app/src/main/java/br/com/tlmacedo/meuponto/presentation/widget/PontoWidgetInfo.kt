// path: app/src/main/java/br/com/tlmacedo/meuponto/presentation/widget/PontoWidgetInfo.kt
package br.com.tlmacedo.meuponto.presentation.widget

import kotlinx.serialization.Serializable

/**
 * Estado rico do widget, com todos os campos necessários para os 5 layouts.
 * Todos os campos são strings prontas para exibição.
 */
@Serializable
data class PontoWidgetInfo(
    // Identificação do emprego
    val apelidoEmprego: String = "Meu emprego",

    // Horas do dia
    val horasTrabalhadasHoje: String = "00h 00min",
    val saldoDia: String = "+00h 00min",
    val saldoDiaNegativo: Boolean = false,

    // Saldo total (banco de horas)
    val saldoTotal: String = "+00h 00min",
    val saldoTotalNegativo: Boolean = false,

    // Saldo semanal e mensal (para widget 4x2)
    val saldoSemana: String = "+00h 00min",
    val saldoSemanaNegativo: Boolean = false,
    val saldoMes: String = "+00h 00min",
    val saldoMesNegativo: Boolean = false,

    // Últimos registros de ponto do dia (para widget "Últimos registros" e 4x2)
    val registro1: String = "--:--",
    val registro2: String = "--:--",
    val registro3: String = "--:--",
    val registro4: String = "--:--",
    val totalRegistrosHoje: Int = 0,

    // Previsão de saída
    val previsaoSaida: String = "--:--",

    // Última atualização
    val ultimaAtualizacao: String = "--/--/---- --:--",

    // Erro (se houver)
    val erro: String? = null
)