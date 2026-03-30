package br.com.tlmacedo.meuponto.presentation.screen.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<RegisterEvent>()
    val eventos: SharedFlow<RegisterEvent> = _eventos.asSharedFlow()

    fun onAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.NomeAlterado -> _uiState.update { it.copy(nome = action.nome, erro = null) }
            is RegisterAction.EmailAlterado -> _uiState.update { it.copy(email = action.email, erro = null) }
            is RegisterAction.SenhaAlterada -> _uiState.update { it.copy(senha = action.senha, erro = null) }
            is RegisterAction.ConfirmarSenhaAlterada -> _uiState.update { it.copy(confirmarSenha = action.senha, erro = null) }
            RegisterAction.AlternarSenhaVisibilidade -> _uiState.update { it.copy(isSenhaVisivel = !it.isSenhaVisivel) }
            RegisterAction.ClicarCadastrar -> cadastrar()
            RegisterAction.NavegarParaLogin -> emitirEvento(RegisterEvent.CadastroSucesso)
        }
    }

    private fun cadastrar() {
        val estadoAtual = _uiState.value
        if (estadoAtual.nome.isBlank() || estadoAtual.email.isBlank() || estadoAtual.senha.isBlank()) {
            emitirEvento(RegisterEvent.MostrarErro("Preencha todos os campos"))
            return
        }
        if (estadoAtual.senha != estadoAtual.confirmarSenha) {
            emitirEvento(RegisterEvent.MostrarErro("As senhas não coincidem"))
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCarregando = true, erro = null) }

            val resultado = authRepository.register(estadoAtual.nome, estadoAtual.email, estadoAtual.senha)

            resultado.onSuccess {
                _uiState.update { it.copy(isCarregando = false) }
                _eventos.emit(RegisterEvent.CadastroSucesso)
            }.onFailure { excecao ->
                val msgErro = excecao.message ?: "Erro ao cadastrar"
                _uiState.update { it.copy(isCarregando = false, erro = msgErro) }
                _eventos.emit(RegisterEvent.MostrarErro(msgErro))
            }
        }
    }

    private fun emitirEvento(evento: RegisterEvent) {
        viewModelScope.launch { _eventos.emit(evento) }
    }
}
