package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.configuracao

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.theme.MeuPontoTheme
import kotlinx.coroutines.flow.collectLatest

/**
 * Tela de configuração geral do emprego.
 *
 * Organizada em duas seções principais:
 *
 * **Info RH:**
 * - Primeiro dia do mês para o RH (fechamento)
 * - Banco de horas (habilitado/desabilitado)
 *   - Tempo de ciclo
 *   - Início do ciclo atual
 *   - Zerar saldo no fechamento
 *
 * **Info Extra:**
 * - NSR (Número Sequencial de Registro)
 *   - Tipo: Numérico ou Alfanumérico
 * - Registrar Localização
 *   - Captura automática
 * - Foto de Comprovante
 *   - Foto obrigatória
 *   - Configurações de tamanho/qualidade
 *   - Registrar ponto por OCR
 * - Exigir Justificativa
 *
 * @author Thiago
 * @since 29.0.0
 */
@Composable
fun ConfiguracaoGeralEmpregoScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConfiguracaoGeralEmpregoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is ConfiguracaoGeralEvent.SalvoComSucesso -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }
                is ConfiguracaoGeralEvent.MostrarErro -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }
                is ConfiguracaoGeralEvent.Voltar -> onNavigateBack()
            }
        }
    }

    ConfiguracaoGeralEmpregoContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@Composable
