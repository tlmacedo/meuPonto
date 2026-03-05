// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/empregos/EmpregoSettingsScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.format.DateTimeFormatter

/**
 * Tela de configurações específicas de um emprego.
 *
 * Contém as configurações que dependem de uma versão de jornada:
 * - Versões de Jornada (histórico e configuração atual)
 * - Ajustes de Saldo do banco de horas
 * - Ausências (férias, folgas, etc.)
 *
 * @author Thiago
 * @since 8.2.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpregoSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditarEmprego: (Long) -> Unit,
    onNavigateToVersoes: (Long) -> Unit,
    onNavigateToEditarVersao: (Long) -> Unit,
    onNavigateToAjustesSaldo: (Long) -> Unit,
    onNavigateToAusencias: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EmpregoSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Configurações",
                            style = MaterialTheme.typography.titleMedium
                        )
                        uiState.emprego?.let { emprego ->
                            Text(
                                text = emprego.nome,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    uiState.emprego?.let { emprego ->
                        IconButton(onClick = { onNavigateToEditarEmprego(emprego.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar emprego")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.emprego == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Emprego não encontrado",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            else -> {
                val emprego = uiState.emprego!!
                EmpregoSettingsContent(
                    uiState = uiState,
                    onNavigateToVersoes = { onNavigateToVersoes(emprego.id) },
                    onNavigateToEditarVersao = onNavigateToEditarVersao,
                    onNavigateToAjustesSaldo = { onNavigateToAjustesSaldo(emprego.id) },
                    onNavigateToAusencias = { onNavigateToAusencias(emprego.id) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun EmpregoSettingsContent(
    uiState: EmpregoSettingsUiState,
    onNavigateToVersoes: () -> Unit,
    onNavigateToEditarVersao: (Long) -> Unit,
    onNavigateToAjustesSaldo: () -> Unit,
    onNavigateToAusencias: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card: Resumo do Emprego
        item {
            EmpregoResumoCard(uiState = uiState)
        }

        // Seção: Jornada de Trabalho
        item {
            SettingsSection(
                titulo = "Jornada de Trabalho",
                icon = Icons.Default.Schedule
            ) {
                // Versão vigente
                uiState.versaoVigente?.let { versao ->
                    SettingsItemInfo(
                        titulo = "Versão Vigente",
                        valor = "Desde ${versao.dataInicio.format(dateFormatter)}",
                        destaque = versao.cargaHorariaDiariaFormatada
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsItemInfo(
                        titulo = "Carga Horária Semanal",
                        valor = versao.cargaHorariaSemanalFormatada
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsItemInfo(
                        titulo = "Jornada Máxima Diária",
                        valor = versao.jornadaMaximaFormatada
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Link: Editar configuração atual
                    SettingsItemClickable(
                        icon = Icons.Default.CalendarMonth,
                        titulo = "Editar Jornada Atual",
                        subtitulo = "Horários por dia da semana",
                        onClick = { onNavigateToEditarVersao(versao.id) }
                    )
                }

                // Link: Histórico de versões
                SettingsItemClickable(
                    icon = Icons.Default.History,
                    titulo = "Versões de Jornada",
                    subtitulo = "Histórico de alterações",
                    badge = uiState.totalVersoes.takeIf { it > 1 }?.let { "$it versões" },
                    onClick = onNavigateToVersoes
                )
            }
        }

        // Seção: Banco de Horas
        item {
            SettingsSection(
                titulo = "Banco de Horas",
                icon = Icons.Default.AccountBalance
            ) {
                // Saldo atual
                SettingsItemInfo(
                    titulo = "Saldo Atual",
                    valor = uiState.saldoBancoHorasFormatado,
                    destaqueColor = if (uiState.saldoBancoHorasMinutos >= 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Link: Ajustes de saldo
                SettingsItemClickable(
                    icon = Icons.Default.AccountBalance,
                    titulo = "Ajustes de Saldo",
                    subtitulo = "Adicionar ou remover horas manualmente",
                    badge = uiState.totalAjustes.takeIf { it > 0 }?.let { "$it ajustes" },
                    onClick = onNavigateToAjustesSaldo
                )
            }
        }

        // Seção: Ausências
        item {
            SettingsSection(
                titulo = "Ausências",
                icon = Icons.Default.BeachAccess
            ) {
                // Resumo de ausências
                if (uiState.totalAusencias > 0) {
                    SettingsItemInfo(
                        titulo = "Total de Ausências",
                        valor = "${uiState.totalAusencias} registradas"
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Link: Gerenciar ausências
                SettingsItemClickable(
                    icon = Icons.Default.BeachAccess,
                    titulo = "Gerenciar Ausências",
                    subtitulo = "Férias, folgas, faltas e licenças",
                    badge = uiState.totalAusencias.takeIf { it > 0 }?.let { "$it" },
                    onClick = onNavigateToAusencias
                )
            }
        }

        // Espaço final
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

// ============================================================================
// Componentes
// ============================================================================

@Composable
private fun EmpregoResumoCard(
    uiState: EmpregoSettingsUiState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.emprego?.nome ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                uiState.versaoVigente?.let { versao ->
                    Text(
                        text = "Jornada: ${versao.cargaHorariaDiariaFormatada}/dia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    titulo: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingsItemInfo(
    titulo: String,
    valor: String,
    destaque: String? = null,
    destaqueColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            destaque?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = destaqueColor
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = valor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (destaque == null) destaqueColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SettingsItemClickable(
    icon: ImageVector,
    titulo: String,
    subtitulo: String? = null,
    badge: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.bodyLarge
                )
                subtitulo?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            badge?.let {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}
