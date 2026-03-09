package br.com.tlmacedo.meuponto.presentation.screen.settings.global

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.components.settings.SettingsSectionCard

@Composable
fun GlobalSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Configurações Globais",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                SettingsSectionCard(
                    title = "Aparência",
                    subtitle = "Tema, cores, densidade visual e preferências de interface.",
                    icon = Icons.Default.DarkMode,
                    onClick = { }
                )
            }

            item {
                SettingsSectionCard(
                    title = "Notificações",
                    subtitle = "Lembretes, alertas de ponto e avisos importantes.",
                    icon = Icons.Default.Notifications,
                    onClick = { }
                )
            }

            item {
                SettingsSectionCard(
                    title = "Backup e dados",
                    subtitle = "Exportação, importação e manutenção local de dados.",
                    icon = Icons.Default.Save,
                    onClick = { }
                )
            }

            item {
                SettingsSectionCard(
                    title = "Privacidade e segurança",
                    subtitle = "Proteção do app, biometria e controle de acesso.",
                    icon = Icons.Default.Security,
                    onClick = { }
                )
            }
        }
    }
}