fun ConfiguracaoGeralEmpregoContent(
    uiState: ConfiguracaoGeralUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (ConfiguracaoGeralAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Configuração Geral",
                subtitle = uiState.nomeEmprego,
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                // ══════════════════════════════════════════════════════════════
                // SEÇÃO: INFO RH
                // ══════════════════════════════════════════════════════════════
                item {
                    SecaoHeader(
                        icon = Icons.Default.AccountBalance,
                        title = "Info RH"
                    )
                }

                item {
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Dia de fechamento RH
                            OutlinedTextField(
                                value = uiState.diaFechamentoRhStr,
                                onValueChange = { onAction(ConfiguracaoGeralAction.AlterarDiaFechamentoRH(it)) },
                                label = { Text("Primeiro dia do mês para o RH") },
                                placeholder = { Text("Ex: 11") },
                                supportingText = {
                                    Text(
                                        text = if (uiState.diaFechamentoRhStr.isNotBlank()) {
                                            val dia = uiState.diaFechamentoRhStr.toIntOrNull()
                                            if (dia != null && dia in 1..28) {
                                                "O mês começa no dia $dia e termina no dia ${dia - 1} do mês seguinte"
                                            } else {
                                                "Informe um dia entre 1 e 28"
                                            }
                                        } else {
                                            "Dia em que começa o período de registro de pontos"
                                        },
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(16.dp))

                            // Banco de horas
                            SwitchRow(
                                icon = Icons.Default.AccountBalance,
                                title = "Banco de Horas",
                                subtitle = "Habilitar controle de banco de horas",
                                checked = uiState.bancoHorasHabilitado,
                                onCheckedChange = { onAction(ConfiguracaoGeralAction.ToggleBancoHoras(it)) }
                            )

                            AnimatedVisibility(
                                visible = uiState.bancoHorasHabilitado,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(4.dp))

                                    OutlinedTextField(
                                        value = uiState.bancoHorasCicloMesesStr,
                                        onValueChange = { onAction(ConfiguracaoGeralAction.AlterarCicloMeses(it)) },
                                        label = { Text("Tempo de ciclo (meses)") },
                                        placeholder = { Text("Ex: 6") },
                                        supportingText = { Text("Duração de cada ciclo do banco de horas") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = uiState.bancoHorasDataInicioCicloStr,
                                        onValueChange = { onAction(ConfiguracaoGeralAction.AlterarDataInicioCiclo(it)) },
                                        label = { Text("Início do ciclo atual (dd/MM/yyyy)") },
                                        placeholder = { Text("Ex: 11/02/2026") },
                                        supportingText = {
                                            val ciclo = uiState.bancoHorasCicloMesesStr.toIntOrNull()
                                            if (ciclo != null && uiState.bancoHorasDataInicioCicloStr.isNotBlank()) {
                                                Text("Próximo fechamento calculado automaticamente")
                                            } else {
                                                Text("Data de início do ciclo atual")
                                            }
                                        },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    SwitchRow(
                                        icon = Icons.Default.CalendarMonth,
                                        title = "Zerar saldo no fechamento",
                                        subtitle = "O banco de horas é zerado ao final de cada ciclo",
                                        checked = uiState.bancoHorasZerarAoFinalCiclo,
                                        onCheckedChange = { onAction(ConfiguracaoGeralAction.ToggleZerarSaldoCiclo(it)) }
                                    )
                                }
                            }
                        }
                    }
                }

                // ══════════════════════════════════════════════════════════════
                // SEÇÃO: INFO EXTRA
                // ══════════════════════════════════════════════════════════════
                item {
                    SecaoHeader(
                        icon = Icons.Default.Settings,
                        title = "Info Extra"
                    )
                }

                // NSR
                item {
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            SwitchRow(
                                icon = Icons.Default.Numbers,
                                title = "NSR - Número Sequencial de Registro",
                                subtitle = "Habilita campo NSR no momento do registro de ponto",
                                checked = uiState.habilitarNsr,
                                onCheckedChange = { onAction(ConfiguracaoGeralAction.ToggleNsr(it)) }
                            )

                            AnimatedVisibility(
                                visible = uiState.habilitarNsr,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column(modifier = Modifier.padding(top = 16.dp)) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Tipo de NSR",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    RadioOptionRow(
                                        selected = uiState.tipoNsr == TipoNsr.NUMERICO,
                                        label = "Numérico (0-9)",
                                        description = "O campo aceita apenas números",
                                        onClick = { onAction(ConfiguracaoGeralAction.AlterarTipoNsr(TipoNsr.NUMERICO)) }
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    RadioOptionRow(
                                        selected = uiState.tipoNsr == TipoNsr.ALFANUMERICO,
                                        label = "Alfanumérico (letras e números)",
                                        description = "O campo aceita letras e números",
                                        onClick = { onAction(ConfiguracaoGeralAction.AlterarTipoNsr(TipoNsr.ALFANUMERICO)) }
                                    )
                                }
                            }
                        }
                    }
                }

                // LOCALIZAÇÃO
                item {
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            SwitchRow(
                                icon = Icons.Default.LocationOn,
                                title = "Registrar Localização",
                                subtitle = "Captura a localização GPS no momento do registro",
                                checked = uiState.habilitarLocalizacao,
                                onCheckedChange = { onAction(ConfiguracaoGeralAction.ToggleLocalizacao(it)) }
                            )

                            AnimatedVisibility(
                                visible = uiState.habilitarLocalizacao,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column(modifier = Modifier.padding(top = 16.dp)) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    SwitchRow(
                                        icon = Icons.Default.LocationOn,
                                        title = "Captura automática",
                                        subtitle = "Obtém a localização automaticamente sem interação do usuário",
                                        checked = uiState.localizacaoAutomatica,
                                        onCheckedChange = { onAction(ConfiguracaoGeralAction.ToggleLocalizacaoAutomatica(it)) }
                                    )
                                }
                            }
                        }
                    }
                }

                // FOTO DE COMPROVANTE
                item {
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            SwitchRow(
                                icon = Icons.Default.PhotoCamera,
                                title = "Foto de Comprovante",
                                subtitle = "Permite tirar foto do comprovante de ponto",
                                checked = uiState.fotoHabilitada,
                                onCheckedChange = { onAction(ConfiguracaoGeralAction.ToggleFoto(it)) }
                            )

                            AnimatedVisibility(
                                visible = uiState.fotoHabilitada,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(4.dp))

                                    SwitchRow(
                                        icon = Icons.Default.Camera,
                                        title = "Foto obrigatória",
                                        subtitle = "Exige foto para concluir o registro de ponto",
                                        checked = uiState.fotoObrigatoria,
                                        onCheckedChange = { onAction(ConfiguracaoGeralAction.ToggleFotoObrigatoria(it)) }
                                    )

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                                    // Qualidade
                                    Text(
                                        text = "Qualidade da imagem: ${uiState.fotoQualidade}%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    OutlinedTextField(
                                        value = uiState.fotoQualidadeStr,
                                        onValueChange = { onAction(ConfiguracaoGeralAction.AlterarFotoQualidade(it)) },
                                        label = { Text("Qualidade (60-100)") },
                                        supportingText = { Text("Qualidade de compressão JPEG") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Resolução
                                    OutlinedTextField(
                                        value = uiState.fotoResolucaoStr,
                                        onValueChange = { onAction(ConfiguracaoGeralAction.AlterarFotoResolucao(it)) },
                                        label = { Text("Resolução máxima (pixels)") },
                                        supportingText = { Text("Largura máxima em pixels. 0 = sem limite") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Tamanho máximo
                                    OutlinedTextField(
                                        value = uiState.fotoTamanhoMaximoStr,
                                        onValueChange = { onAction(ConfiguracaoGeralAction.AlterarFotoTamanhoMaximo(it)) },
                                        label = { Text("Tamanho máximo (KB)") },
                                        supportingText = { Text("Tamanho máximo do arquivo. 0 = sem limite") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                                    SwitchRow(
                                        icon = Icons.Default.Camera,
                                        title = "Registrar ponto pela foto (OCR)",
                                        subtitle = "Identifica campos na imagem e registra o ponto automaticamente",
                                        checked = uiState.fotoRegistrarPontoOcr,
                                        onCheckedChange = { onAction(ConfiguracaoGeralAction.ToggleFotoOcr(it)) }
                                    )
                                }
                            }
                        }
                    }
                }

                // JUSTIFICATIVA
                item {
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            SwitchRow(
                                icon = Icons.Default.Warning,
                                title = "Exigir Justificativa",
                                subtitle = "Solicita justificativa ao registrar ponto que descumpra as regras de jornada",
                                checked = uiState.exigeJustificativa,
                                onCheckedChange = { onAction(ConfiguracaoGeralAction.ToggleJustificativa(it)) }
                            )

                            AnimatedVisibility(
                                visible = uiState.exigeJustificativa,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                                        ),
                                        shape = MaterialTheme.shapes.medium
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Exemplos: turno acima de 6h, jornada acima do máximo de 10h, " +
                                                        "intervalo menor que 1h para jornada de 8h, ou início de jornada " +
                                                        "com menos de 11h após o fim da última.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // BOTÃO SALVAR
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onAction(ConfiguracaoGeralAction.Salvar) },
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Salvar Configurações")
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfiguracaoGeralEmpregoPreview() {
    val uiState = ConfiguracaoGeralUiState(
        isLoading = false,
        nomeEmprego = "Minha Empresa LTDA",
        diaFechamentoRhStr = "21",
        bancoHorasHabilitado = true,
        bancoHorasCicloMesesStr = "6",
        bancoHorasDataInicioCicloStr = "01/01/2024",
        bancoHorasZerarAoFinalCiclo = true,
        habilitarNsr = true,
        tipoNsr = TipoNsr.NUMERICO,
        habilitarLocalizacao = true,
        localizacaoAutomatica = true,
        fotoHabilitada = true,
        fotoObrigatoria = false,
        fotoQualidadeStr = "85",
        fotoResolucaoStr = "1920",
        fotoTamanhoMaximoStr = "1024",
        fotoRegistrarPontoOcr = true,
        exigeJustificativa = true
    )

    MeuPontoTheme {
        ConfiguracaoGeralEmpregoContent(
            uiState = uiState,
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfiguracaoGeralEmpregoDisabledPreview() {
    val uiState = ConfiguracaoGeralUiState(
        isLoading = false,
        nomeEmprego = "Minha Empresa LTDA",
        diaFechamentoRhStr = "1",
        bancoHorasHabilitado = false,
        habilitarNsr = false,
        habilitarLocalizacao = false,
        fotoHabilitada = false,
        exigeJustificativa = false
    )

    MeuPontoTheme {
        ConfiguracaoGeralEmpregoContent(
            uiState = uiState,
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
            onNavigateBack = {}
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// COMPONENTES INTERNOS
// ════════════════════════════════════════════════════════════════════════════════

@Composable
private fun SecaoHeader(
    icon: ImageVector,
    title: String,
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
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun RadioOptionRow(
    selected: Boolean,
    label: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
