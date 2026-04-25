package br.com.tlmacedo.meuponto.presentation.screen.settings.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import kotlinx.coroutines.flow.collectLatest

/**
 * Tela de configurações de aparência.
 *
 * Permite ao usuário configurar:
 * - Tema (Claro, Escuro, Sistema, Sidia)
 *
 * @author Thiago
 * @since 9.0.0
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AparenciaScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AparenciaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is AparenciaEvent.MostrarMensagem -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Aparência",
                subtitle = (uiState.empregoApelido ?: uiState.empregoNome)?.uppercase(),
                logo = uiState.empregoLogo,
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ══════════════════════════════════════════════════════════════
            // IDENTIDADE VISUAL (Sidia)
            // ══════════════════════════════════════════════════════════════
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader(
                        title = "Identidade Sidia",
                        icon = Icons.Outlined.Palette
                    )
                    Text(
                        text = "Aplique as cores da marca Sidia ao seu aplicativo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TemaOption(
                        title = "Sidia",
                        subtitle = "Azul, Navy e Ciano",
                        icon = Icons.Outlined.Palette,
                        isSelected = uiState.temaSelecionado == "sidia",
                        onClick = { viewModel.onAction(AparenciaAction.SelecionarTema("sidia")) }
                    )
                    TemaOption(
                        title = "Sidia Dark",
                        subtitle = "Identidade no escuro",
                        icon = Icons.Outlined.DarkMode,
                        isSelected = uiState.temaSelecionado == "sidia_dark",
                        onClick = { viewModel.onAction(AparenciaAction.SelecionarTema("sidia_dark")) }
                    )
                }
            }

            // ══════════════════════════════════════════════════════════════
            // CORES DO SISTEMA
            // ══════════════════════════════════════════════════════════════
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader(
                        title = "Padrão Android",
                        icon = Icons.Outlined.PhoneAndroid
                    )
                    TemaOption(
                        title = "Claro",
                        subtitle = "Tema padrão claro",
                        icon = Icons.Outlined.LightMode,
                        isSelected = uiState.temaSelecionado == "light",
                        onClick = { viewModel.onAction(AparenciaAction.SelecionarTema("light")) }
                    )
                    TemaOption(
                        title = "Escuro",
                        subtitle = "Tema padrão escuro",
                        icon = Icons.Outlined.DarkMode,
                        isSelected = uiState.temaSelecionado == "dark",
                        onClick = { viewModel.onAction(AparenciaAction.SelecionarTema("dark")) }
                    )
                    TemaOption(
                        title = "Seguir Sistema",
                        subtitle = "Sincronizar com as configurações do seu dispositivo",
                        icon = Icons.Outlined.PhoneAndroid,
                        isSelected = uiState.temaSelecionado == "system",
                        onClick = { viewModel.onAction(AparenciaAction.SelecionarTema("system")) }
                    )
                }
            }

            // ══════════════════════════════════════════════════════════════
            // ACESSIBILIDADE
            // ══════════════════════════════════════════════════════════════
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader(
                        title = "Acessibilidade",
                        icon = Icons.Outlined.Contrast
                    )
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🚧 Em breve",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Opções de alto contraste e ajuste de tamanho de fonte estarão disponíveis em uma próxima atualização.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun TemaOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selecionado",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
