package br.com.tlmacedo.meuponto.presentation.screen.auth.forgotpassword

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
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<ForgotPasswordEvent>()
    val eventos: SharedFlow<ForgotPasswordEvent> = _eventos.asSharedFlow()

    fun onAction(action: ForgotPasswordAction) {
        when (action) {
            is ForgotPasswordAction.EmailAlterado -> {
                _uiState.update {
                    it.copy(email = action.email, emailErro = null, erro = null).also { newState ->
                        validarFormulario(newState)
                    }
                }
            }

            ForgotPasswordAction.ClicarEnviar -> enviarRecuperacao()
            ForgotPasswordAction.ClicarVoltar -> {
                viewModelScope.launch { _eventos.emit(ForgotPasswordEvent.NavegarVoltar) }
            }
        }
    }

    private fun validarFormulario(state: ForgotPasswordUiState) {
        val emailValido =
            state.email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(state.email)
                .matches()
        _uiState.update { it.copy(isFormValido = emailValido) }
    }

    private fun enviarRecuperacao() {
        val email = _uiState.value.email
        if (!_uiState.value.isFormValido) {
            _uiState.update { it.copy(emailErro = "E-mail inválido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCarregando = true, erro = null, mensagemSucesso = null) }

            val resultado = authRepository.recuperarSenha(email)

            resultado.onSuccess {
                _uiState.update {
                    it.copy(
                        isCarregando = false,
                        mensagemSucesso = "As instruções de recuperação foram enviadas para o seu e-mail."
                    )
                }
                _eventos.emit(ForgotPasswordEvent.MostrarSucesso("Instruções enviadas!"))
            }.onFailure { excecao ->
                val msgErro = excecao.message ?: "Erro ao solicitar recuperação de senha"
                _uiState.update { it.copy(isCarregando = false, erro = msgErro) }
                _eventos.emit(ForgotPasswordEvent.MostrarErro(msgErro))
            }
        }
    }
}
