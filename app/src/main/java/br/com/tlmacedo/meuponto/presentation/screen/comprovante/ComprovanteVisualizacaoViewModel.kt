package br.com.tlmacedo.meuponto.presentation.screen.comprovante

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.presentation.navigation.MeuPontoDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComprovanteVisualizacaoViewModel @Inject constructor(
    private val pontoRepository: PontoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val pontoId: Long = checkNotNull(savedStateHandle[MeuPontoDestinations.ARG_PONTO_ID])

    private val _ponto = MutableStateFlow<Ponto?>(null)
    val ponto = _ponto.asStateFlow()

    init {
        carregarPonto()
    }

    private fun carregarPonto() {
        viewModelScope.launch {
            _ponto.value = pontoRepository.buscarPorId(pontoId)
        }
    }
}
