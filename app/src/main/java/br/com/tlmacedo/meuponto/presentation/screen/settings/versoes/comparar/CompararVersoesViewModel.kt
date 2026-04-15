package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes.comparar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.presentation.navigation.MeuPontoDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel da tela de comparação de versões de jornada.
 */
@HiltViewModel
class CompararVersoesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val empregoRepository: br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
) : ViewModel() {

    private val versaoId1: Long =
        savedStateHandle.get<Long>(MeuPontoDestinations.ARG_VERSAO_ID_1) ?: -1L
    private val versaoId2: Long =
        savedStateHandle.get<Long>(MeuPontoDestinations.ARG_VERSAO_ID_2) ?: -1L

    private val _uiState = MutableStateFlow(CompararVersoesUiState())
    val uiState: StateFlow<CompararVersoesUiState> = _uiState.asStateFlow()

    init {
        carregarVersoes()
    }

    private fun carregarVersoes() {
        if (versaoId1 <= 0L || versaoId2 <= 0L) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Identificadores de versão inválidos"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val v1 = versaoJornadaRepository.buscarPorId(versaoId1)
                val v2 = versaoJornadaRepository.buscarPorId(versaoId2)

                if (v1 == null || v2 == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Uma ou ambas as versões não foram encontradas"
                        )
                    }
                } else {
                    val emprego = empregoRepository.buscarPorId(v1.empregoId)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            versao1 = v1,
                            versao2 = v2,
                            empregoApelido = emprego?.apelido ?: emprego?.nome,
                            empregoLogo = emprego?.logo
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar versões para comparação")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Erro ao carregar versões"
                    )
                }
            }
        }
    }
}
