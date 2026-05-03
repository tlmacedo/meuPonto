// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/pendencias/PendenciasUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.pendencias

import br.com.tlmacedo.meuponto.domain.model.PendenciaDia
import br.com.tlmacedo.meuponto.domain.usecase.pendencias.CalcularSaudeDoEmpregoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.pendencias.ListarPendenciasPontoUseCase
import java.time.LocalDate
import java.time.YearMonth

data class DialogoJustificativaState(
    val pendencia: PendenciaDia,
    val sugestoes: List<String>,
    val textoAtual: String = "",
)

enum class TabPendencias(val label: String) {
    TODOS("Todos"),
    BLOQUEADOS("Bloqueados"),
    PENDENTES("Pendentes"),
    EM_ANDAMENTO("Em andamento"),
    INFORMATIVOS("Informativos"),
}

data class PendenciasUiState(
    val isLoading: Boolean = false,
    val resultado: ListarPendenciasPontoUseCase.ResultadoPendencias? = null,
    val saude: CalcularSaudeDoEmpregoUseCase.SaudeEmprego? = null,
    val tabSelecionada: TabPendencias = TabPendencias.TODOS,
    val mesReferencia: YearMonth = YearMonth.now(),
    val mensagemErro: String? = null,
    val empregoId: Long? = null,
    val dialogoJustificativa: DialogoJustificativaState? = null,
    val isSalvandoJustificativa: Boolean = false,
    val mensagemSucesso: String? = null,
) {
    val diasExibidos: List<PendenciaDia>
        get() {
            val r = resultado ?: return emptyList()
            return when (tabSelecionada) {
                TabPendencias.TODOS -> (r.bloqueados + r.pendentes + r.emAndamento + r.informativos)
                    .sortedByDescending { it.data }
                TabPendencias.BLOQUEADOS -> r.bloqueados
                TabPendencias.PENDENTES -> r.pendentes
                TabPendencias.EM_ANDAMENTO -> r.emAndamento
                TabPendencias.INFORMATIVOS -> r.informativos
            }
        }

    val contadorPorTab: Map<TabPendencias, Int>
        get() {
            val r = resultado ?: return emptyMap()
            return mapOf(
                TabPendencias.TODOS to r.total,
                TabPendencias.BLOQUEADOS to r.bloqueados.size,
                TabPendencias.PENDENTES to r.pendentes.size,
                TabPendencias.EM_ANDAMENTO to r.emAndamento.size,
                TabPendencias.INFORMATIVOS to r.informativos.size
            )
        }

    val temPendencias: Boolean get() = resultado?.temPendencias == true
}

sealed class PendenciasEvent {
    data object Carregar : PendenciasEvent()
    data class AlterarMes(val delta: Long) : PendenciasEvent()
    data class SelecionarTab(val tab: TabPendencias) : PendenciasEvent()
    data object LimparErro : PendenciasEvent()
    data object LimparSucesso : PendenciasEvent()
    data class AbrirDialogoJustificativa(val pendencia: PendenciaDia) : PendenciasEvent()
    data object FecharDialogoJustificativa : PendenciasEvent()
    data class AlterarTextoJustificativa(val texto: String) : PendenciasEvent()
    data class SelecionarSugestao(val sugestao: String) : PendenciasEvent()
    data class ConfirmarJustificativa(val data: LocalDate, val justificativa: String) : PendenciasEvent()
}
