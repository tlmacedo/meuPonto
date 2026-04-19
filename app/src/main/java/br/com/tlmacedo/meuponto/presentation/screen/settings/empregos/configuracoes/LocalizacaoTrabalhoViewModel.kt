package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.configuracoes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.presentation.navigation.MeuPontoDestinations
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocalizacaoTrabalhoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val configuracaoRepository: ConfiguracaoEmpregoRepository,
    private val empregoRepository: EmpregoRepository
) : ViewModel() {

    private val empregoId: Long = checkNotNull(savedStateHandle[MeuPontoDestinations.ARG_EMPREGO_ID])

    private val _uiState = MutableStateFlow(LocalizacaoTrabalhoUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<LocalizacaoTrabalhoEvent>()
    val eventos = _eventos.asSharedFlow()

    init {
        carregarDados()
    }

    private fun carregarDados() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val emprego = empregoRepository.buscarPorId(empregoId)
            val config = configuracaoRepository.buscarPorEmpregoId(empregoId)

            val loc = if (config?.latitude != null && config.longitude != null) {
                LatLng(config.latitude!!, config.longitude!!)
            } else null

            _uiState.update {
                it.copy(
                    isLoading = false,
                    nomeEmprego = emprego?.nome ?: "",
                    localizacaoInicial = loc,
                    localizacaoSelecionada = loc,
                    raioMetros = config?.raioGeofencing ?: 200
                )
            }
        }
    }

    fun onAction(action: LocalizacaoTrabalhoAction) {
        when (action) {
            is LocalizacaoTrabalhoAction.SelecionarLocalizacao -> {
                _uiState.update { it.copy(localizacaoSelecionada = action.latLng) }
            }
            is LocalizacaoTrabalhoAction.AlterarRaio -> {
                _uiState.update { it.copy(raioMetros = action.raio) }
            }
            LocalizacaoTrabalhoAction.Confirmar -> {
                val loc = _uiState.value.localizacaoSelecionada
                if (loc != null) {
                    viewModelScope.launch {
                        _uiState.update { it.copy(isLoading = true) }
                        val config = configuracaoRepository.buscarPorEmpregoId(empregoId)
                        val configParaSalvar = (config ?: br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego(empregoId = empregoId)).copy(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            raioGeofencing = _uiState.value.raioMetros,
                            habilitarLocalizacao = true // Habilita automaticamente se configurar o local
                        )
                        
                        if (configParaSalvar.id == 0L) {
                            configuracaoRepository.inserir(configParaSalvar)
                        } else {
                            configuracaoRepository.atualizar(configParaSalvar)
                        }
                        
                        _uiState.update { it.copy(isLoading = false) }
                        _eventos.emit(
                            LocalizacaoTrabalhoEvent.Confirmado(
                                latitude = loc.latitude,
                                longitude = loc.longitude,
                                raio = _uiState.value.raioMetros
                            )
                        )
                    }
                }
            }
            LocalizacaoTrabalhoAction.Voltar -> {
                viewModelScope.launch {
                    _eventos.emit(LocalizacaoTrabalhoEvent.Voltar)
                }
            }
        }
    }
}
