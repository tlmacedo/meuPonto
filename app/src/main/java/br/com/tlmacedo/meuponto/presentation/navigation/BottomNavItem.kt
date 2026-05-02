// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/navigation/BottomNavItem.kt
package br.com.tlmacedo.meuponto.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class que define os itens da barra de navegação inferior.
 *
 * Cada item contém as informações necessárias para renderização
 * e navegação: rota, label, e ícones para estados selecionado/não selecionado.
 *
 * @property route Rota de navegação associada ao item
 * @property label Texto exibido abaixo do ícone (em português)
 * @property selectedIcon Ícone quando o item está selecionado
 * @property unselectedIcon Ícone quando o item não está selecionado
 *
 * @author Thiago
 * @since 1.0.0
 */
sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    /**
     * Item de navegação para a tela inicial.
     */
    data object Home : BottomNavItem(
        route = MeuPontoDestinations.HOME_BASE,
        label = "Início",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    /**
     * Item de navegação para a tela de histórico.
     */
    data object History : BottomNavItem(
        route = MeuPontoDestinations.HISTORY_BASE,
        label = "Histórico",
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange
    )

    /**
     * Item de navegação para a tela de configurações.
     */
    data object Settings : BottomNavItem(
        route = MeuPontoDestinations.SETTINGS,
        label = "Configurações",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    companion object {
        /**
         * Lista de todos os itens da barra de navegação inferior.
         *
         * @return Lista ordenada dos itens de navegação
         */
        fun getItems(): List<BottomNavItem> = listOf(Home, History, Settings)
    }
}
