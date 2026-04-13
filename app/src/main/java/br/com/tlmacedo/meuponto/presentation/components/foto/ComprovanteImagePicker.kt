// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/foto/ComprovanteImagePicker.kt
package br.com.tlmacedo.meuponto.presentation.components.foto

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import timber.log.Timber

/**
 * Composable que gerencia seleção de imagem via câmera ou galeria.
 *
 * IMPORTANTE: Este componente deve permanecer na composição enquanto
 * a funcionalidade de foto estiver habilitada, não apenas quando o
 * diálogo estiver visível. Isso garante que os launchers recebam
 * os callbacks corretamente.
 *
 * @param showSourceDialog Se deve exibir o diálogo de seleção de fonte
 * @param onDismissSourceDialog Callback para fechar o diálogo (chamado pelo botão cancelar)
 * @param cameraUri URI já preparado para captura de foto
 * @param onCameraResult Callback com resultado da captura (true = sucesso)
 * @param onGalleryResult Callback com URI selecionado da galeria (null = cancelado)
 * @param onPermissionDenied Callback quando permissão é negada
 *
 * @author Thiago
 * @since 10.0.0
 * @updated 10.2.0 - Corrigido problema de callbacks perdidos ao fechar diálogo
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ComprovanteImagePicker(
    showSourceDialog: Boolean,
    onDismissSourceDialog: () -> Unit,
    cameraUri: Uri?,
    onCameraResult: (Boolean, br.com.tlmacedo.meuponto.domain.model.FotoOrigem) -> Unit,
    onGalleryResult: (Uri?, br.com.tlmacedo.meuponto.domain.model.FotoOrigem) -> Unit,
    onPermissionDenied: (String) -> Unit,
    onLaunchCustomCamera: () -> Unit = {}
) {
    // Flag para rastrear se uma ação está em andamento
    var actionInProgress by remember { mutableStateOf(false) }

    // Launcher da galeria
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        Timber.d("galleryLauncher resultado: uri=$uri")
        actionInProgress = false
        onGalleryResult(uri, br.com.tlmacedo.meuponto.domain.model.FotoOrigem.GALERIA)
    }

    // Permissão da câmera
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA) { granted ->
        Timber.d("Permissão câmera: granted=$granted")
        actionInProgress = false
        if (granted) {
            onLaunchCustomCamera()
        } else {
            onPermissionDenied("Permissão de câmera necessária para tirar fotos")
        }
    }

    // Diálogo de seleção de fonte
    if (showSourceDialog && !actionInProgress) {
        FotoSourceDialog(
            onDismiss = {
                Timber.d("Diálogo fechado pelo usuário")
                onDismissSourceDialog()
            },
            onCameraSelected = {
                Timber.d("Câmera selecionada. permissao=${cameraPermissionState.status.isGranted}")
                if (cameraPermissionState.status.isGranted) {
                    onLaunchCustomCamera()
                } else {
                    actionInProgress = true
                    Timber.d("Solicitando permissão de câmera")
                    cameraPermissionState.launchPermissionRequest()
                }
            },
            onGallerySelected = {
                Timber.d("Galeria selecionada")
                actionInProgress = true
                galleryLauncher.launch("image/*")
            }
        )
    }
}
