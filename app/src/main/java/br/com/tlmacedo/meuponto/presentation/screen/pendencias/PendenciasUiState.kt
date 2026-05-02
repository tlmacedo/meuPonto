// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/pendencias/PendenciasUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.pendencias

import br.com.tlmacedo.meuponto.domain.model.PendenciaDia
import br.com.tlmacedo.meuponto.domain.usecase.pendencias.ListarPendenciasPontoUseCase
import java.time.LocalDate

enum class TabPendencias(val label: String) {
    TODOS("Todos"),
    BLOQUEANTES("Bloqueantes"),
    PENDENTES("Pendentes"),
    INFORMATIVOS("Informativos"),
}

data class PendenciasUiState(
    val isLoading: Boolean = false,
    val resultado: ListarPendenciasPontoUseCase.ResultadoPendencias? = null,
    val tabSelecionada: TabPendencias = TabPendencias.TODOS,
    val dataInicio: LocalDate = LocalDate.now().minusDays(30),
    val dataFim: LocalDate = LocalDate.now(),
    val mensagemErro: String? = null,
    val empregoId: Long? = null
) {
    val diasExibidos: List<PendenciaDia>
        get() {
            val r = resultado ?: return emptyList()
            return when (tabSelecionada) {
                TabPendencias.TODOS -> (r.bloqueantes + r.pendentes + r.informativos)
                    .sortedByDescending { it.data }
                TabPendencias.BLOQUEANTES -> r.bloqueantes
                TabPendencias.PENDENTES -> r.pendentes
                TabPendencias.INFORMATIVOS -> r.informativos
            }
        }

    val contadorPorTab: Map<TabPendencias, Int>
        get() {
            val r = resultado ?: return emptyMap()
            return mapOf(
                TabPendencias.TODOS to r.total,
                TabPendencias.BLOQUEANTES to r.bloqueantes.size,
                TabPendencias.PENDENTES to r.pendentes.size,
                TabPendencias.INFORMATIVOS to r.informativos.size
            )
        }

    val temPendencias: Boolean get() = resultado?.temPendencias == true
}

sealed class PendenciasEvent {
    data object Carregar : PendenciasEvent()
    data class SelecionarTab(val tab: TabPendencias) : PendenciasEvent()
    data object LimparErro : PendenciasEvent()
}
