// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/historicociclos/HistoricoCiclosViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.historicociclos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.CicloBancoHoras
import br.com.tlmacedo.meuponto.domain.model.FechamentoPeriodo
import br.com.tlmacedo.meuponto.domain.model.TipoFechamento
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.domain.repository.FechamentoPeriodoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularBancoHorasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel da tela de Histórico de Ciclos do Banco de Horas.
 *
 * Gerencia a listagem e exibição de todos os ciclos do banco de horas,
 * incluindo o ciclo atual e os ciclos históricos (fechados).
 *
 * @author Thiago
 * @since 9.0.0
 */
@HiltViewModel
class HistoricoCiclosViewModel @Inject constructor(
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase,
    private val fechamentoPeriodoRepository: FechamentoPeriodoRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val calcularBancoHorasUseCase: CalcularBancoHorasUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoricoCiclosUiState())
    val uiState: StateFlow<HistoricoCiclosUiState> = _uiState.asStateFlow()

    private var empregoIdAtual: Long? = null

    init {
        carregarCiclos()
    }

    /**
     * Carrega todos os ciclos do banco de horas.
     */
    fun carregarCiclos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                obterEmpregoAtivoUseCase.observar().collect { emprego ->
                    if (emprego == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Nenhum emprego ativo encontrado"
                            )
                        }
                        return@collect
                    }

                    empregoIdAtual = emprego.id

                    // Buscar versão vigente para obter configuração do ciclo
                    val versaoVigente = versaoJornadaRepository.buscarVigente(emprego.id)

                    // Calcular duração do ciclo baseado na configuração
                    val duracaoCicloDias = calcularDuracaoCicloDias(versaoVigente)

                    // Buscar todos os fechamentos de banco de horas (manual e automático de ciclo)
                    val fechamentos = fechamentoPeriodoRepository
                        .buscarPorEmpregoId(emprego.id)
                        .filter {
                            it.tipo == TipoFechamento.BANCO_HORAS ||
                                    it.tipo == TipoFechamento.CICLO_BANCO_AUTOMATICO
                        }
                        .sortedByDescending { it.dataFimPeriodo }

                    // Construir lista de ciclos
                    val ciclos = mutableListOf<CicloBancoHoras>()

                    // Adicionar ciclos históricos (fechados)
                    fechamentos.forEach { fechamento ->
                        ciclos.add(
                            CicloBancoHoras(
                                dataInicio = fechamento.dataInicioPeriodo,
                                dataFim = fechamento.dataFimPeriodo,
                                saldoInicialMinutos = 0, // Após fechamento, começa do zero
                                saldoAtualMinutos = fechamento.saldoAnteriorMinutos,
                                fechamento = fechamento,
                                isCicloAtual = false
                            )
                        )
                    }

                    // Calcular e adicionar ciclo atual
                    val cicloAtual = calcularCicloAtual(
                        empregoId = emprego.id,
                        versaoVigente = versaoVigente,
                        ultimoFechamento = fechamentos.firstOrNull(),
                        duracaoCicloDias = duracaoCicloDias
                    )
                    if (cicloAtual != null) {
                        ciclos.add(0, cicloAtual) // Adiciona no início da lista
                    }

                    _uiState.update { state ->
                        state.copy(
                            ciclos = ciclos,
                            empregoNome = emprego.nome,
                            empregoApelido = emprego.apelido ?: "",
                            empregoLogo = emprego.logo,
                            isLoading = false
                        )
                    }
                }

            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar ciclos do banco de horas")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Erro ao carregar ciclos: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Calcula a duração do ciclo em dias baseado na configuração da versão.
     */
    private fun calcularDuracaoCicloDias(versao: VersaoJornada?): Int {
        if (versao == null) return 30 // Padrão: 30 dias

        return when {
            versao.periodoBancoSemanas > 0 -> versao.periodoBancoSemanas * 7
            versao.periodoBancoMeses > 0 -> versao.periodoBancoMeses * 30
            else -> 30 // Padrão: 30 dias
        }
    }

    /**
     * Calcula o ciclo atual com base no último fechamento e configuração.
     */
    private suspend fun calcularCicloAtual(
        empregoId: Long,
        versaoVigente: VersaoJornada?,
        ultimoFechamento: FechamentoPeriodo?,
        duracaoCicloDias: Int
    ): CicloBancoHoras? {
        val hoje = LocalDate.now()

        // Determinar data de início do ciclo atual
        val dataInicioCiclo = when {
            // Se tem data de início configurada na versão, usar ela
            versaoVigente?.dataInicioCicloBancoAtual != null -> {
                versaoVigente.dataInicioCicloBancoAtual
            }
            // Se tem último fechamento, próximo ciclo começa no dia seguinte
            ultimoFechamento != null -> {
                ultimoFechamento.dataFimPeriodo.plusDays(1)
            }
            // Sem fechamento e sem configuração, usar padrão
            else -> {
                hoje.minusDays((duracaoCicloDias - 1).toLong())
            }
        }

        // Se a data de início do ciclo atual é futura, não há ciclo atual
        if (dataInicioCiclo.isAfter(hoje)) {
            return null
        }

        // Calcular data de fim do ciclo
        val dataFimCiclo = when {
            versaoVigente?.periodoBancoSemanas != null && versaoVigente.periodoBancoSemanas > 0 -> {
                dataInicioCiclo.plusWeeks(versaoVigente.periodoBancoSemanas.toLong()).minusDays(1)
            }

            versaoVigente?.periodoBancoMeses != null && versaoVigente.periodoBancoMeses > 0 -> {
                dataInicioCiclo.plusMonths(versaoVigente.periodoBancoMeses.toLong()).minusDays(1)
            }

            else -> {
                dataInicioCiclo.plusDays((duracaoCicloDias - 1).toLong())
            }
        }

        // Calcular saldo atual do ciclo
        val saldoAtual = try {
            val resultado = calcularBancoHorasUseCase.calcularAteData(empregoId, hoje)
            resultado.saldoTotal.toMinutes().toInt()
        } catch (e: Exception) {
            Timber.e(e, "Erro ao calcular saldo do ciclo atual")
            0
        }

        return CicloBancoHoras(
            dataInicio = dataInicioCiclo,
            dataFim = dataFimCiclo,
            saldoInicialMinutos = 0,
            saldoAtualMinutos = saldoAtual,
            fechamento = null,
            isCicloAtual = true
        )
    }

    /**
     * Alterna a expansão de um ciclo na lista.
     */
    fun toggleCicloExpandido(index: Int) {
        _uiState.update { state ->
            state.copy(
                cicloExpandido = if (state.cicloExpandido == index) null else index
            )
        }
    }

    /**
     * Limpa a mensagem de erro.
     */
    fun limparErro() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Recarrega os dados.
     */
    fun recarregar() {
        carregarCiclos()
    }
}
