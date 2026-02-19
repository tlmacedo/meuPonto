// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/SettingsScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar

/**
 * Tela principal de configurações com menu de navegação para sub-telas.
 *
 * Exibe um menu organizado por seções, permitindo ao usuário acessar
 * as diferentes configurações do aplicativo.
 *
 * @param onNavigateBack Callback para voltar à tela anterior
 * @param onNavigateToEmpregos Callback para navegar à tela de gerenciamento de empregos
 * @param onNavigateToJornada Callback para navegar à tela de configuração de jornada
 * @param onNavigateToHorarios Callback para navegar à tela de horários por dia
 * @param onNavigateToAjustesBancoHoras Callback para navegar à tela de ajustes de banco de horas
 * @param onNavigateToMarcadores Callback para navegar à tela de marcadores
 * @param onNavigateToFeriados Callback para navegar à tela de feriados
 * @param onNavigateToSobre Callback para navegar à tela sobre o app
 * @param modifier Modificador opcional para customização do layout
 * @param viewModel ViewModel da tela de configurações
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 3.4.0 - Adicionado item de menu para Feriados
 */
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEmpregos: () -> Unit,
    onNavigateToJornada: () -> Unit,
    onNavigateToHorarios: () -> Unit,
    onNavigateToAjustesBancoHoras: () -> Unit,
    onNavigateToMarcadores: () -> Unit,
    onNavigateToFeriados: () -> Unit,
    onNavigateToSobre: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Header com emprego ativo
            uiState.empregoAtivo?.let { emprego ->
                EmpregoAtivoHeader(
                    nomeEmprego = emprego.nome,
                    onClick = onNavigateToEmpregos
                )
            }

            // Seção: Emprego
            SettingsSectionHeader(title = "Emprego")

            SettingsMenuItem(
                icon = Icons.Default.Business,
                title = "Gerenciar Empregos",
                subtitle = "Adicionar, editar ou arquivar empregos",
                onClick = onNavigateToEmpregos
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Seção: Jornada de Trabalho
            SettingsSectionHeader(title = "Jornada de Trabalho")

            SettingsMenuItem(
                icon = Icons.Default.Schedule,
                title = "Configuração de Jornada",
                subtitle = "Carga horária, tolerâncias e limites",
                onClick = onNavigateToJornada
            )

            SettingsMenuItem(
                icon = Icons.Default.CalendarMonth,
                title = "Horários por Dia",
                subtitle = "Definir horários para cada dia da semana",
                onClick = onNavigateToHorarios
            )

            SettingsMenuItem(
                icon = Icons.Default.Event,
                title = "Feriados",
                subtitle = "Gerenciar feriados e pontes facultativas",
                onClick = onNavigateToFeriados
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Seção: Banco de Horas
            SettingsSectionHeader(title = "Banco de Horas")

            SettingsMenuItem(
                icon = Icons.Default.AccountBalance,
                title = "Ajustes de Saldo",
                subtitle = "Adicionar ou remover horas manualmente",
                onClick = onNavigateToAjustesBancoHoras
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Seção: Personalização
            SettingsSectionHeader(title = "Personalização")

            SettingsMenuItem(
                icon = Icons.AutoMirrored.Filled.Label,
                title = "Marcadores",
                subtitle = "Criar e gerenciar tags para pontos",
                onClick = onNavigateToMarcadores
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Seção: Sobre
            SettingsSectionHeader(title = "Aplicativo")

            SettingsMenuItem(
                icon = Icons.Default.Info,
                title = "Sobre",
                subtitle = "Versão ${uiState.appVersion}",
                onClick = onNavigateToSobre
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Header mostrando o emprego ativo atual.
 *
 * Card destacado no topo da tela que exibe o emprego selecionado
 * e permite navegação rápida para a tela de gerenciamento de empregos.
 *
 * @param nomeEmprego Nome do emprego ativo
 * @param onClick Callback ao clicar no card
 * @param modifier Modificador opcional
 */
@Composable
private fun EmpregoAtivoHeader(
    nomeEmprego: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Work,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = "Emprego Ativo",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = nomeEmprego,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ver empregos",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Cabeçalho de seção de configurações.
 *
 * Texto estilizado que separa visualmente os grupos de opções.
 *
 * @param title Título da seção
 * @param modifier Modificador opcional
 */
@Composable
private fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

/**
 * Item de menu de configurações.
 *
 * Componente clicável que exibe ícone, título, subtítulo e seta indicando navegação.
 *
 * @param icon Ícone à esquerda do item
 * @param title Título principal do item
 * @param subtitle Descrição secundária do item
 * @param onClick Callback ao clicar no item
 * @param modifier Modificador opcional
 */
@Composable
private fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
