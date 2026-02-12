// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

import br.com.tlmacedo.meuponto.domain.model.BancoHoras
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import java.time.LocalDate
import java.time.LocalTime

/**
 * Estado da tela Home.
 *
 * Contém todos os dados necessários para renderização da tela principal,
 * incluindo pontos do dia, resumo, banco de horas e estados de UI.
 *
 * @property dataAtual Data selecionada
 * @property horaAtual Hora atual (atualiza a cada segundo)
 * @property pontosHoje Lista de pontos do dia
 * @property resumoDia Resumo calculado do dia
 * @property bancoHoras Banco de horas acumulado
 * @property proximoTipo Próximo tipo de ponto esperado
 * @property isLoading Indica se está carregando dados
 * @property showTimePickerDialog Controla exibição do dialog de horário
 * @property showDeleteConfirmDialog Controla exibição do dialog de exclusão
 * @property pontoParaExcluir Ponto selecionado para exclusão
 *
 * @author Thiago
 * @since 1.0.0
 */
data class HomeUiState(
    val dataAtual: LocalDate = LocalDate.now(),
    val horaAtual: LocalTime = LocalTime.now(),
    val pontosHoje: List<Ponto> = emptyList(),
    val resumoDia: ResumoDia = ResumoDia(data = LocalDate.now()),
    val bancoHoras: BancoHoras = BancoHoras(),
    val proximoTipo: TipoPonto = TipoPonto.ENTRADA,
    val isLoading: Boolean = false,
    val showTimePickerDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val pontoParaExcluir: Ponto? = null
) {
    /**
     * Formata a data atual para exibição.
     */
    val dataFormatada: String
        get() {
            val hoje = LocalDate.now()
            return when (dataAtual) {
                hoje -> "Hoje"
                hoje.minusDays(1) -> "Ontem"
                else -> {
                    val formatter = java.time.format.DateTimeFormatter
                        .ofPattern("EEEE, dd 'de' MMMM", java.util.Locale("pt", "BR"))
                    dataAtual.format(formatter).replaceFirstChar { it.uppercase() }
                }
            }
        }

    /**
     * Verifica se há pontos registrados no dia.
     */
    val temPontos: Boolean
        get() = pontosHoje.isNotEmpty()
}
