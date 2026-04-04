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
            is RegisterAction.NomeAlterado -> {
                _uiState.update { 
                    it.copy(nome = action.nome, nomeErro = null, erro = null).also { newState ->
                        validarFormulario(newState)
                    }
                }
            }
            is RegisterAction.EmailAlterado -> {
                _uiState.update { 
                    it.copy(email = action.email, emailErro = null, erro = null).also { newState ->
                        validarFormulario(newState)
                    }
                }
            }
            is RegisterAction.SenhaAlterada -> {
                _uiState.update { 
                    it.copy(senha = action.senha, senhaErro = null, erro = null).also { newState ->
                        validarFormulario(newState)
                    }
                }
            }
            is RegisterAction.ConfirmarSenhaAlterada -> {
                _uiState.update { 
                    it.copy(confirmarSenha = action.senha, confirmarSenhaErro = null, erro = null).also { newState ->
                        validarFormulario(newState)
                    }
                }
            }
            RegisterAction.AlternarSenhaVisibilidade -> _uiState.update { it.copy(isSenhaVisivel = !it.isSenhaVisivel) }
            RegisterAction.ClicarCadastrar -> cadastrar()
            RegisterAction.NavegarParaLogin -> emitirEvento(RegisterEvent.CadastroSucesso)
        }
    }

    private fun validarFormulario(state: RegisterUiState) {
        val emailValido = state.email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()
        val senhaValida = state.senha.length >= 6
        val senhasCoincidem = state.senha == state.confirmarSenha
        val nomeValido = state.nome.trim().split(" ").size >= 2

        _uiState.update { it.copy(
            isFormValido = nomeValido && emailValido && senhaValida && senhasCoincidem
        ) }
    }

    private fun cadastrar() {
        val estadoAtual = _uiState.value
        
        // Validação final detalhada para exibir erros específicos se o usuário clicar em "Cadastrar" com o form inválido
        val nomeValido = estadoAtual.nome.trim().split(" ").size >= 2
        val emailValido = estadoAtual.email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(estadoAtual.email).matches()
        val senhaValida = estadoAtual.senha.length >= 6
        val senhasCoincidem = estadoAtual.senha == estadoAtual.confirmarSenha

        if (!nomeValido || !emailValido || !senhaValida || !senhasCoincidem) {
            _uiState.update { it.copy(
                nomeErro = if (nomeValido) null else "Informe nome e sobrenome",
                emailErro = if (emailValido) null else "E-mail inválido",
                senhaErro = if (senhaValida) null else "Mínimo 6 caracteres",
                confirmarSenhaErro = if (senhasCoincidem) null else "As senhas não coincidem"
            ) }
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
