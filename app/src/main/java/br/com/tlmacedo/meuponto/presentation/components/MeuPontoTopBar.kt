// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/MeuPontoTopBar.kt
package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * TopBar customizada do app Meu Ponto.
 *
 * @author Thiago
 * @since 3.0.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeuPontoTopBar(
    title: String,
    subtitle: String? = null,
    showBackButton: Boolean = false,
    showTodayButton: Boolean = false,
    showHistoryButton: Boolean = false,
    showSettingsButton: Boolean = false,
    logo: String? = null,
    onBackClick: () -> Unit = {},
    onTodayClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    actions: @Composable (RowScope.() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                subtitle?.let {
                    Text(
                        text = it.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        navigationIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                if (showBackButton) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
                
                logo?.let {
                    Box(modifier = Modifier.padding(start = if (showBackButton) 0.dp else 8.dp)) {
                        LocalImage(
                            imagePath = it,
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(38.dp)
                                .clip(MaterialTheme.shapes.small),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        },
        actions = {
            // Ações customizadas (se fornecidas)
            actions?.invoke(this)

            // Botões padrão
            if (showTodayButton) {
                IconButton(onClick = onTodayClick) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = "Hoje"
                    )
                }
            }
            if (showHistoryButton) {
                IconButton(onClick = onHistoryClick) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Histórico"
                    )
                }
            }
            if (showSettingsButton) {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configurações"
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )
}
