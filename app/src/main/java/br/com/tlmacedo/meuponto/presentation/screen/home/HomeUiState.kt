// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

import br.com.tlmacedo.meuponto.domain.model.BancoHoras
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Estado da tela Home.
 *
 * Contém todos os dados necessários para renderização da tela principal,
 * incluindo pontos do dia, resumo, banco de horas, empregos e estados de UI.
 *
 * @property dataSelecionada Data atualmente selecionada para visualização
 * @property horaAtual Hora atual (atualiza a cada segundo)
 * @property pontosHoje Lista de pontos do dia selecionado
 * @property resumoDia Resumo calculado do dia
 * @property bancoHoras Banco de horas acumulado do emprego ativo
 * @property proximoTipo Próximo tipo de ponto esperado
 * @property empregoAtivo Emprego atualmente selecionado
 * @property empregosDisponiveis Lista de empregos disponíveis para seleção
 * @property isLoading Indica se está carregando dados
 * @property isLoadingEmpregos Indica se está carregando lista de empregos
 * @property showTimePickerDialog Controla exibição do dialog de horário
 * @property showDeleteConfirmDialog Controla exibição do dialog de exclusão
 * @property showEmpregoSelector Controla exibição do seletor de emprego
 * @property pontoParaExcluir Ponto selecionado para exclusão
 * @property erro Mensagem de erro atual (se houver)
 *
 * @author Thiago
 * @since 2.0.0
 */
data class HomeUiState(
    val dataSelecionada: LocalDate = LocalDate.now(),
    val horaAtual: LocalTime = LocalTime.now(),
    val pontosHoje: List<Ponto> = emptyList(),
    val resumoDia: ResumoDia = ResumoDia(data = LocalDate.now()),
    val bancoHoras: BancoHoras = BancoHoras(),
    val proximoTipo: TipoPonto = TipoPonto.ENTRADA,
    val empregoAtivo: Emprego? = null,
    val empregosDisponiveis: List<Emprego> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingEmpregos: Boolean = false,
    val showTimePickerDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val showEmpregoSelector: Boolean = false,
    val pontoParaExcluir: Ponto? = null,
    val erro: String? = null
) {
    companion object {
        private val localeBR = Locale("pt", "BR")
        private val formatterDiaSemana = DateTimeFormatter.ofPattern("EEEE", localeBR)
        private val formatterDataCompleta = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", localeBR)
        private val formatterDataCurta = DateTimeFormatter.ofPattern("dd/MM/yyyy", localeBR)
    }

    /**
     * Verifica se a data selecionada é hoje.
     */
    val isHoje: Boolean
        get() = dataSelecionada == LocalDate.now()

    /**
     * Verifica se a data selecionada é ontem.
     */
    val isOntem: Boolean
        get() = dataSelecionada == LocalDate.now().minusDays(1)

    /**
     * Verifica se a data selecionada é amanhã.
     */
    val isAmanha: Boolean
        get() = dataSelecionada == LocalDate.now().plusDays(1)

    /**
     * Verifica se a data selecionada está no futuro.
     */
    val isFuturo: Boolean
        get() = dataSelecionada.isAfter(LocalDate.now())

    /**
     * Formata a data selecionada para exibição no header.
     */
    val dataFormatada: String
        get() {
            return when {
                isHoje -> "Hoje"
                isOntem -> "Ontem"
                isAmanha -> "Amanhã"
                else -> dataSelecionada.format(formatterDataCompleta).replaceFirstChar { it.uppercase() }
            }
        }

    /**
     * Formata a data para exibição curta (navegador de data).
     */
    val dataFormatadaCurta: String
        get() = dataSelecionada.format(formatterDataCurta)

    /**
     * Obtém o dia da semana formatado.
     */
    val diaSemana: String
        get() = dataSelecionada.format(formatterDiaSemana).replaceFirstChar { it.uppercase() }

    /**
     * Verifica se há pontos registrados no dia.
     */
    val temPontos: Boolean
        get() = pontosHoje.isNotEmpty()

    /**
     * Verifica se há múltiplos empregos disponíveis.
     */
    val temMultiplosEmpregos: Boolean
        get() = empregosDisponiveis.size > 1

    /**
     * Verifica se há emprego ativo selecionado.
     */
    val temEmpregoAtivo: Boolean
        get() = empregoAtivo != null

    /**
     * Nome do emprego ativo para exibição.
     */
    val nomeEmpregoAtivo: String
        get() = empregoAtivo?.nome ?: "Nenhum emprego"

    /**
     * Verifica se pode registrar ponto (tem emprego ativo e não é data futura).
     */
    val podeRegistrarPonto: Boolean
        get() = temEmpregoAtivo && !isFuturo && (empregoAtivo?.podeRegistrarPonto == true)

    /**
     * Verifica se pode navegar para a data anterior.
     * Limite de 1 ano no passado para evitar problemas de performance.
     */
    val podeNavegaAnterior: Boolean
        get() = dataSelecionada.isAfter(LocalDate.now().minusYears(1))

    /**
     * Verifica se pode navegar para a próxima data.
     * Limite de 1 mês no futuro para casos de planejamento.
     */
    val podeNavegarProximo: Boolean
        get() = dataSelecionada.isBefore(LocalDate.now().plusMonths(1))
}
