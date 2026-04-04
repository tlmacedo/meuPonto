package br.com.tlmacedo.meuponto.presentation.screen.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.repository.AuthRepository
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import br.com.tlmacedo.meuponto.domain.usecase.auth.ValidarLoginUseCase
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
    private val preferenciasRepository: PreferenciasRepository,
    private val validarLoginUseCase: ValidarLoginUseCase
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
            val biometriaHabilitada = preferenciasRepository.isBiometriaHabilitada()
            
            _uiState.update { it.copy(
                lembrarMe = lembrarMe,
                biometriaHabilitada = biometriaHabilitada
            ) }

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

            // Se biometria já está habilitada e disponível, dispara o login automaticamente
            if (biometriaHabilitada && _uiState.value.biometriaDisponivel) {
                loginComBiometria()
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
            LoginAction.LoginBiometriaClick -> loginComBiometria()
            LoginAction.ClicarCadastrar -> emitirEvento(LoginEvent.NavegarParaRegistro)
            LoginAction.ClicarEsqueciSenha -> emitirEvento(LoginEvent.NavegarParaEsqueciSenha)
            
            is LoginAction.BiometriaDisponibilidadeAlterada -> {
                _uiState.update { it.copy(biometriaDisponivel = action.disponivel) }
                // Se biometria está habilitada e disponível, tenta disparar o login biográfico
                if (action.disponivel && _uiState.value.biometriaHabilitada) {
                    loginComBiometria()
                }
            }
            LoginAction.HabilitarBiometriaConfirmado -> {
                viewModelScope.launch {
                    preferenciasRepository.definirBiometriaHabilitada(true)
                    _uiState.update { it.copy(biometriaHabilitada = true, showDialogHabilitarBiometria = false) }
                    _eventos.emit(LoginEvent.LoginSucesso)
                }
            }
            LoginAction.HabilitarBiometriaCancelado -> {
                _uiState.update { it.copy(showDialogHabilitarBiometria = false) }
                viewModelScope.launch { _eventos.emit(LoginEvent.LoginSucesso) }
            }
        }
    }

    private fun validarFormulario(state: LoginUiState) {
        val resultado = validarLoginUseCase(state.email, state.senha)
        
        _uiState.update { it.copy(
            isFormValido = resultado.isValido
        ) }
    }

    private fun login() {
        val estadoAtual = _uiState.value
        if (!estadoAtual.isFormValido) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCarregando = true, erro = null) }

            val resultado = authRepository.login(estadoAtual.email, estadoAtual.senha)

            resultado.onSuccess {
                salvarPreferenciasPosLogin(estadoAtual)
                
                _uiState.update { it.copy(isCarregando = false) }
                
                // Se a biometria está disponível mas NÃO está habilitada, pergunta se quer habilitar
                if (estadoAtual.biometriaDisponivel && !estadoAtual.biometriaHabilitada) {
                    _uiState.update { it.copy(showDialogHabilitarBiometria = true) }
                } else {
                    _eventos.emit(LoginEvent.LoginSucesso)
                }
            }.onFailure { excecao ->
                val msgErro = excecao.message ?: "Erro ao fazer login"
                _uiState.update { it.copy(isCarregando = false, erro = msgErro) }
                _eventos.emit(LoginEvent.MostrarErro(msgErro))
            }
        }
    }

    private fun loginComBiometria() {
        viewModelScope.launch {
            _eventos.emit(LoginEvent.SolicitarBiometria)
        }
    }
    
    // Chamado pelo LoginScreen após autenticação biométrica com sucesso
    fun onBiometriaSucesso() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCarregando = true, erro = null) }
            
            // Em um app real, aqui usaríamos um token salvo com segurança (EncryptedSharedPreferences/DataStore)
            // Para este exemplo, vamos assumir que o sucesso da biometria permite o login do último usuário.
            val ultimoEmail = preferenciasRepository.obterUltimoEmailLogado()
            if (ultimoEmail != null) {
                _uiState.update { it.copy(isCarregando = false) }
                
                // Se logou com biometria mas o recurso ainda não está "Habilitado" para login automático, pergunta se quer ativar
                if (!_uiState.value.biometriaHabilitada) {
                    _uiState.update { it.copy(showDialogHabilitarBiometria = true) }
                } else {
                    _eventos.emit(LoginEvent.LoginSucesso)
                }
            } else {
                _uiState.update { it.copy(isCarregando = false, erro = "Nenhum usuário salvo para biometria") }
                _eventos.emit(LoginEvent.MostrarErro("Nenhum usuário salvo para biometria. Faça login com senha primeiro."))
            }
        }
    }

    private suspend fun salvarPreferenciasPosLogin(estado: LoginUiState) {
        preferenciasRepository.definirLembrarMe(estado.lembrarMe)
        if (estado.lembrarMe) {
            preferenciasRepository.definirUltimoEmailLogado(estado.email)
        } else if (!estado.biometriaHabilitada) { 
            // Se nem lembrar me nem biometria estão ativos, limpa o email
            preferenciasRepository.definirUltimoEmailLogado("")
        }
    }

    private fun emitirEvento(evento: LoginEvent) {
        viewModelScope.launch { _eventos.emit(evento) }
    }
}
