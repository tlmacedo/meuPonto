package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.configuracoes

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import kotlinx.coroutines.flow.collectLatest

@Composable
fun OpcoesRegistroScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OpcoesRegistroViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is OpcoesRegistroEvent.SalvoComSucesso -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }
                OpcoesRegistroEvent.Voltar -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Opções de Registro",
                subtitle = uiState.apelidoEmprego ?: uiState.nomeEmprego,
                showBackButton = true,
                onBackClick = { viewModel.onAction(OpcoesRegistroAction.Voltar) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CircularProgressIndicator()
            }
        } else {
            OpcoesRegistroContent(
                uiState = uiState,
                onAction = viewModel::onAction,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun OpcoesRegistroContent(
    uiState: OpcoesRegistroUiState,
    onAction: (OpcoesRegistroAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // 🔢 NSR
        item {
            ConfigSection(
                title = "Número Sequencial de Registro (NSR)",
                icon = Icons.Default.Numbers
            ) {
                SwitchOption(
                    title = "Habilitar NSR",
                    description = "Exibir campo para informar o NSR do comprovante",
                    checked = uiState.habilitarNsr,
                    onCheckedChange = { onAction(OpcoesRegistroAction.AlterarHabilitarNsr(it)) }
                )

                AnimatedVisibility(visible = uiState.habilitarNsr) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            text = "Tipo de NSR",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        TipoNsr.values().forEach { tipo ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                RadioButton(
                                    selected = uiState.tipoNsr == tipo,
                                    onClick = { onAction(OpcoesRegistroAction.AlterarTipoNsr(tipo)) }
                                )
                                Text(
                                    text = when (tipo) {
                                        TipoNsr.NUMERICO -> "Numérico"
                                        TipoNsr.ALFANUMERICO -> "Alfanumérico"
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 📍 LOCALIZAÇÃO
        item {
            ConfigSection(
                title = "Localização",
                icon = Icons.Default.LocationOn
            ) {
                SwitchOption(
                    title = "Habilitar Localização",
                    description = "Vincular coordenadas geográficas ao registro",
                    checked = uiState.habilitarLocalizacao,
                    onCheckedChange = { onAction(OpcoesRegistroAction.AlterarHabilitarLocalizacao(it)) }
                )

                AnimatedVisibility(visible = uiState.habilitarLocalizacao) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                        SwitchOption(
                            title = "Captura Automática",
                            description = "Obter localização sem intervenção do usuário",
                            checked = uiState.localizacaoAutomatica,
                            onCheckedChange = { onAction(OpcoesRegistroAction.AlterarLocalizacaoAutomatica(it)) }
                        )
                        SwitchOption(
                            title = "Exibir Detalhes",
                            description = "Mostrar endereço aproximado no registro",
                            checked = uiState.exibirLocalizacaoDetalhes,
                            onCheckedChange = { onAction(OpcoesRegistroAction.AlterarExibirLocalizacaoDetalhes(it)) }
                        )
                    }
                }
            }
        }

        // 📸 FOTO
        item {
            ConfigSection(
                title = "Foto e Comprovante",
                icon = Icons.Default.CameraAlt
            ) {
                SwitchOption(
                    title = "Habilitar Foto",
                    description = "Permitir anexar foto ao registro",
                    checked = uiState.fotoHabilitada,
                    onCheckedChange = { onAction(OpcoesRegistroAction.AlterarFotoHabilitada(it)) }
                )

                AnimatedVisibility(visible = uiState.fotoHabilitada) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                        SwitchOption(
                            title = "Foto Obrigatória",
                            description = "Impedir registro sem foto",
                            checked = uiState.fotoObrigatoria,
                            onCheckedChange = { onAction(OpcoesRegistroAction.AlterarFotoObrigatoria(it)) }
                        )
                        SwitchOption(
                            title = "Validar Comprovante",
                            description = "Usar OCR para validar dados do comprovante",
                            checked = uiState.fotoValidarComprovante,
                            onCheckedChange = { onAction(OpcoesRegistroAction.AlterarFotoValidarComprovante(it)) }
                        )
                    }
                }
            }
        }

        // 💬 COMENTÁRIOS
        item {
            ConfigSection(
                title = "Comentários",
                icon = Icons.AutoMirrored.Filled.Comment
            ) {
                SwitchOption(
                    title = "Habilitar Comentários",
                    description = "Permitir adicionar notas aos registros",
                    checked = uiState.comentarioHabilitado,
                    onCheckedChange = { onAction(OpcoesRegistroAction.AlterarComentarioHabilitado(it)) }
                )

                AnimatedVisibility(visible = uiState.comentarioHabilitado) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        SwitchOption(
                            title = "Obrigatório em Hora Extra",
                            description = "Exigir justificativa para registros fora do horário",
                            checked = uiState.comentarioObrigatorioHoraExtra,
                            onCheckedChange = { onAction(OpcoesRegistroAction.AlterarComentarioObrigatorioHoraExtra(it)) }
                        )
                    }
                }
            }
        }

        // 👁️ EXIBIÇÃO
        item {
            ConfigSection(
                title = "Opções de Exibição",
                icon = Icons.Default.Visibility
            ) {
                SwitchOption(
                    title = "Duração do Turno",
                    description = "Exibir tempo decorrido no turno atual",
                    checked = uiState.exibirDuracaoTurno,
                    onCheckedChange = { onAction(OpcoesRegistroAction.AlterarExibirDuracaoTurno(it)) }
                )
                SwitchOption(
                    title = "Duração do Intervalo",
                    description = "Exibir tempo decorrido no intervalo atual",
                    checked = uiState.exibirDuracaoIntervalo,
                    onCheckedChange = { onAction(OpcoesRegistroAction.AlterarExibirDuracaoIntervalo(it)) }
                )
            }
        }

        // BOTÃO SALVAR
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onAction(OpcoesRegistroAction.Salvar) },
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Salvar Configurações")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ConfigSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            content()
        }
    }
}

@Composable
private fun SwitchOption(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
