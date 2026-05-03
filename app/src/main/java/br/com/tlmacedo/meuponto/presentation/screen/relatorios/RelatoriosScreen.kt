package br.com.tlmacedo.meuponto.presentation.screen.relatorios

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatoriosScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: RelatoriosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    val mesFormatter = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale("pt", "BR"))

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let {
            val outputStream = context.contentResolver.openOutputStream(it)
            if (outputStream != null) {
                viewModel.onAction(RelatoriosAction.ExportarPDF(outputStream))
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { event ->
            when (event) {
                is RelatoriosEvent.MostrarMensagem -> snackbarHostState.showSnackbar(event.mensagem)
                RelatoriosEvent.ExportacaoConcluida -> { /* Fechar diálogos se houver */ }
            }
        }
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Relatórios",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Escolha o período",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(16.dp))

            // Seletor de Mês
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { viewModel.onAction(RelatoriosAction.AlterarMes(-1)) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBackIos, null)
                }
                
                Text(
                    text = uiState.mesSelecionado.format(mesFormatter).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                IconButton(onClick = { viewModel.onAction(RelatoriosAction.AlterarMes(1)) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null)
                }
            }

            Spacer(Modifier.height(48.dp))

            // Opções de Exportação
            Text(
                text = "Formatos disponíveis",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { 
                    val fileName = "Espelho_Ponto_${uiState.mesSelecionado}.pdf"
                    pdfLauncher.launch(fileName)
                },
                enabled = !uiState.isExportando
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Description, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Espelho de Ponto (PDF)", style = MaterialTheme.typography.titleSmall)
                        Text("Relatório oficial formatado para impressão.", style = MaterialTheme.typography.bodySmall)
                    }
                    if (uiState.isExportando) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.FileDownload, null)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.onAction(RelatoriosAction.AlterarMes(0)) }, // Placeholder
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Exportar CSV", style = MaterialTheme.typography.titleSmall)
                        Text("Ideal para abrir no Excel ou Planilhas Google.", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("BREVE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
