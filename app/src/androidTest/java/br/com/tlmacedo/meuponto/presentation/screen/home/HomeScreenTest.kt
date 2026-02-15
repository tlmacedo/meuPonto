// Arquivo: app/src/androidTest/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeScreenTest.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import br.com.tlmacedo.meuponto.domain.model.BancoHoras
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_exibeHoje_quandoDataAtualSelecionada() {
        val uiState = HomeUiState(
            dataSelecionada = LocalDate.now(),
            empregoAtivo = Emprego(id = 1, nome = "Empresa Teste"),
            empregosDisponiveis = listOf(Emprego(id = 1, nome = "Empresa Teste"))
        )

        composeTestRule.setContent {
            HomeContent(
                uiState = uiState,
                onAction = {}
            )
        }

        composeTestRule.onNodeWithText("Hoje").assertIsDisplayed()
    }

    @Test
    fun homeScreen_exibeOntem_quandoNavegaParaDiaAnterior() {
        val uiState = HomeUiState(
            dataSelecionada = LocalDate.now().minusDays(1),
            empregoAtivo = Emprego(id = 1, nome = "Empresa Teste"),
            empregosDisponiveis = listOf(Emprego(id = 1, nome = "Empresa Teste"))
        )

        composeTestRule.setContent {
            HomeContent(
                uiState = uiState,
                onAction = {}
            )
        }

        composeTestRule.onNodeWithText("Ontem").assertIsDisplayed()
    }

    @Test
    fun homeScreen_exibeChipEmprego_comNomeCorreto() {
        val uiState = HomeUiState(
            empregoAtivo = Emprego(id = 1, nome = "Minha Empresa"),
            empregosDisponiveis = listOf(Emprego(id = 1, nome = "Minha Empresa"))
        )

        composeTestRule.setContent {
            HomeContent(
                uiState = uiState,
                onAction = {}
            )
        }

        composeTestRule.onNodeWithText("Minha Empresa").assertIsDisplayed()
    }

    @Test
    fun homeScreen_ocultaBotaoRegistrar_quandoDataFutura() {
        val uiState = HomeUiState(
            dataSelecionada = LocalDate.now().plusDays(1),
            empregoAtivo = Emprego(id = 1, nome = "Empresa"),
            empregosDisponiveis = listOf(Emprego(id = 1, nome = "Empresa"))
        )

        composeTestRule.setContent {
            HomeContent(
                uiState = uiState,
                onAction = {}
            )
        }

        // Botão de registrar não deve aparecer
        composeTestRule.onNodeWithText("Registrar", substring = true).assertDoesNotExist()
        // Aviso de data futura deve aparecer
        composeTestRule.onNodeWithText("Data futura").assertIsDisplayed()
    }

    @Test
    fun homeScreen_exibeAvisoSemEmprego_quandoNaoHaEmpregoAtivo() {
        val uiState = HomeUiState(
            empregoAtivo = null,
            empregosDisponiveis = emptyList()
        )

        composeTestRule.setContent {
            HomeContent(
                uiState = uiState,
                onAction = {}
            )
        }

        composeTestRule.onNodeWithText("Nenhum emprego configurado").assertIsDisplayed()
    }

    @Test
    fun homeScreen_exibeEstadoVazio_quandoSemPontos() {
        val uiState = HomeUiState(
            pontosHoje = emptyList(),
            empregoAtivo = Emprego(id = 1, nome = "Empresa"),
            empregosDisponiveis = listOf(Emprego(id = 1, nome = "Empresa"))
        )

        composeTestRule.setContent {
            HomeContent(
                uiState = uiState,
                onAction = {}
            )
        }

        composeTestRule.onNodeWithText("Nenhum ponto registrado").assertIsDisplayed()
    }

    @Test
    fun homeScreen_mostraSetaEmprego_quandoMultiplosEmpregos() {
        val uiState = HomeUiState(
            empregoAtivo = Emprego(id = 1, nome = "Empresa 1"),
            empregosDisponiveis = listOf(
                Emprego(id = 1, nome = "Empresa 1"),
                Emprego(id = 2, nome = "Empresa 2")
            )
        )

        composeTestRule.setContent {
            HomeContent(
                uiState = uiState,
                onAction = {}
            )
        }

        // Chip deve ser clicável (tem seta)
        composeTestRule.onNodeWithText("Empresa 1").assertHasClickAction()
    }
}
