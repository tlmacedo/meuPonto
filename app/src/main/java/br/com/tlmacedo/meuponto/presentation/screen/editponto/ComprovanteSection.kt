// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/editponto/ComprovanteSection.kt
package br.com.tlmacedo.meuponto.presentation.screen.editponto

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.presentation.components.foto.ComprovanteCard
import br.com.tlmacedo.meuponto.presentation.components.foto.ComprovanteImagePicker
import br.com.tlmacedo.meuponto.presentation.components.foto.DeleteConfirmationDialog
import br.com.tlmacedo.meuponto.presentation.components.foto.FotoFullScreenViewer
import br.com.tlmacedo.meuponto.presentation.components.foto.FotoPreviewDialog
import br.com.tlmacedo.meuponto.presentation.viewmodel.FotoEvent
import br.com.tlmacedo.meuponto.presentation.viewmodel.FotoViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Seção de comprovante para a tela de edição de ponto.
 *
 * IMPORTANTE: Este composable só deve ser chamado quando a funcionalidade
 * de foto de comprovante estiver habilitada nas configurações do emprego.
 *
 * @param ponto Ponto sendo editado
 * @param onFotoSaved Callback quando foto é salva (filePath, hash)
 * @param onFotoDeleted Callback quando foto é excluída
 * @param modifier Modifier opcional
 * @param viewModel ViewModel de foto (injetado automaticamente)
 *
 * @author Thiago
 * @since 10.0.0
 * @updated 10.1.0 - Atualizado para nova API do ComprovanteImagePicker
 */
@Composable
fun ComprovanteSection(
    ponto: Ponto,
    onFotoSaved: (filePath: String, hash: String) -> Unit,
    onFotoDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FotoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showFullScreen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is FotoEvent.SaveSuccess -> {
                    onFotoSaved(event.filePath, event.hash)
                    Toast.makeText(context, "Comprovante salvo!", Toast.LENGTH_SHORT).show()
                }

                is FotoEvent.DeleteSuccess -> {
                    onFotoDeleted()
                    Toast.makeText(context, "Comprovante excluído", Toast.LENGTH_SHORT).show()
                }

                is FotoEvent.ValidationError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }

                is FotoEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Comprovante",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ComprovanteCard(
            fotoPath = ponto.fotoComprovantePath,
            onAddClick = { viewModel.showSourceDialog() },
            onViewClick = { showFullScreen = true },
            onEditClick = { viewModel.showSourceDialog() },
            onDeleteClick = { viewModel.showDeleteConfirmation() },
            onShareClick = { sharePhoto(context, viewModel, ponto) }
        )
    }

    // Image Picker - Só renderiza quando o diálogo está aberto
    if (uiState.showSourceDialog) {
        ComprovanteImagePicker(
            showSourceDialog = true,
            onDismissSourceDialog = { viewModel.dismissSourceDialog() },
            cameraUri = uiState.cameraUri,
            onCameraResult = { success, origem -> viewModel.onCameraResult(success, origem) },
            onGalleryResult = { uri, origem -> viewModel.onGalleryResult(uri, origem) },
            onPermissionDenied = { message ->
                viewModel.dismissSourceDialog()
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        )
    }

    // Preview Dialog
    if (uiState.showPreviewDialog && uiState.previewUri != null) {
        FotoPreviewDialog(
            uri = uiState.previewUri!!,
            imageInfo = uiState.imageInfo,
            isLoading = uiState.isLoading,
            onConfirm = { viewModel.confirmAndSave(ponto) },
            onCancel = { viewModel.cancelPreview() }
        )
    }

    // Delete Confirmation
    if (uiState.showDeleteConfirmation) {
        DeleteConfirmationDialog(
            onConfirm = { viewModel.deletePhoto(ponto) },
            onDismiss = { viewModel.dismissDeleteConfirmation() }
        )
    }

    // Full Screen Viewer
    if (showFullScreen && ponto.fotoComprovantePath != null) {
        FotoFullScreenViewer(
            fotoPath = ponto.fotoComprovantePath!!,
            onDismiss = { showFullScreen = false },
            onShare = { sharePhoto(context, viewModel, ponto) },
            onDelete = {
                showFullScreen = false
                viewModel.showDeleteConfirmation()
            }
        )
    }
}

private fun sharePhoto(context: Context, viewModel: FotoViewModel, ponto: Ponto) {
    val file = viewModel.getPhotoFile(ponto) ?: return

    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Comprovante de Ponto - ${ponto.data}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Comprovante"))
    } catch (e: Exception) {
        Toast.makeText(context, "Erro ao compartilhar: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
