// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/editponto/EditPontoViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.editponto

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.MarcadorRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.util.ComprovanteImageStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/**
 * ViewModel da tela de edição de ponto.
 *
 * ## Sobre tipoPonto:
 * Este ViewModel não gerencia tipoPonto. O tipo do ponto é calculado
 * dinamicamente pelo domínio com base na posição do registro no dia:
 * posição ímpar = entrada, posição par = saída. Portanto:
 * - Não existe campo tipoPonto em [EditPontoUiState]
 * - Não existe ação AlterarTipoPonto em [EditPontoAction]
 * - O Ponto.copy() não inclui tipoPonto pois o campo não existe no modelo
 *
 * ## Correções aplicadas (12.0.0):
 * - Removido tipoPonto de todas as referências (linha 168: Unresolved reference)
 * - Corrigido Ponto.copy() (linhas 297-299: No parameter with name 'data',
 *   'hora', 'tipoPonto'). O modelo Ponto usa os campos confirmados no código
 *   fonte: data, hora, empregoId, observacao, nsr, fotoComprovantePath
 * - when(action) exaustivo para todas as subclasses de EditPontoAction
 *
 * @param savedStateHandle Handle para leitura do argumento de navegação pontoId
 * @param pontoRepository Repositório de pontos
 * @param empregoRepository Repositório de empregos
 * @param marcadorRepository Repositório de marcadores
 * @param imageStorage Armazenamento físico de fotos
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 12.0.0 - Removido tipoPonto; corrigido Ponto.copy(); when exaustivo
 */
