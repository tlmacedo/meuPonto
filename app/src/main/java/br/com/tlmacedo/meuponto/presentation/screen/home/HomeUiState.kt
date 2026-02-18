// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

import br.com.tlmacedo.meuponto.domain.model.BancoHoras
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ProximoPonto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Estado da tela Home.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 2.5.0 - Adicionado showDatePicker, corrigido formato de data
 * @updated 2.6.0 - Flags para controle de registro por tipo de dia
 * @updated 2.7.0 - Adicionado showEmpregoMenu para menu de opções do emprego
 * @updated 2.8.0 - Adicionada versaoJornadaAtual para exibição do período no ResumoCard
 */
data class HomeUiState(
    val dataSelecionada: LocalDate = LocalDate.now(),
    val horaAtual: LocalTime = LocalTime.now(),
    val pontosHoje: List<Ponto> = emptyList(),
    val resumoDia: ResumoDia = ResumoDia(data = LocalDate.now()),
    val bancoHoras: BancoHoras = BancoHoras(),
    val proximoTipo: ProximoPonto = ProximoPonto(isEntrada = true, descricao = "Entrada", indice = 0),
    val empregoAtivo: Emprego? = null,
    val empregosDisponiveis: List<Emprego> = emptyList(),
    val versaoJornadaAtual: VersaoJornada? = null,
    val isLoading: Boolean = false,
    val isLoadingEmpregos: Boolean = false,
    val showTimePickerDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val showEmpregoSelector: Boolean = false,
    val showEmpregoMenu: Boolean = false,
    val showDatePicker: Boolean = false,
    val pontoParaExcluir: Ponto? = null,
    val erro: String? = null
) {
    companion object {
        private val localeBR = Locale("pt", "BR")
        private val formatterDiaSemana = DateTimeFormatter.ofPattern("EEEE", localeBR)
        private val formatterDiaSemanaAbrev = DateTimeFormatter.ofPattern("EEE", localeBR)
        private val formatterDataCompleta = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", localeBR)
        private val formatterDataCurta = DateTimeFormatter.ofPattern("dd/MM/yyyy", localeBR)
    }

    val isHoje: Boolean
        get() = dataSelecionada == LocalDate.now()

    val isOntem: Boolean
        get() = dataSelecionada == LocalDate.now().minusDays(1)

    val isAmanha: Boolean
        get() = dataSelecionada == LocalDate.now().plusDays(1)

    val isFuturo: Boolean
        get() = dataSelecionada.isAfter(LocalDate.now())

    /**
     * Verifica se a data é passada (anterior a hoje).
     */
    val isPassado: Boolean
        get() = dataSelecionada.isBefore(LocalDate.now())

    /**
     * Formata a data selecionada para exibição no navegador.
     */
    val dataFormatada: String
        get() {
            val diaSemana = dataSelecionada.format(formatterDiaSemana)
                .replaceFirstChar { it.uppercase() }

            return when {
                isHoje -> "$diaSemana, Hoje"
                isOntem -> "$diaSemana, Ontem"
                isAmanha -> "$diaSemana, Amanhã"
                else -> dataSelecionada.format(formatterDataCompleta)
                    .replaceFirstChar { it.uppercase() }
            }
        }

    val dataFormatadaCurta: String
        get() = dataSelecionada.format(formatterDataCurta)

    val diaSemana: String
        get() = dataSelecionada.format(formatterDiaSemana).replaceFirstChar { it.uppercase() }

    val temPontos: Boolean
        get() = pontosHoje.isNotEmpty()

    val temMultiplosEmpregos: Boolean
        get() = empregosDisponiveis.size > 1

    val temEmpregoAtivo: Boolean
        get() = empregoAtivo != null

    val nomeEmpregoAtivo: String
        get() = empregoAtivo?.nome ?: "Nenhum emprego"

    /**
     * Verifica se pode registrar ponto (hoje ou passado, com emprego ativo).
     * Dias futuros NÃO permitem registro de ponto.
     */
    val podeRegistrarPonto: Boolean
        get() = temEmpregoAtivo && !isFuturo && (empregoAtivo?.podeRegistrarPonto == true)

    /**
     * Verifica se pode registrar ponto automático (apenas hoje).
     * Registro automático = com horário atual do sistema.
     */
    val podeRegistrarPontoAutomatico: Boolean
        get() = podeRegistrarPonto && isHoje

    /**
     * Verifica se pode registrar ponto manual (hoje ou passado).
     * Registro manual = usuário informa o horário.
     */
    val podeRegistrarPontoManual: Boolean
        get() = podeRegistrarPonto

    /**
     * Verifica se pode registrar eventos especiais (férias, folga, falta).
     * Permitido em qualquer dia (passado, presente ou futuro).
     */
    val podeRegistrarEventoEspecial: Boolean
        get() = temEmpregoAtivo

    val podeNavegaAnterior: Boolean
        get() = dataSelecionada.isAfter(LocalDate.now().minusYears(1))

    val podeNavegarProximo: Boolean
        get() = dataSelecionada.isBefore(LocalDate.now().plusMonths(1))

    val temIntervaloAberto: Boolean
        get() = isHoje && resumoDia.intervalos.any { it.aberto }

    val intervaloAberto: br.com.tlmacedo.meuponto.domain.model.IntervaloPonto?
        get() = if (isHoje) resumoDia.intervalos.find { it.aberto } else null

    val dataHoraInicioContador: LocalDateTime?
        get() = intervaloAberto?.entrada?.dataHora

    val deveExibirContador: Boolean
        get() = isHoje && temIntervaloAberto && dataHoraInicioContador != null

    val jornadaEmAndamento: Boolean
        get() = temPontos && !resumoDia.jornadaCompleta && isHoje

    val ultimoPonto: Ponto?
        get() = pontosHoje.maxByOrNull { it.dataHora }

    val statusJornada: String
        get() = when {
            !temPontos -> "Aguardando entrada"
            jornadaEmAndamento -> "Jornada em andamento"
            resumoDia.jornadaCompleta -> "Jornada finalizada"
            else -> "Status indefinido"
        }

    // ========================================================================
    // VERSÃO DE JORNADA
    // ========================================================================

    /**
     * Verifica se há uma versão de jornada disponível para a data selecionada.
     */
    val temVersaoJornada: Boolean
        get() = versaoJornadaAtual != null

    /**
     * Período formatado da versão de jornada atual.
     * Ex: "01/01/2025 em diante" ou "01/01/2025 até 31/12/2025"
     */
    val periodoVersaoJornadaFormatado: String?
        get() = versaoJornadaAtual?.periodoFormatado

    /**
     * Título da versão de jornada atual.
     * Ex: "Versão 1" ou "Versão 2 - Horário Flexível"
     */
    val tituloVersaoJornada: String?
        get() = versaoJornadaAtual?.titulo
}
