package br.com.tlmacedo.meuponto.presentation.screen.auth.login

import br.com.tlmacedo.meuponto.domain.model.Usuario
import br.com.tlmacedo.meuponto.domain.repository.AuthRepository
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import br.com.tlmacedo.meuponto.domain.usecase.auth.ValidarLoginUseCase
import br.com.tlmacedo.meuponto.util.MainDispatcherRule
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: AuthRepository
    private lateinit var preferenciasRepository: PreferenciasRepository
    private lateinit var validarLoginUseCase: ValidarLoginUseCase
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        authRepository = mockk(relaxed = true)
        preferenciasRepository = mockk(relaxed = true)
        validarLoginUseCase = ValidarLoginUseCase() // Podemos usar a real ou mockar
        
        // Mock inicial das preferências para evitar NPE no init
        coEvery { preferenciasRepository.isLembrarMeAtivo() } returns false
        coEvery { preferenciasRepository.isBiometriaHabilitada() } returns false
        
        viewModel = LoginViewModel(authRepository, preferenciasRepository, validarLoginUseCase)
    }

    @Test
    fun `init deve carregar preferencias do DataStore`() = runTest {
        coEvery { preferenciasRepository.isLembrarMeAtivo() } returns true
        coEvery { preferenciasRepository.obterUltimoEmailLogado() } returns "teste@teste.com"
        
        // Recria o ViewModel para disparar o init com os novos mocks
        viewModel = LoginViewModel(authRepository, preferenciasRepository, validarLoginUseCase)
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.lembrarMe).isTrue()
            assertThat(state.email).isEqualTo("teste@teste.com")
        }
    }

    @Test
    fun `login com sucesso deve emitir evento LoginSucesso`() = runTest {
        val email = "teste@teste.com"
        val senha = "senha"
        
        coEvery { authRepository.login(email, senha) } returns Result.success(
            Usuario("1", "Teste", email, false)
        )

        viewModel.onAction(LoginAction.EmailAlterado(email))
        viewModel.onAction(LoginAction.SenhaAlterada(senha))
        
        viewModel.eventos.test {
            viewModel.onAction(LoginAction.ClicarEntrar)
            assertThat(awaitItem()).isEqualTo(LoginEvent.LoginSucesso)
        }
    }
    
    @Test
    fun `AlternarSenhaVisibilidade deve inverter estado no uiState`() = runTest {
        viewModel.uiState.test {
            var state = awaitItem()
            assertThat(state.isSenhaVisivel).isFalse()
            
            viewModel.onAction(LoginAction.AlternarSenhaVisibilidade)
            state = awaitItem()
            assertThat(state.isSenhaVisivel).isTrue()
            
            viewModel.onAction(LoginAction.AlternarSenhaVisibilidade)
            state = awaitItem()
            assertThat(state.isSenhaVisivel).isFalse()
        }
    }
}
