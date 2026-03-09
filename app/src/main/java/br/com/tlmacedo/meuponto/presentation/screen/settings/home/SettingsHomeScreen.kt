package br.com.tlmacedo.meuponto.presentation.screen.settings.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.components.settings.SettingsInfoCard
import br.com.tlmacedo.meuponto.presentation.components.settings.SettingsSectionCard

@Composable
fun SettingsHomeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToGlobal: () -> Unit,
    onNavigateToEmpregos: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Configurações",
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
                SettingsInfoCard(
                    title = "Central de Configurações",
                    description = "Gerencie preferências globais do app, configurações de empregos e versões de jornada com uma navegação mais clara e organizada."
                )
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                SettingsSectionCard(
                    title = "Configurações Globais",
                    subtitle = "Preferências gerais do sistema, aparência, notificações e comportamento padrão.",
                    icon = Icons.Default.Tune,
                    onClick = onNavigateToGlobal
                )
            }

            item {
                SettingsSectionCard(
                    title = "Empregos",
                    subtitle = "Gerencie múltiplos empregos, regras específicas e versões de jornada por emprego.",
                    icon = Icons.Default.BusinessCenter,
                    onClick = onNavigateToEmpregos
                )
            }

            item {
                SettingsSectionCard(
                    title = "Sistema",
                    subtitle = "Informações técnicas, versão do app e opções administrativas.",
                    icon = Icons.Default.Settings,
                    onClick = { }
                )
            }
        }
    }
}
