// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/empregos/EmpregoSettingsViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.repository.AjusteSaldoRepository
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel da tela de configurações do emprego.
 *
 * @author Thiago
 * @since 8.2.0
 */
@HiltViewModel
class EmpregoSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val empregoRepository: EmpregoRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val ajusteSaldoRepository: AjusteSaldoRepository,
    private val ausenciaRepository: AusenciaRepository
) : ViewModel() {

    private val empregoId: Long = savedStateHandle.get<Long>("empregoId") ?: -1L

    private val _uiState = MutableStateFlow(EmpregoSettingsUiState())
    val uiState: StateFlow<EmpregoSettingsUiState> = _uiState.asStateFlow()

    init {
        carregarDados()
    }

    private fun carregarDados() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Carregar emprego
                val emprego = empregoRepository.buscarPorId(empregoId)

                if (emprego == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Emprego não encontrado"
                        )
                    }
                    return@launch
                }

                // Carregar versão vigente
                val versaoVigente = versaoJornadaRepository.buscarVigente(empregoId)

                // Contar versões
                val totalVersoes = versaoJornadaRepository.contarPorEmprego(empregoId)

                // Contar ajustes
                val totalAjustes = ajusteSaldoRepository.contarPorEmprego(empregoId)

                // Contar ausências
                val totalAusencias = ausenciaRepository.contarPorEmprego(empregoId)

                // Calcular saldo do banco de horas (soma dos ajustes)
                val saldoBancoHoras = ajusteSaldoRepository.somarTotalPorEmprego(empregoId)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        emprego = emprego,
                        versaoVigente = versaoVigente,
                        totalVersoes = totalVersoes,
                        totalAjustes = totalAjustes,
                        totalAusencias = totalAusencias,
                        saldoBancoHorasMinutos = saldoBancoHoras
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Erro ao carregar dados"
                    )
                }
            }
        }
    }

    fun recarregar() {
        carregarDados()
    }
}
