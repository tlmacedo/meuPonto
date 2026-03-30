package br.com.tlmacedo.meuponto.presentation.screen.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.repository.AuthRepository
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferenciasRepository: PreferenciasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<LoginEvent>()
    val eventos: SharedFlow<LoginEvent> = _eventos.asSharedFlow()

    init {
        carregarPreferencias()
    }

    private fun carregarPreferencias() {
        viewModelScope.launch {
            val lembrarMe = preferenciasRepository.isLembrarMeAtivo()
            _uiState.update { it.copy(lembrarMe = lembrarMe) }

            if (lembrarMe) {
                val ultimoEmail = preferenciasRepository.obterUltimoEmailLogado()
                if (!ultimoEmail.isNullOrBlank()) {
                    _uiState.update { 
                        it.copy(email = ultimoEmail).also { newState ->
                            validarFormulario(newState)
                        }
                    }
                }
            }
        }
    }

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.EmailAlterado -> {
                _uiState.update { 
                    it.copy(email = action.email, emailErro = null, erro = null).also { newState ->
                        validarFormulario(newState)
                    }
                }
            }
            is LoginAction.SenhaAlterada -> {
                _uiState.update { 
                    it.copy(senha = action.senha, senhaErro = null, erro = null).also { newState ->
                        validarFormulario(newState)
                    }
                }
            }
            is LoginAction.LembrarMeAlterado -> _uiState.update { it.copy(lembrarMe = action.lembrar) }
            LoginAction.AlternarSenhaVisibilidade -> _uiState.update { it.copy(isSenhaVisivel = !it.isSenhaVisivel) }
            LoginAction.ClicarEntrar -> login()
            LoginAction.LoginBiometriaClick -> { /* Implementar biometria no futuro */ }
            LoginAction.ClicarCadastrar -> emitirEvento(LoginEvent.NavegarParaRegistro)
            LoginAction.ClicarEsqueciSenha -> emitirEvento(LoginEvent.NavegarParaEsqueciSenha)
        }
    }

    private fun validarFormulario(state: LoginUiState) {
        val emailValido = state.email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()
        val senhaValida = state.senha.length >= 4
        
        _uiState.update { it.copy(isFormValido = emailValido && senhaValida) }
    }

    private fun login() {
        val estadoAtual = _uiState.value
        if (!estadoAtual.isFormValido) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCarregando = true, erro = null) }

            val resultado = authRepository.login(estadoAtual.email, estadoAtual.senha)

            resultado.onSuccess {
                preferenciasRepository.definirLembrarMe(estadoAtual.lembrarMe)
                if (estadoAtual.lembrarMe) {
                    preferenciasRepository.definirUltimoEmailLogado(estadoAtual.email)
                } else {
                    preferenciasRepository.definirUltimoEmailLogado("")
                }

                _uiState.update { it.copy(isCarregando = false) }
                _eventos.emit(LoginEvent.LoginSucesso)
            }.onFailure { excecao ->
                val msgErro = excecao.message ?: "Erro ao fazer login"
                _uiState.update { it.copy(isCarregando = false, erro = msgErro) }
                _eventos.emit(LoginEvent.MostrarErro(msgErro))
            }
        }
    }

    private fun emitirEvento(evento: LoginEvent) {
        viewModelScope.launch { _eventos.emit(evento) }
    }
}
