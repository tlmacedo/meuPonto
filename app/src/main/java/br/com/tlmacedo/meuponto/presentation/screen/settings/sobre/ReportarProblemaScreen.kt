package br.com.tlmacedo.meuponto.presentation.screen.settings.sobre

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.BuildConfig
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import coil.compose.AsyncImage

/**
 * Tela para reportar problemas e bugs (Beta).
 *
 * @author Thiago
 * @since 12.1.0
 */
@Composable
fun ReportarProblemaScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportarProblemaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var descricao by remember { mutableStateOf("") }
    var passos by remember { mutableStateOf("") }
    var evidencias by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            evidencias = (evidencias + uris).distinct().take(5)
        }
    )

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Reportar Problema",
                subtitle = (uiState.empregoApelido ?: uiState.empregoNome)?.uppercase(),
                logo = uiState.empregoLogo,
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Informativo
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Seu feedback é fundamental para melhorarmos o Meu Ponto durante esta fase beta.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Descrição do problema
            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("O que aconteceu?") },
                placeholder = { Text("Descreva o erro ou comportamento inesperado...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                leadingIcon = {
                    Icon(Icons.Outlined.BugReport, contentDescription = null)
                }
            )

            // Passos para reproduzir
            OutlinedTextField(
                value = passos,
                onValueChange = { passos = it },
                label = { Text("Como reproduzir? (Opcional)") },
                placeholder = { Text("1. Abri a tela X\n2. Cliquei no botão Y...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                leadingIcon = {
                    Icon(Icons.Outlined.Description, contentDescription = null)
                }
            )

            // Informações do Dispositivo (Visual)
            Text(
                text = "Informações que serão enviadas:",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            DeviceInfoItem(
                icon = Icons.Outlined.Info,
                label = "App Version",
                value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            )
            DeviceInfoItem(
                icon = Icons.Outlined.Info,
                label = "Android",
                value = "API ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})"
            )
            DeviceInfoItem(
                icon = Icons.Outlined.Info,
                label = "Device",
                value = "${Build.MANUFACTURER} ${Build.MODEL}"
            )

            // Evidências
            Text(
                text = "Evidências (Opcional):",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Card(
                        onClick = {
                            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.size(80.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.AttachFile, contentDescription = "Anexar")
                        }
                    }
                }

                items(evidencias) { uri ->
                    Box(modifier = Modifier.size(80.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { evidencias = evidencias - uri },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(20.dp)
                                .background(
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Remover",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    enviarEmailReporte(context, descricao, passos, evidencias)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = descricao.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enviar Reporte via E-mail")
            }

            Text(
                text = "O e-mail será aberto no seu aplicativo padrão para que você possa revisar as informações e anexar capturas de tela, se desejar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun DeviceInfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun enviarEmailReporte(
    context: Context,
    descricao: String,
    passos: String,
    evidencias: List<Uri>
) {
    val deviceInfo = """
        --- Device Info ---
        App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
        Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
        Manufacturer: ${Build.MANUFACTURER}
        Model: ${Build.MODEL}
        Product: ${Build.PRODUCT}
        -------------------
    """.trimIndent()

    val body = """
        PROBLEMA:
        $descricao
        
        PASSOS PARA REPRODUZIR:
        $passos
        
        $deviceInfo
    """.trimIndent()

    val intent = if (evidencias.isEmpty()) {
        Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:tl.macedo@hotmail.com".toUri()
            putExtra(Intent.EXTRA_SUBJECT, "Meu Ponto Beta - Reporte de Problema")
            putExtra(Intent.EXTRA_TEXT, body)
        }
    } else {
        val action = if (evidencias.size == 1) Intent.ACTION_SEND else Intent.ACTION_SEND_MULTIPLE
        Intent(action).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("tl.macedo@hotmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Meu Ponto Beta - Reporte de Problema")
            putExtra(Intent.EXTRA_TEXT, body)
            if (evidencias.size == 1) {
                putExtra(Intent.EXTRA_STREAM, evidencias[0])
            } else {
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(evidencias))
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    context.startActivity(Intent.createChooser(intent, "Enviar reporte usando..."))
}
