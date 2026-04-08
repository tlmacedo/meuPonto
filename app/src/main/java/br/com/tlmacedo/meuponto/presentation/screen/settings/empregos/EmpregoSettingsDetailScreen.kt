package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import kotlinx.coroutines.flow.collectLatest
import java.time.format.DateTimeFormatter

/**
 * Tela de detalhes e configurações de um emprego específico.
 *
 * Serve como hub central para todas as configurações relacionadas ao emprego:
 * - Informações da Empresa
 * - Cargos e Salários
 * - Configuração Geral (RH, Banco de Horas, NSR, Localização, Foto)
 * - Jornadas Versionadas
 * - Ausências e Ajustes de Saldo
 *
 * @author Thiago
 * @since 4.0.0
 * @updated 29.0.0 - Redesenhado como hub completo de configurações do emprego
 */
@Composable
fun EmpregoSettingsDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVersoes: (Long) -> Unit,
    onNavigateToEditarEmprego: ((Long) -> Unit)? = null,
    onNavigateToCargos: ((Long) -> Unit)? = null,
    onNavigateToAusencias: ((Long) -> Unit)? = null,
    onNavigateToAjustesSaldo: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: EmpregoSettingsDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is EmpregoSettingsDetailEvent.NavegarParaVersoes -> {
                    onNavigateToVersoes(evento.empregoId)
                }
                is EmpregoSettingsDetailEvent.NavegarParaAusencias -> {
                    onNavigateToAusencias?.invoke(evento.empregoId)
                }
                is EmpregoSettingsDetailEvent.NavegarParaAjustesSaldo -> {
                    onNavigateToAjustesSaldo?.invoke(evento.empregoId)
                }
                is EmpregoSettingsDetailEvent.NavegarParaEditar -> {
                    onNavigateToEditarEmprego?.invoke(evento.empregoId)
                }
                is EmpregoSettingsDetailEvent.NavegarParaCargos -> {
                    onNavigateToCargos?.invoke(evento.empregoId)
                }
                is EmpregoSettingsDetailEvent.MostrarMensagem -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = uiState.nomeEmprego,
                subtitle = if (uiState.empregoAtivo) "Emprego Ativo" else "Emprego Inativo",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.errorMessage ?: "Erro desconhecido",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.onAction(EmpregoSettingsDetailAction.Recarregar) }
                        ) {
                            Text("Tentar novamente")
                        }
                    }
                }
            }

            else -> {
                EmpregoSettingsDetailContent(
                    uiState = uiState,
                    onNavigateToEditar = {
                        viewModel.onAction(EmpregoSettingsDetailAction.NavegarParaEditar)
                    },
                    onNavigateToCargos = {
                        viewModel.onAction(EmpregoSettingsDetailAction.NavegarParaCargos)
                    },
                    onNavigateToVersoes = {
                        viewModel.onAction(EmpregoSettingsDetailAction.NavegarParaVersoes)
                    },
                    onNavigateToAusencias = {
                        viewModel.onAction(EmpregoSettingsDetailAction.NavegarParaAusencias)
                    },
                    onNavigateToAjustesSaldo = {
                        viewModel.onAction(EmpregoSettingsDetailAction.NavegarParaAjustesSaldo)
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun EmpregoSettingsDetailContent(
    uiState: EmpregoSettingsDetailUiState,
    onNavigateToEditar: () -> Unit,
    onNavigateToCargos: () -> Unit,
    onNavigateToVersoes: () -> Unit,
    onNavigateToAusencias: () -> Unit,
    onNavigateToAjustesSaldo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // ══════════════════════════════════════════════════════════════
        // CARD DE DESTAQUE DO EMPREGO
        // ══════════════════════════════════════════════════════════════
        item {
            EmpregoHeaderCard(
                nomeEmprego = uiState.nomeEmprego,
                apelido = uiState.emprego?.apelido,
                isAtivo = uiState.empregoAtivo,
                versaoVigenteDescricao = uiState.versaoVigenteDescricao,
                cargoAtual = uiState.cargoAtual
            )
        }

        // ══════════════════════════════════════════════════════════════
        // SEÇÃO: INFORMAÇÕES DA EMPRESA
        // ══════════════════════════════════════════════════════════════
        item {
            SettingsSectionHeader(
                title = "Informações da Empresa",
                icon = Icons.Default.Business
            )
        }

        item {
            SettingsNavigationItem(
                icon = Icons.Default.Edit,
                title = "Dados da Empresa",
                subtitle = buildString {
                    uiState.emprego?.let { emp ->
                        append(emp.nome)
                        emp.endereco?.let { append(" • $it") }
                    } ?: append("Nome, datas, endereço e informações gerais")
                },
                onClick = onNavigateToEditar
            )
        }

        // ══════════════════════════════════════════════════════════════
        // SEÇÃO: CARGOS E SALÁRIOS
        // ══════════════════════════════════════════════════════════════
        item {
            Spacer(modifier = Modifier.height(4.dp))
            SettingsSectionHeader(
                title = "Cargos na Empresa",
                icon = Icons.Default.Badge
            )
        }

        item {
            SettingsNavigationItem(
                icon = Icons.Default.Work,
                title = "Cargos e Salários",
                subtitle = buildString {
                    if (uiState.totalCargos > 0) {
                        append("${uiState.totalCargos} cargo(s) registrado(s)")
                        uiState.cargoAtual?.let { append(" • Atual: $it") }
                    } else {
                        append("Histórico de funções, salários e dissídios")
                    }
                },
                badge = if (uiState.totalCargos > 0) uiState.totalCargos.toString() else null,
                onClick = onNavigateToCargos
            )
        }

        // ══════════════════════════════════════════════════════════════
        // SEÇÃO: CONFIGURAÇÃO GERAL
        // ══════════════════════════════════════════════════════════════
        item {
            Spacer(modifier = Modifier.height(4.dp))
            SettingsSectionHeader(
                title = "Configuração Geral do Emprego",
                icon = Icons.Default.Settings
            )
        }

        item {
            SettingsNavigationItem(
                icon = Icons.Default.CalendarMonth,
                title = "Info RH e Banco de Horas",
                subtitle = buildString {
                    uiState.configuracao?.let { cfg ->
                        append("Fechamento dia ${cfg.diaInicioFechamentoRH}")
                        if (cfg.bancoHorasHabilitado) {
                            append(" • Banco de horas: ${cfg.bancoHorasCicloMeses} meses")
                        }
                    } ?: append("Dia de fechamento, ciclos e banco de horas")
                },
                onClick = onNavigateToEditar
            )
        }

        item {
            SettingsNavigationItem(
                icon = Icons.Default.LocationOn,
                title = "Opções de Registro",
                subtitle = buildString {
                    uiState.configuracao?.let { cfg ->
                        val opcoes = mutableListOf<String>()
                        if (cfg.habilitarNsr) opcoes.add("NSR")
                        if (cfg.habilitarLocalizacao) opcoes.add("Localização")
                        if (cfg.fotoHabilitada) opcoes.add("Foto")
                        if (cfg.exigeJustificativaInconsistencia) opcoes.add("Justificativa")
                        if (opcoes.isEmpty()) append("NSR, Localização, Foto e Justificativas")
                        else append(opcoes.joinToString(" • "))
                    } ?: append("NSR, Localização, Foto e Justificativas")
                },
                onClick = onNavigateToEditar
            )
        }

        // ══════════════════════════════════════════════════════════════
        // SEÇÃO: JORNADAS VERSIONADAS
        // ══════════════════════════════════════════════════════════════
        item {
            Spacer(modifier = Modifier.height(4.dp))
            SettingsSectionHeader(
                title = "Jornadas Versionadas",
                icon = Icons.Default.Schedule
            )
        }

        item {
            SettingsNavigationItem(
                icon = Icons.Default.History,
                title = "Versões de Jornada",
                subtitle = buildString {
                    append("${uiState.totalVersoes} versão(ões)")
                    uiState.versaoVigenteDescricao?.let {
                        append(" • Vigente: $it")
                    }
                },
                badge = if (uiState.totalVersoes > 0) uiState.totalVersoes.toString() else null,
                onClick = onNavigateToVersoes
            )
        }

        // ══════════════════════════════════════════════════════════════
        // SEÇÃO: REGISTROS E AUSÊNCIAS
        // ══════════════════════════════════════════════════════════════
        item {
            Spacer(modifier = Modifier.height(4.dp))
            SettingsSectionHeader(
                title = "Registros e Ausências",
                icon = Icons.AutoMirrored.Filled.EventNote
            )
        }

        item {
            SettingsNavigationItem(
                icon = Icons.AutoMirrored.Filled.EventNote,
                title = "Ausências",
                subtitle = "Férias, licenças, atestados e afastamentos",
                onClick = onNavigateToAusencias
            )
        }

        item {
            SettingsNavigationItem(
                icon = Icons.Default.AccountBalance,
                title = "Ajustes de Saldo",
                subtitle = "Ajustes manuais no banco de horas",
                onClick = onNavigateToAjustesSaldo
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// COMPONENTES INTERNOS
// ════════════════════════════════════════════════════════════════════════════════

@Composable
private fun EmpregoHeaderCard(
    nomeEmprego: String,
    apelido: String?,
    isAtivo: Boolean,
    versaoVigenteDescricao: String?,
    cargoAtual: String?,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isAtivo)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = if (isAtivo)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = nomeEmprego,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isAtivo)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!apelido.isNullOrBlank()) {
                        Text(
                            text = apelido,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isAtivo)
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Status badge
                Surface(
                    color = if (isAtivo)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (isAtivo) "Ativo" else "Inativo",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isAtivo)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.surface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (cargoAtual != null || versaoVigenteDescricao != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    color = if (isAtivo)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                    else
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    cargoAtual?.let {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cargo Atual",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isAtivo)
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (isAtivo)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    versaoVigenteDescricao?.let {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Jornada Vigente",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isAtivo)
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (isAtivo)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
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
private fun SettingsNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    enabled: Boolean = true
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (badge != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
}
