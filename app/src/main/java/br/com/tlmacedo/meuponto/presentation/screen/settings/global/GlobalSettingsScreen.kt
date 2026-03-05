// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/global/GlobalSettingsScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.global

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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.PreferenciasGlobais
import br.com.tlmacedo.meuponto.domain.model.PreferenciasGlobais.FormatoData
import br.com.tlmacedo.meuponto.domain.model.PreferenciasGlobais.FormatoHora
import br.com.tlmacedo.meuponto.domain.model.PreferenciasGlobais.TemaEscuro
import kotlinx.coroutines.flow.collectLatest
import java.time.DayOfWeek

/**
 * Tela de configurações globais do aplicativo.
 *
 * @author Thiago
 * @since 8.1.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GlobalSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Coletar eventos
    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is GlobalSettingsEvent.Voltar -> onNavigateBack()
                is GlobalSettingsEvent.MostrarSucesso -> snackbarHostState.showSnackbar(evento.mensagem)
                is GlobalSettingsEvent.MostrarErro -> snackbarHostState.showSnackbar(evento.mensagem)
            }
        }
    }

    // Mostrar mensagens
    LaunchedEffect(uiState.mensagemSucesso, uiState.mensagemErro) {
        uiState.mensagemSucesso?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onAction(GlobalSettingsAction.LimparMensagem)
        }
        uiState.mensagemErro?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onAction(GlobalSettingsAction.LimparMensagem)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações Globais") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            GlobalSettingsContent(
                uiState = uiState,
                onAction = viewModel::onAction,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }

    // Diálogos
    uiState.dialogAtivo?.let { tipo ->
        when (tipo) {
            DialogTipo.TEMA -> TemaDialog(
                temaAtual = uiState.preferencias.temaEscuro,
                onSelecionarTema = { viewModel.onAction(GlobalSettingsAction.AlterarTema(it)) },
                onDismiss = { viewModel.onAction(GlobalSettingsAction.FecharDialog) }
            )
            DialogTipo.FORMATO_DATA -> FormatoDataDialog(
                formatoAtual = uiState.preferencias.formatoData,
                onSelecionarFormato = { viewModel.onAction(GlobalSettingsAction.AlterarFormatoData(it)) },
                onDismiss = { viewModel.onAction(GlobalSettingsAction.FecharDialog) }
            )
            DialogTipo.FORMATO_HORA -> FormatoHoraDialog(
                formatoAtual = uiState.preferencias.formatoHora,
                onSelecionarFormato = { viewModel.onAction(GlobalSettingsAction.AlterarFormatoHora(it)) },
                onDismiss = { viewModel.onAction(GlobalSettingsAction.FecharDialog) }
            )
            DialogTipo.PRIMEIRO_DIA_SEMANA -> PrimeiroDiaSemanaDialog(
                diaAtual = uiState.preferencias.primeiroDiaSemana,
                onSelecionarDia = { viewModel.onAction(GlobalSettingsAction.AlterarPrimeiroDiaSemana(it)) },
                onDismiss = { viewModel.onAction(GlobalSettingsAction.FecharDialog) }
            )
            else -> {}
        }
    }
}

@Composable
private fun GlobalSettingsContent(
    uiState: GlobalSettingsUiState,
    onAction: (GlobalSettingsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs = uiState.preferencias

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Seção: Aparência
        item {
            SettingsSection(
                titulo = "Aparência",
                icon = Icons.Default.Palette
            ) {
                // Tema
                SettingsItemClickable(
                    titulo = "Tema",
                    valor = prefs.temaEscuro.descricao,
                    onClick = { onAction(GlobalSettingsAction.AbrirDialog(DialogTipo.TEMA)) }
                )
                
                // Cores do sistema
                SettingsItemSwitch(
                    titulo = "Usar cores do sistema",
                    subtitulo = "Dynamic Colors (Android 12+)",
                    checked = prefs.usarCoresDoSistema,
                    onCheckedChange = { onAction(GlobalSettingsAction.AlterarCoresSistema(it)) }
                )
            }
        }

        // Seção: Formatos
        item {
            SettingsSection(
                titulo = "Formatos",
                icon = Icons.Default.DateRange
            ) {
                SettingsItemClickable(
                    titulo = "Formato de data",
                    valor = "${prefs.formatoData.descricao} (${prefs.formatoData.exemplo})",
                    onClick = { onAction(GlobalSettingsAction.AbrirDialog(DialogTipo.FORMATO_DATA)) }
                )
                
                SettingsItemClickable(
                    titulo = "Formato de hora",
                    valor = "${prefs.formatoHora.descricao} (${prefs.formatoHora.exemplo})",
                    onClick = { onAction(GlobalSettingsAction.AbrirDialog(DialogTipo.FORMATO_HORA)) }
                )
                
                SettingsItemClickable(
                    titulo = "Primeiro dia da semana",
                    valor = prefs.primeiroDiaSemanaDescricao,
                    onClick = { onAction(GlobalSettingsAction.AbrirDialog(DialogTipo.PRIMEIRO_DIA_SEMANA)) }
                )
            }
        }

        // Seção: Notificações
        item {
            SettingsSection(
                titulo = "Notificações",
                icon = Icons.Default.Notifications
            ) {
                SettingsItemSwitch(
                    titulo = "Lembrete de ponto",
                    subtitulo = "Notificar horários de entrada/saída",
                    checked = prefs.lembretePontoAtivo,
                    onCheckedChange = { onAction(GlobalSettingsAction.AlterarLembretePonto(it)) }
                )
                
                SettingsItemSwitch(
                    titulo = "Alerta de feriado",
                    subtitulo = "Avisar com ${prefs.antecedenciaAlertaFeriadoDias} dias de antecedência",
                    checked = prefs.alertaFeriadoAtivo,
                    onCheckedChange = { onAction(GlobalSettingsAction.AlterarAlertaFeriado(it)) }
                )
                
                if (prefs.alertaFeriadoAtivo) {
                    SettingsItemSlider(
                        titulo = "Antecedência do alerta",
                        valor = prefs.antecedenciaAlertaFeriadoDias,
                        valorFormatado = "${prefs.antecedenciaAlertaFeriadoDias} dias",
                        range = PreferenciasGlobais.ANTECEDENCIA_FERIADO_MIN.toFloat()..
                                PreferenciasGlobais.ANTECEDENCIA_FERIADO_MAX.toFloat(),
                        onValueChange = { onAction(GlobalSettingsAction.AlterarAntecedenciaFeriado(it.toInt())) }
                    )
                }
                
                SettingsItemSwitch(
                    titulo = "Alerta de banco de horas",
                    subtitulo = "Avisar quando acumular horas extras/devidas",
                    checked = prefs.alertaBancoHorasAtivo,
                    onCheckedChange = { onAction(GlobalSettingsAction.AlterarAlertaBancoHoras(it)) }
                )
            }
        }

        // Seção: Localização
        item {
            SettingsSection(
                titulo = "Localização",
                icon = Icons.Default.LocationOn
            ) {
                SettingsItemClickable(
                    titulo = "Local de trabalho padrão",
                    valor = prefs.localizacaoResumo,
                    onClick = { onAction(GlobalSettingsAction.AbrirDialog(DialogTipo.LOCALIZACAO)) }
                )
                
                if (prefs.temLocalizacaoPadrao) {
                    SettingsItemSlider(
                        titulo = "Raio de geofencing",
                        valor = prefs.raioGeofencingMetros,
                        valorFormatado = "${prefs.raioGeofencingMetros}m",
                        range = PreferenciasGlobais.RAIO_GEOFENCING_MIN.toFloat()..
                                PreferenciasGlobais.RAIO_GEOFENCING_MAX.toFloat(),
                        onValueChange = { onAction(GlobalSettingsAction.AlterarRaioGeofencing(it.toInt())) }
                    )
                    
                    SettingsItemSwitch(
                        titulo = "Registro automático",
                        subtitulo = "Registrar ponto ao entrar/sair da área",
                        checked = prefs.registroAutomaticoGeofencing,
                        onCheckedChange = { onAction(GlobalSettingsAction.AlterarRegistroAutomatico(it)) }
                    )
                }
            }
        }

        // Seção: Backup
        item {
            SettingsSection(
                titulo = "Backup",
                icon = Icons.Default.Backup
            ) {
                SettingsItemSwitch(
                    titulo = "Backup automático",
                    subtitulo = "Último: ${uiState.ultimoBackupFormatado}",
                    checked = prefs.backupAutomaticoAtivo,
                    onCheckedChange = { onAction(GlobalSettingsAction.AlterarBackupAutomatico(it)) }
                )
            }
        }

        // Espaço final
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

// ============================================================================
// Componentes de Seção
// ============================================================================

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
private fun SettingsItemClickable(
    titulo: String,
    valor: String,
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun SettingsItemSwitch(
    titulo: String,
    subtitulo: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitulo != null) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsItemSlider(
    titulo: String,
    valor: Int,
    valorFormatado: String,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = valorFormatado,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
        Slider(
            value = valor.toFloat(),
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ============================================================================
// Diálogos
// ============================================================================

@Composable
private fun TemaDialog(
    temaAtual: TemaEscuro,
    onSelecionarTema: (TemaEscuro) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecionar Tema") },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                TemaEscuro.entries.forEach { tema ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = tema == temaAtual,
                                onClick = { onSelecionarTema(tema) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tema == temaAtual,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(tema.descricao)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
private fun FormatoDataDialog(
    formatoAtual: FormatoData,
    onSelecionarFormato: (FormatoData) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Formato de Data") },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                FormatoData.entries.forEach { formato ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = formato == formatoAtual,
                                onClick = { onSelecionarFormato(formato) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = formato == formatoAtual,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(formato.descricao)
                            Text(
                                text = "Ex: ${formato.exemplo}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
private fun FormatoHoraDialog(
    formatoAtual: FormatoHora,
    onSelecionarFormato: (FormatoHora) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Formato de Hora") },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                FormatoHora.entries.forEach { formato ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = formato == formatoAtual,
                                onClick = { onSelecionarFormato(formato) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = formato == formatoAtual,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(formato.descricao)
                            Text(
                                text = "Ex: ${formato.exemplo}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
private fun PrimeiroDiaSemanaDialog(
    diaAtual: DayOfWeek,
    onSelecionarDia: (DayOfWeek) -> Unit,
    onDismiss: () -> Unit
) {
    val diasComuns = listOf(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.SATURDAY)
    val nomesDias = mapOf(
        DayOfWeek.SUNDAY to "Domingo",
        DayOfWeek.MONDAY to "Segunda-feira",
        DayOfWeek.SATURDAY to "Sábado"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Primeiro Dia da Semana") },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                diasComuns.forEach { dia ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = dia == diaAtual,
                                onClick = { onSelecionarDia(dia) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = dia == diaAtual,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(nomesDias[dia] ?: dia.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}
