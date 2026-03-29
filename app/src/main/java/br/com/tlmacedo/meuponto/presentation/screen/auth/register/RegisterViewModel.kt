package br.com.tlmacedo.meuponto.presentation.screen.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<RegisterEvent>()
    val eventos: SharedFlow<RegisterEvent> = _eventos.asSharedFlow()

    fun onAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.AlterarNome -> _uiState.update { it.copy(nome = action.nome, erro = null) }
            is RegisterAction.AlterarEmail -> _uiState.update { it.copy(email = action.email, erro = null) }
            is RegisterAction.AlterarSenha -> _uiState.update { it.copy(senha = action.senha, erro = null) }
            is RegisterAction.AlterarConfirmarSenha -> _uiState.update { it.copy(confirmarSenha = action.senha, erro = null) }
            RegisterAction.LimparErro -> _uiState.update { it.copy(erro = null) }
            RegisterAction.Cadastrar -> cadastrar()
            RegisterAction.NavegarParaLogin -> viewModelScope.launch { _eventos.emit(RegisterEvent.NavegarParaLogin) }
        }
    }

    private fun cadastrar() {
        val state = _uiState.value
        if (!state.formularioValido) {
            viewModelScope.launch { _eventos.emit(RegisterEvent.MostrarErro("Verifique os dados informados.")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1500)
            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            _eventos.emit(RegisterEvent.CadastroSucesso("Conta criada com sucesso!"))
        }
    }
}
