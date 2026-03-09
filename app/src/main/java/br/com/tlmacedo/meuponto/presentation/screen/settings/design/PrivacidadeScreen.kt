package br.com.tlmacedo.meuponto.presentation.screen.settings.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar

/**
 * Tela de configurações de privacidade.
 *
 * Permite ao usuário configurar:
 * - Bloqueio por biometria
 * - Ocultar preview na tela de recentes
 * - Tempo para bloqueio automático
 *
 * @author Thiago
 * @since 9.0.0
 */
@Composable
fun PrivacidadeScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO: Implementar ViewModel com persistência real
    var biometriaHabilitada by remember { mutableStateOf(false) }
    var ocultarPreview by remember { mutableStateOf(false) }
    var bloqueioAutomatico by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Privacidade",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ══════════════════════════════════════════════════════════════
            // SEÇÃO: PROTEÇÃO DO APP
            // ══════════════════════════════════════════════════════════════
            item {
                SectionHeader(
                    title = "Proteção do App",
                    icon = Icons.Outlined.Security
                )
            }

            item {
                PrivacidadeSwitch(
                    title = "Bloqueio por biometria",
                    subtitle = "Exigir impressão digital ou reconhecimento facial para abrir o app",
                    icon = Icons.Outlined.Fingerprint,
                    checked = biometriaHabilitada,
                    onCheckedChange = { biometriaHabilitada = it }
                )
            }

            item {
                PrivacidadeSwitch(
                    title = "Bloqueio automático",
                    subtitle = "Bloquear o app após ficar em segundo plano",
                    icon = Icons.Outlined.Lock,
                    checked = bloqueioAutomatico,
                    onCheckedChange = { bloqueioAutomatico = it },
                    enabled = biometriaHabilitada
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // ══════════════════════════════════════════════════════════════
            // SEÇÃO: VISIBILIDADE
            // ══════════════════════════════════════════════════════════════
            item {
                SectionHeader(
                    title = "Visibilidade",
                    icon = Icons.Outlined.Visibility
                )
            }

            item {
                PrivacidadeSwitch(
                    title = "Ocultar na tela de recentes",
                    subtitle = "Esconder conteúdo do app quando alternar entre aplicativos",
                    icon = Icons.Outlined.VisibilityOff,
                    checked = ocultarPreview,
                    onCheckedChange = { ocultarPreview = it }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ══════════════════════════════════════════════════════════════
            // INFORMATIVO
            // ══════════════════════════════════════════════════════════════
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "💡 Dica de segurança",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Seus dados de ponto são armazenados localmente no dispositivo. " +
                                    "Para maior segurança, recomendamos ativar o bloqueio por biometria " +
                                    "e fazer backups regulares.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
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
        modifier = modifier.padding(vertical = 8.dp)
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
private fun PrivacidadeSwitch(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
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
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    }
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}
