// app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/swipe/SwipeAction.kt

package br.com.tlmacedo.meuponto.presentation.components.swipe

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Representa uma ação revelada pelo gesto de swipe.
 *
 * @param icon Ícone da ação
 * @param label Texto descritivo (exibido abaixo do ícone)
 * @param backgroundColor Cor de fundo do botão
 * @param contentColor Cor do ícone e texto
 * @param enabled Se a ação está habilitada
 * @param onClick Callback ao clicar na ação
 *
 * @author Thiago
 * @since 7.1.0
 */
data class SwipeAction(
    val icon: ImageVector,
    val label: String,
    val backgroundColor: Color,
    val contentColor: Color = Color.White,
    val enabled: Boolean = true,
    val onClick: () -> Unit
)

/**
 * Estado do swipe do container.
 */
enum class SwipeState {
    COLLAPSED,
    EXPANDED_START,
    EXPANDED_END
}

/**
 * Factory de ações padrão para uso rápido.
 */
object SwipeActions {

    @Composable
    fun editar(
        enabled: Boolean = true,
        onClick: () -> Unit
    ) = SwipeAction(
        icon = Icons.Default.Edit,
        label = "Editar",
        backgroundColor = MaterialTheme.colorScheme.primary,
        enabled = enabled,
        onClick = onClick
    )

    @Composable
    fun excluir(
        enabled: Boolean = true,
        onClick: () -> Unit
    ) = SwipeAction(
        icon = Icons.Default.Delete,
        label = "Excluir",
        backgroundColor = MaterialTheme.colorScheme.error,
        enabled = enabled,
        onClick = onClick
    )

    @Composable
    fun verLocalizacao(
        enabled: Boolean = true,
        onClick: () -> Unit
    ) = SwipeAction(
        icon = Icons.Default.LocationOn,
        label = "Local",
        backgroundColor = MaterialTheme.colorScheme.tertiary,
        enabled = enabled,
        onClick = onClick
    )

    @Composable
    fun verFoto(
        enabled: Boolean = true,
        onClick: () -> Unit
    ) = SwipeAction(
        icon = Icons.Default.Photo,
        label = "Foto",
        backgroundColor = MaterialTheme.colorScheme.secondary,
        enabled = enabled,
        onClick = onClick
    )

    @Composable
    fun detalhes(
        enabled: Boolean = true,
        onClick: () -> Unit
    ) = SwipeAction(
        icon = Icons.Default.Info,
        label = "Detalhes",
        backgroundColor = MaterialTheme.colorScheme.secondary,
        enabled = enabled,
        onClick = onClick
    )
}
