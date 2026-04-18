package br.com.tlmacedo.meuponto.presentation.screen.settings.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.repository.CloudFile
import br.com.tlmacedo.meuponto.domain.repository.LocalBackupFile
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Tela de gerenciamento de backup e dados.
 *
 * Permite ao usuário:
 * - Exportar backup completo (JSON)
 * - Importar backup
 * - Limpar dados antigos
 * - Visualizar estatísticas do banco
 *
 * @author Thiago
 * @since 9.0.0
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Launchers para SAF
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/x-sqlite3")
    ) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.let { outputStream ->
                viewModel.onAction(BackupAction.ExportarBackup(outputStream))
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.let { inputStream ->
                viewModel.onAction(BackupAction.ImportarBackup(inputStream))
            }
        }
    }

    val googleAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // O ViewModel observa as mudanças de estado da conta via Repository
        viewModel.onGoogleAuthResult()
    }

    // Dialogs
    var showConfirmLimpeza by remember { mutableStateOf(false) }
    var backupParaRestaurar by remember { mutableStateOf<CloudFile?>(null) }
    var backupParaExcluirNuvem by remember { mutableStateOf<CloudFile?>(null) }
    var showConfirmExcluirTudoNuvem by remember { mutableStateOf(false) }
    var backupLocalParaRestaurar by remember { mutableStateOf<LocalBackupFile?>(null) }
    var showConfirmExcluirTudo by remember { mutableStateOf(false) }
    var backupLocalParaExcluir by remember { mutableStateOf<LocalBackupFile?>(null) }

    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is BackupEvent.MostrarMensagem -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }
                is BackupEvent.ExportacaoConcluida -> {
                    snackbarHostState.showSnackbar("Backup exportado com sucesso!")
                }
                is BackupEvent.ImportacaoConcluida -> {
                    snackbarHostState.showSnackbar("Backup importado com sucesso!")
                }
                is BackupEvent.LimpezaConcluida -> {
                    snackbarHostState.showSnackbar("${evento.registrosRemovidos} registros removidos")
                }
                is BackupEvent.SolicitarDestinoExportacao -> {
                    val fileName = "meuponto_backup_${
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
                    }.db"
                    exportLauncher.launch(fileName)
                }
                is BackupEvent.SolicitarOrigemImportacao -> {
                    importLauncher.launch(arrayOf("*/*")) // Permitir qualquer arquivo, o repo trata
                }
                is BackupEvent.SolicitarAutenticacaoGoogle -> {
                    @Suppress("DEPRECATION")
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                        .build()
                    @Suppress("DEPRECATION")
                    val client = GoogleSignIn.getClient(context, gso)
                    googleAuthLauncher.launch(client.signInIntent)
                }
            }
        }
    }

    // Dialog de confirmação de limpeza
    if (showConfirmLimpeza) {
        AlertDialog(
            onDismissRequest = { showConfirmLimpeza = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DeleteSweep,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Limpar Dados Antigos") },
            text = {
                Text(
                    "Esta ação removerá registros de ponto anteriores a ${uiState.mesesParaLimpeza} meses atrás.\n\n" +
                            "Recomendamos fazer um backup antes de prosseguir.\n\n" +
                            "Esta ação NÃO pode ser desfeita."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmLimpeza = false
                        viewModel.onAction(BackupAction.LimparDadosAntigos)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Limpar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmLimpeza = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog de confirmação de restauração da nuvem
    if (backupParaRestaurar != null) {
        AlertDialog(
            onDismissRequest = { backupParaRestaurar = null },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Restore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Restaurar Backup da Nuvem") },
            text = {
                val data = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(backupParaRestaurar!!.modifiedTime),
                    ZoneId.systemDefault()
                ).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                Text(
                    "Deseja restaurar o backup de $data?\n\n" +
                            "Isso substituirá todos os dados atuais do aplicativo por esta versão da nuvem.\n\n" +
                            "Esta ação NÃO pode ser desfeita."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onAction(BackupAction.RestaurarBackupNuvem(backupParaRestaurar!!.id))
                        backupParaRestaurar = null
                    }
                ) {
                    Text("Restaurar")
                }
            },
            dismissButton = {
                TextButton(onClick = { backupParaRestaurar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog de confirmação de exclusão de backup na nuvem
    if (backupParaExcluirNuvem != null) {
        AlertDialog(
            onDismissRequest = { backupParaExcluirNuvem = null },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DeleteSweep,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Excluir Backup na Nuvem") },
            text = {
                Text("Deseja excluir permanentemente o backup de ${LocalDateTime.ofInstant(Instant.ofEpochMilli(backupParaExcluirNuvem!!.modifiedTime), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))} da nuvem?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onAction(BackupAction.ExcluirBackupNuvem(backupParaExcluirNuvem!!.id))
                        backupParaExcluirNuvem = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { backupParaExcluirNuvem = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog de confirmação de exclusão de TODOS os backups na nuvem
    if (showConfirmExcluirTudoNuvem) {
        AlertDialog(
            onDismissRequest = { showConfirmExcluirTudoNuvem = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DeleteSweep,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Excluir Todos da Nuvem") },
            text = {
                Text("Deseja excluir permanentemente TODOS os backups deste emprego armazenados na nuvem?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmExcluirTudoNuvem = false
                        viewModel.onAction(BackupAction.ExcluirTodosBackupsNuvem)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir Tudo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmExcluirTudoNuvem = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog de confirmação de restauração local
    if (backupLocalParaRestaurar != null) {
        AlertDialog(
            onDismissRequest = { backupLocalParaRestaurar = null },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Restore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Restaurar Backup Local") },
            text = {
                val data = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(backupLocalParaRestaurar!!.dataCriacao),
                    ZoneId.systemDefault()
                ).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                Text(
                    "Deseja restaurar o backup interno de $data?\n\n" +
                            "Isso substituirá todos os dados atuais do aplicativo por este arquivo.\n\n" +
                            "Esta ação NÃO pode ser desfeita."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onAction(BackupAction.RestaurarBackupLocal(backupLocalParaRestaurar!!.nome))
                        backupLocalParaRestaurar = null
                    }
                ) {
                    Text("Restaurar")
                }
            },
            dismissButton = {
                TextButton(onClick = { backupLocalParaRestaurar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog de confirmação de exclusão de TODOS os backups locais
    if (showConfirmExcluirTudo) {
        AlertDialog(
            onDismissRequest = { showConfirmExcluirTudo = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DeleteSweep,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Excluir Todos os Backups") },
            text = {
                Text("Deseja excluir permanentemente todos os arquivos de backup local deste emprego?\n\nEsta ação não pode ser desfeita.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmExcluirTudo = false
                        viewModel.onAction(BackupAction.ExcluirTodosBackupsLocais)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir Tudo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmExcluirTudo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog de confirmação de exclusão de backup local específico
    if (backupLocalParaExcluir != null) {
        AlertDialog(
            onDismissRequest = { backupLocalParaExcluir = null },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DeleteSweep,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Excluir Backup") },
            text = {
                Text("Deseja excluir o arquivo '${backupLocalParaExcluir!!.nome}'?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onAction(BackupAction.ExcluirBackupLocal(backupLocalParaExcluir!!.nome))
                        backupLocalParaExcluir = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { backupLocalParaExcluir = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Backup e Dados",
                subtitle = (uiState.empregoApelido ?: uiState.empregoNome)?.uppercase(),
                logo = uiState.empregoLogo,
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ══════════════════════════════════════════════════════════════
            // ESTATÍSTICAS DO BANCO
            // ══════════════════════════════════════════════════════════════
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader(
                        title = "Estatísticas",
                        icon = Icons.Outlined.Storage
                    )
                    EstatisticasCard(
                        totalEmpregos = uiState.totalEmpregos,
                        totalPontos = uiState.totalPontos,
                        totalFeriados = uiState.totalFeriados,
                        tamanhoEstimado = uiState.tamanhoEstimado,
                        tamanhoBanco = uiState.tamanhoBanco,
                        tamanhoImagens = uiState.tamanhoImagens,
                        isLoading = uiState.isLoading
                    )
                }
            }

            // ══════════════════════════════════════════════════════════════
            // BACKUP NA NUVEM (Google Drive)
            // ══════════════════════════════════════════════════════════════
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader(
                        title = "Backup na Nuvem",
                        icon = Icons.Outlined.CloudSync
                    )

                    if (!uiState.isGoogleAutenticado) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Sincronize seus dados com o Google Drive",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Armazene seu banco de dados de forma privada no seu Google Drive para restaurar em outros dispositivos.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.onAction(BackupAction.AutenticarGoogle) },
                                    enabled = !uiState.isProcessando,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (uiState.isProcessando && uiState.operacaoAtual == "autenticar_google") {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Icon(Icons.Outlined.AccountCircle, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Conectar com Google")
                                }
                            }
                        }
                    } else {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Outlined.AccountCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Conectado como",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = uiState.contaGoogle ?: "Usuário Google",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    TextButton(onClick = { viewModel.onAction(BackupAction.DeslogarGoogle) }) {
                                        Text("Sair", color = MaterialTheme.colorScheme.error)
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Backup Automático",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Diário (Wi-Fi + Carregando)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = uiState.backupNuvemAtivo,
                                        onCheckedChange = { viewModel.onAction(BackupAction.ToggleBackupNuvem(it)) }
                                    )
                                }

                                uiState.ultimoBackupNuvem?.let { timestamp ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val data = LocalDateTime.ofInstant(
                                        Instant.ofEpochMilli(timestamp),
                                        ZoneId.systemDefault()
                                    ).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                                    Text(
                                        text = "Último backup na nuvem: $data",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (uiState.ultimoBackupNuvem != null)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { viewModel.onAction(BackupAction.BackupNuvemAgora) },
                                    enabled = !uiState.isProcessando,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (uiState.isProcessando && uiState.operacaoAtual == "backup_nuvem") {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Icon(Icons.Outlined.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Fazer Backup Agora")
                                }
                            }
                        }
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════
            // HISTÓRICO DE BACKUPS
            // ══════════════════════════════════════════════════════════════
            if (uiState.isGoogleAutenticado && uiState.backupsNuvem.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader(
                            title = "Histórico na Nuvem",
                            icon = Icons.Outlined.History
                        )
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { showConfirmExcluirTudoNuvem = true },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Limpar Todos Nuvem", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                uiState.backupsNuvem.take(5).forEachIndexed { index, backup ->
                                    val data = LocalDateTime.ofInstant(
                                        Instant.ofEpochMilli(backup.modifiedTime),
                                        ZoneId.systemDefault()
                                    ).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

                                    ListItem(
                                        headlineContent = { Text(data) },
                                        supportingContent = {
                                            Text("Tamanho compactado: ${backup.size / 1024} KB")
                                        },
                                        leadingContent = {
                                            Icon(
                                                Icons.Outlined.CloudDownload,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                            )
                                        },
                                        trailingContent = {
                                            Row {
                                                IconButton(onClick = { backupParaRestaurar = backup }) {
                                                    Icon(
                                                        Icons.Outlined.Restore,
                                                        contentDescription = "Restaurar",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                IconButton(onClick = { backupParaExcluirNuvem = backup }) {
                                                    Icon(
                                                        Icons.Outlined.DeleteSweep,
                                                        contentDescription = "Excluir",
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        },
                                        colors = ListItemDefaults.colors(
                                            containerColor = Color.Transparent
                                        )
                                    )
                                    if (index < uiState.backupsNuvem.size - 1 && index < 4) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════
            // HISTÓRICO LOCAL (BACKUPS INTERNOS)
            // ══════════════════════════════════════════════════════════════
            if (uiState.backupsLocais.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader(
                            title = "Backups Internos",
                            icon = Icons.Outlined.History
                        )
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { showConfirmExcluirTudo = true },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Limpar Todos", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                uiState.backupsLocais.take(5).forEachIndexed { index, backup ->
                                    val data = LocalDateTime.ofInstant(
                                        Instant.ofEpochMilli(backup.dataCriacao),
                                        ZoneId.systemDefault()
                                    ).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

                                    ListItem(
                                        headlineContent = { Text(data) },
                                        supportingContent = {
                                            Text("${backup.tamanho / 1024} KB")
                                        },
                                        leadingContent = {
                                            Icon(
                                                Icons.Outlined.Storage,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                                            )
                                        },
                                        trailingContent = {
                                            Row {
                                                IconButton(onClick = { backupLocalParaRestaurar = backup }) {
                                                    Icon(
                                                        Icons.Outlined.Restore,
                                                        contentDescription = "Restaurar",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                IconButton(onClick = { backupLocalParaExcluir = backup }) {
                                                    Icon(
                                                        Icons.Outlined.DeleteSweep,
                                                        contentDescription = "Excluir",
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        },
                                        colors = ListItemDefaults.colors(
                                            containerColor = Color.Transparent
                                        )
                                    )
                                    if (index < uiState.backupsLocais.size - 1 && index < 4) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════
            // EXPORTAR BACKUP
            // ══════════════════════════════════════════════════════════════
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader(
                        title = "Exportar para Arquivo Externo",
                        icon = Icons.Outlined.CloudUpload
                    )
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Salve uma cópia de segurança fora da pasta do aplicativo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.iniciarExportacao() },
                                enabled = !uiState.isProcessando,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (uiState.isProcessando && uiState.operacaoAtual == "exportar") {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Icon(Icons.Outlined.CloudUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Exportar Agora")
                            }
                        }
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════
            // IMPORTAR BACKUP
            // ══════════════════════════════════════════════════════════════
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader(
                        title = "Importar Backup",
                        icon = Icons.Outlined.CloudDownload
                    )
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Restaure seus dados a partir de um arquivo local.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { viewModel.iniciarImportacao() },
                                enabled = !uiState.isProcessando,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (uiState.isProcessando && uiState.operacaoAtual == "importar") {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Icon(Icons.Outlined.CloudDownload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Importar de Arquivo")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "A importação substitui todos os dados atuais.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════
            // MANUTENÇÃO
            // ══════════════════════════════════════════════════════════════
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader(
                        title = "Manutenção",
                        icon = Icons.Outlined.DeleteSweep
                    )
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteSweep,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Limpar Dados Antigos",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "Remover registros de ponto anteriores a uma data específica",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Manter últimos",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    IconButton(
                                        onClick = { if (uiState.mesesParaLimpeza > 1) viewModel.onAction(BackupAction.AlterarMesesLimpeza(uiState.mesesParaLimpeza - 1)) },
                                        enabled = uiState.mesesParaLimpeza > 1
                                    ) {
                                        Text("-", style = MaterialTheme.typography.headlineSmall)
                                    }
                                    
                                    Text(
                                        text = "${uiState.mesesParaLimpeza} meses",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    IconButton(
                                        onClick = { viewModel.onAction(BackupAction.AlterarMesesLimpeza(uiState.mesesParaLimpeza + 1)) }
                                    ) {
                                        Text("+", style = MaterialTheme.typography.headlineSmall)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { showConfirmLimpeza = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Outlined.DeleteSweep, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Limpar Dados Agora")
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
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
private fun EstatisticasCard(
    totalEmpregos: Int,
    totalPontos: Int,
    totalFeriados: Int,
    tamanhoEstimado: String,
    tamanhoBanco: String,
    tamanhoImagens: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        if (isLoading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        } else {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    EstatisticaItem(
                        valor = totalEmpregos.toString(),
                        label = "Empregos"
                    )
                    EstatisticaItem(
                        valor = totalPontos.toString(),
                        label = "Registros"
                    )
//                    EstatisticaItem(
//                        valor = totalFeriados.toString(),
//                        label = "Feriados"
//                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    EstatisticaItem(
                        valor = tamanhoBanco,
                        label = "Banco DB"
                    )
                    EstatisticaItem(
                        valor = tamanhoImagens,
                        label = "Imagens"
                    )
                    EstatisticaItem(
                        valor = tamanhoEstimado,
                        label = "Total"
                    )
                }
            }
        }
    }
}

@Composable
private fun EstatisticaItem(
    valor: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = valor,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
