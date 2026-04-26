// app/src/main/java/br/com/tlmacedo/meuponto/presentation/chamado/ChamadoViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.chamado

import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.BuildConfig
import br.com.tlmacedo.meuponto.domain.model.chamado.AvaliacaoChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.CategoriaChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.Chamado
import br.com.tlmacedo.meuponto.domain.model.chamado.PrioridadeChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.StatusChamado
import br.com.tlmacedo.meuponto.domain.repository.AuthRepository
import br.com.tlmacedo.meuponto.domain.repository.ChamadoRepository
import br.com.tlmacedo.meuponto.domain.service.SistemaNotificacaoService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ChamadoViewModel @Inject constructor(
    private val chamadoRepository: ChamadoRepository,
    private val authRepository: AuthRepository,
    private val sistemaNotificacaoService: SistemaNotificacaoService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChamadoUiState())
    val uiState: StateFlow<ChamadoUiState> = _uiState.asStateFlow()

    val usuarioLogado = authRepository.observarUsuarioLogado()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun criarChamado(
        titulo: String,
        descricao: String,
        passosParaReproduzir: String? = null,
        categoria: CategoriaChamado,
        prioridade: PrioridadeChamado,
        anexos: List<Uri>
    ) {
        val usuario = usuarioLogado.value ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val deviceInfo = """
                App: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
                Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
                Device: ${Build.MANUFACTURER} ${Build.MODEL}
            """.trimIndent()

            // Coleta logs (simplificado para o MVP)
            val logs = try {
                val process = Runtime.getRuntime().exec("logcat -d")
                process.inputStream.bufferedReader().use { it.readText() }.takeLast(20000)
            } catch (e: Exception) {
                "Erro ao coletar logs: ${e.message}"
            }

            val descricaoComLogs = """
                $descricao
                
                --- LOGS DO SISTEMA ---
                $logs
            """.trimIndent()

            val chamado = Chamado(
                id = 0L,
                identificador = "",
                titulo = titulo,
                descricao = descricaoComLogs,
                passosParaReproduzir = passosParaReproduzir,
                deviceInfo = deviceInfo,
                categoria = categoria,
                prioridade = prioridade,
                status = StatusChamado.ABERTO,
                empregoId = null,
                usuarioEmail = usuario.email,
                usuarioNome = usuario.nome,
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