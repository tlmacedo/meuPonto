// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/SettingsScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.screen.settings.components.EmpregoSelectorItem
import br.com.tlmacedo.meuponto.presentation.screen.settings.components.EmptyStateNoEmpregos
import br.com.tlmacedo.meuponto.presentation.screen.settings.components.SettingsDivider
import br.com.tlmacedo.meuponto.presentation.screen.settings.components.SettingsMenuItem
import br.com.tlmacedo.meuponto.presentation.screen.settings.components.SettingsSectionHeader
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Tela principal de configurações do aplicativo.
 *
 * Reorganizada em 3 seções principais:
 * - Empregos (gerenciar + configurações do emprego ativo)
 * - Calendário (feriados globais)
 * - Aplicativo (configurações globais)
 *
 * @author Thiago
 * @since 3.0.0
 * @updated 8.2.0 - Reorganização: Jornada e Ajustes movidos para dentro de Empregos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEmpregos: () -> Unit,
    onNavigateToEmpregoSettings: (Long) -> Unit,
    onNavigateToNovoEmprego: () -> Unit,
    onNavigateToFeriados: () -> Unit,
    onNavigateToConfiguracoesGlobais: () -> Unit,
    onNavigateToSobre: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState()

    // Coletar eventos
    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is SettingsEvent.MostrarMensagem -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }
                is SettingsEvent.NavegarParaEmprego -> {
                    onNavigateToEmpregoSettings(evento.empregoId)
                }
                is SettingsEvent.NavegarParaNovoEmprego -> {
                    onNavigateToNovoEmprego()
                }
            }
        }
    }

    // Fechar bottom sheet ao mudar de emprego
    LaunchedEffect(uiState.empregoAtivo?.emprego?.id) {
        if (uiState.mostrarSeletorEmprego) {
            scope.launch { bottomSheetState.hide() }
            viewModel.onAction(SettingsAction.FecharSeletorEmprego)
        }
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Configurações",
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

            uiState.isPrimeiroAcesso -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    EmptyStateNoEmpregos(
                        onCriarEmprego = onNavigateToNovoEmprego
                    )
                }
            }

            else -> {
                SettingsContent(
                    uiState = uiState,
                    onTrocarEmprego = { viewModel.onAction(SettingsAction.AbrirSeletorEmprego) },
                    onNavigateToEmpregoSettings = {
                        uiState.empregoAtivo?.let { onNavigateToEmpregoSettings(it.emprego.id) }
                    },
                    onNavigateToEmpregos = onNavigateToEmpregos,
                    onNavigateToFeriados = onNavigateToFeriados,
                    onNavigateToConfiguracoesGlobais = onNavigateToConfiguracoesGlobais,
                    onNavigateToSobre = onNavigateToSobre,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }

    // Bottom Sheet de seleção de emprego
    if (uiState.mostrarSeletorEmprego) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onAction(SettingsAction.FecharSeletorEmprego) },
            sheetState = bottomSheetState
        ) {
            EmpregoSelectorBottomSheet(
                empregoAtivo = uiState.empregoAtivo,
                outrosEmpregos = uiState.outrosEmpregos,
                onSelecionarEmprego = { empregoId ->
                    viewModel.onAction(SettingsAction.TrocarEmpregoAtivo(empregoId))
                },
                onNovoEmprego = {
                    viewModel.onAction(SettingsAction.FecharSeletorEmprego)
                    onNavigateToNovoEmprego()
                },
                onGerenciarEmpregos = {
                    viewModel.onAction(SettingsAction.FecharSeletorEmprego)
                    onNavigateToEmpregos()
                }
            )
        }
    }
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onTrocarEmprego: () -> Unit,
    onNavigateToEmpregoSettings: () -> Unit,
    onNavigateToEmpregos: () -> Unit,
    onNavigateToFeriados: () -> Unit,
    onNavigateToConfiguracoesGlobais: () -> Unit,
    onNavigateToSobre: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ═══════════════════════════════════════════════════════════════
        // SEÇÃO: EMPREGOS
        // ═══════════════════════════════════════════════════════════════
        item(key = "section_empregos") {
            SettingsSectionCard(
                titulo = "Empregos",
                icon = Icons.Default.Business
            ) {
                // Card do Emprego Ativo
                uiState.empregoAtivo?.let { resumo ->
                    EmpregoAtivoCardCompact(
                        resumo = resumo,
                        totalOutrosEmpregos = uiState.outrosEmpregos.size,
                        onTrocarEmprego = onTrocarEmprego,
                        onAbrirConfiguracoes = onNavigateToEmpregoSettings
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Gerenciar Empregos
                SettingsItemClickable(
                    icon = Icons.Default.Business,
                    titulo = "Gerenciar Empregos",
                    subtitulo = "Adicionar, editar ou arquivar",
                    badge = uiState.empregosArquivados.size.takeIf { it > 0 }
                        ?.let { "$it arquivados" },
                    onClick = onNavigateToEmpregos
                )
            }
        }

        // ═══════════════════════════════════════════════════════════════
        // SEÇÃO: CALENDÁRIO
        // ═══════════════════════════════════════════════════════════════
        item(key = "section_calendario") {
            SettingsSectionCard(
                titulo = "Calendário",
                icon = Icons.Default.Event
            ) {
                SettingsItemClickable(
                    icon = Icons.Default.Event,
                    titulo = "Feriados",
                    subtitulo = "Gerenciar feriados e pontes facultativas",
                    badge = uiState.totalFeriadosGlobais.takeIf { it > 0 }?.let { "$it" },
                    onClick = onNavigateToFeriados
                )
            }
        }

        // ═══════════════════════════════════════════════════════════════
        // SEÇÃO: APLICATIVO
        // ═══════════════════════════════════════════════════════════════
        item(key = "section_app") {
            SettingsSectionCard(
                titulo = "Aplicativo",
                icon = Icons.Default.Settings
            ) {
                SettingsItemClickable(
                    icon = Icons.Default.Palette,
                    titulo = "Configurações Globais",
                    subtitulo = "Aparência, formatos, notificações e backup",
                    onClick = onNavigateToConfiguracoesGlobais
                )

                SettingsItemClickable(
                    icon = Icons.Default.Info,
                    titulo = "Sobre",
                    subtitulo = "Versão ${uiState.versaoFormatada}",
                    onClick = onNavigateToSobre
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
private fun SettingsSectionCard(
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
private fun EmpregoAtivoCardCompact(
    resumo: EmpregoResumo,
    totalOutrosEmpregos: Int,
    onTrocarEmprego: () -> Unit,
    onAbrirConfiguracoes: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onAbrirConfiguracoes,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = resumo.emprego.nome,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (totalOutrosEmpregos > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            onClick = onTrocarEmprego,
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.secondary
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Trocar emprego",
                                    tint = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+$totalOutrosEmpregos",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Jornada: ${resumo.cargaHorariaFormatada}/dia • ${resumo.totalVersoes} versões",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Configurações do emprego",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
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

/**
 * Bottom Sheet para seleção de emprego.
 */
@Composable
private fun EmpregoSelectorBottomSheet(
    empregoAtivo: EmpregoResumo?,
    outrosEmpregos: List<EmpregoResumo>,
    onSelecionarEmprego: (Long) -> Unit,
    onNovoEmprego: () -> Unit,
    onGerenciarEmpregos: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Selecionar Emprego",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Emprego ativo
        empregoAtivo?.let { resumo ->
            EmpregoSelectorItem(
                resumo = resumo,
                isAtivo = true,
                onClick = { /* Já é o ativo */ }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Outros empregos
        outrosEmpregos.forEach { resumo ->
            EmpregoSelectorItem(
                resumo = resumo,
                isAtivo = false,
                onClick = { onSelecionarEmprego(resumo.emprego.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ações
        Surface(
            onClick = onNovoEmprego,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(
                    text = "Novo Emprego",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onGerenciarEmpregos,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Gerenciar todos os empregos")
        }
    }
}