@HiltViewModel
class EditPontoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pontoRepository: PontoRepository,
    private val empregoRepository: EmpregoRepository,
    private val marcadorRepository: MarcadorRepository,
    private val imageStorage: ComprovanteImageStorage
) : ViewModel() {

    private val pontoId: Long = checkNotNull(savedStateHandle["pontoId"])

    private val _uiState = MutableStateFlow(EditPontoUiState())
    val uiState: StateFlow<EditPontoUiState> = _uiState.asStateFlow()

    init {
        carregarDados()
    }

    // ========================================================================
    // PONTO ÚNICO DE ENTRADA DE AÇÕES
    // ========================================================================

    /**
     * Despacha ações da UI para os handlers correspondentes.
     * O when é exaustivo — cobre todas as subclasses de [EditPontoAction].
     */
    fun onAction(action: EditPontoAction) {
        when (action) {
            is EditPontoAction.AlterarData -> alterarData(action.data)
            is EditPontoAction.AlterarHora -> alterarHora(action.hora)
            is EditPontoAction.AlterarEmprego -> alterarEmprego(action.emprego)
            is EditPontoAction.AlterarObservacao -> alterarObservacao(action.observacao)
            is EditPontoAction.AlterarNsr -> alterarNsr(action.nsr)
            is EditPontoAction.AlterarFoto -> alterarFoto(action.relativePath)
            EditPontoAction.AbrirDatePicker -> _uiState.update { it.copy(mostrarDatePicker = true) }
            EditPontoAction.FecharDatePicker -> _uiState.update { it.copy(mostrarDatePicker = false) }
            EditPontoAction.AbrirTimePicker -> _uiState.update { it.copy(mostrarTimePicker = true) }
            EditPontoAction.FecharTimePicker -> _uiState.update { it.copy(mostrarTimePicker = false) }
            EditPontoAction.AbrirVisualizadorFoto -> { /* implementar na Fase 3 */
            }

            EditPontoAction.RemoverFoto -> removerFoto()
            EditPontoAction.Salvar -> salvar()
            EditPontoAction.Excluir -> excluir()
            EditPontoAction.LimparErro -> limparErro()
        }
    }

    // ========================================================================
    // CARREGAMENTO INICIAL
    // ========================================================================

    /**
     * Carrega o ponto e dados de suporte em background.
     *
     * tipoPonto NÃO é carregado pois é calculado dinamicamente
     * pelo domínio com base na posição do ponto no dia.
     */
    private fun carregarDados() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val ponto = pontoRepository.buscarPorId(pontoId)
                if (ponto == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            erro = "Ponto não encontrado"
                        )
                    }
                    return@launch
                }

                val empregos = empregoRepository.buscarAtivos()
                val empregoAtual = empregos.find { it.id == ponto.empregoId }
                val marcadores = empregoAtual?.let {
                    marcadorRepository.buscarAtivosPorEmprego(it.id)
                } ?: emptyList()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        ponto = ponto,
                        empregos = empregos,
                        marcadores = marcadores,
                        empregoSelecionado = empregoAtual,
                        empregoApelido = empregoAtual?.apelido,
                        empregoLogo = empregoAtual?.logo,
                        // ✅ Decompõe dataHora em data e hora para edição separada
                        data = ponto.dataHora.toLocalDate(),
                        hora = ponto.dataHora.toLocalTime().withSecond(0).withNano(0),
                        observacao = ponto.observacao ?: "",
                        nsr = ponto.nsr ?: "",
                        fotoRelativePath = ponto.fotoComprovantePath,
                        fotoRemovida = false
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar ponto: $pontoId")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao carregar: ${e.message}"
                    )
                }
            }
        }
    }

    // ========================================================================
    // ALTERAÇÕES DE CAMPOS
    // ========================================================================

    private fun alterarData(data: LocalDate) {
        _uiState.update {
            it.copy(
                data = data,
                mostrarDatePicker = false
            )
        }
    }

    private fun alterarHora(hora: LocalTime) {
        _uiState.update {
            it.copy(
                hora = hora,
                mostrarTimePicker = false
            )
        }
    }

    private fun alterarEmprego(emprego: Emprego) {
        viewModelScope.launch {
            try {
                val marcadores = marcadorRepository.buscarAtivosPorEmprego(emprego.id)
                _uiState.update {
                    it.copy(
                        empregoSelecionado = emprego,
                        empregoApelido = emprego.apelido,
                        empregoLogo = emprego.logo,
                        marcadores = marcadores
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar marcadores do emprego: ${emprego.id}")
            }
        }
    }

    private fun alterarObservacao(observacao: String) {
        _uiState.update { it.copy(observacao = observacao) }
    }

    private fun alterarNsr(nsr: String) {
        _uiState.update { it.copy(nsr = nsr) }
    }

    private fun alterarFoto(relativePath: String?) {
        _uiState.update {
            it.copy(
                fotoRelativePath = relativePath,
                fotoRemovida = false
            )
        }
    }

    // ========================================================================
    // FOTO
    // ========================================================================

    /**
     * Marca a foto para remoção e deleta o arquivo físico em background.
     *
     * O estado é atualizado imediatamente para feedback visual instantâneo.
     * A deleção física ocorre em background via viewModelScope.launch —
     * isso corrige o erro original "Suspend function 'delete' should be
     * called only from a coroutine" (linha 291 da versão anterior).
     */
    private fun removerFoto() {
        val relativePath = _uiState.value.fotoRelativePath ?: return

        // Feedback imediato na UI
        _uiState.update {
            it.copy(
                fotoRelativePath = null,
                fotoRemovida = true
            )
        }

        // Deleção física em background
        viewModelScope.launch {
            try {
                val deleted = imageStorage.delete(relativePath)
                if (!deleted) {
                    Timber.w("Arquivo físico não removido: $relativePath")
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao remover foto: $relativePath")
            }
        }
    }

    // ========================================================================
    // SALVAR
    // ========================================================================

    /**
     * Valida e persiste as alterações do ponto.
     *
     * ## Sobre o Ponto.copy():
     * O modelo Ponto contém apenas os campos que são persistidos no banco.
     * tipoPonto NÃO é um desses campos — é calculado dinamicamente.
     * Os campos editáveis são: empregoId, data, hora, observacao, nsr,
     * fotoComprovantePath.
     *
     * Isso resolve os erros de compilação nas linhas 297-299:
     * "No parameter with name 'data' found" — o copy usa os nomes
     * corretos dos campos conforme o modelo Ponto do domínio.
     */
    private fun salvar() {
        val state = _uiState.value
        val pontoOriginal = state.ponto ?: return

        if (state.empregoSelecionado == null) {
            _uiState.update { it.copy(erro = "Selecione um emprego") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // ✅ Correto: dataHora é LocalDateTime.of(data, hora)
                // ❌ Errado (linhas 262-263): copy(data = ..., hora = ...)
                //    pois Ponto não tem campos separados 'data' e 'hora'
                val pontoAtualizado = pontoOriginal.copy(
                    empregoId = state.empregoSelecionado.id,
                    dataHora = LocalDateTime.of(state.data, state.hora),
                    observacao = state.observacao.trim().ifBlank { null },
                    nsr = state.nsr.trim().ifBlank { null },
                    fotoComprovantePath = if (state.fotoRemovida) null
                    else state.fotoRelativePath
                )

                pontoRepository.atualizar(pontoAtualizado)

                Timber.i("Ponto $pontoId atualizado com sucesso")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSalvo = true
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao salvar ponto: $pontoId")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao salvar: ${e.message}"
                    )
                }
            }
        }
    }

    // ========================================================================
    // EXCLUIR
    // ========================================================================

    /**
     * Exclui o ponto permanentemente.
     *
     * [PontoRepository.excluir] recebe um objeto [Ponto] completo
     * conforme confirmado no RepositoryModule e PontoRepositoryImpl.
     */
    private fun excluir() {
        val pontoOriginal = _uiState.value.ponto ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Remove foto física antes de excluir o registro
                _uiState.value.fotoRelativePath?.let { path ->
                    imageStorage.delete(path)
                }

                pontoRepository.excluir(pontoOriginal)

                Timber.i("Ponto $pontoId excluído com sucesso")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSalvo = true
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao excluir ponto: $pontoId")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao excluir: ${e.message}"
                    )
                }
            }
        }
    }

    private fun limparErro() {
        _uiState.update { it.copy(erro = null) }
    }
}