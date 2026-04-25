// app/src/main/java/br/com/tlmacedo/meuponto/presentation/chamado/ChamadoViewModel.kt
package br.com.tlmacedo.meuponto.presentation.chamado

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.chamado.AvaliacaoChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.CategoriaChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.Chamado
import br.com.tlmacedo.meuponto.domain.model.chamado.PrioridadeChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.StatusChamado
import br.com.tlmacedo.meuponto.domain.repository.ChamadoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.Collections.emptyList
import javax.inject.Inject

@HiltViewModel
class ChamadoViewModel @Inject constructor(
    private val chamadoRepository: ChamadoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChamadoUiState())
    val uiState: StateFlow<ChamadoUiState> = _uiState.asStateFlow()

    val chamados: StateFlow<List<Chamado>> = chamadoRepository
        .observarTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun criarChamado(
        titulo: String,
        descricao: String,
        categoria: CategoriaChamado,
        prioridade: PrioridadeChamado,
        anexos: List<Uri>,
        usuarioEmail: String,
        usuarioNome: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val chamado = Chamado(
                id = 0L,
                identificador = "",
                titulo = titulo,
                descricao = descricao,
                categoria = categoria,
                prioridade = prioridade,
                status = StatusChamado.ABERTO,
                empregoId = null,
                usuarioEmail = usuarioEmail,
                usuarioNome = usuarioNome,
                criadoEm = LocalDateTime.now(),
                atualizadoEm = LocalDateTime.now(),
                resposta = null,
                anexos = null,
                resolvidoEm = null,
                avaliacaoNota = null,
                avaliacaoComentario = null,
                avaliadoEm = null
            )

            chamadoRepository.criar(chamado, anexos)
                .onSuccess { chamadoCriado ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            sucesso = "Chamado ${chamadoCriado.identificador} criado com sucesso!"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, erro = e.message)
                    }
                }
        }
    }

    fun avaliarChamado(chamadoId: Long, nota: Int, comentario: String?) {
        viewModelScope.launch {
            val avaliacao = AvaliacaoChamado(
                nota = nota,
                comentario = comentario,
                avaliadoEm = LocalDateTime.now()
            )
            chamadoRepository.salvarAvaliacao(chamadoId, avaliacao)
        }
    }

    fun limparMensagens() {
        _uiState.update { it.copy(sucesso = null, erro = null) }
    }
}

data class ChamadoUiState(
    val isLoading: Boolean = false,
    val sucesso: String? = null,
    val erro: String? = null
)