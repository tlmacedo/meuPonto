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
    onCameraResult: (Boolean) -> Unit,
    onGalleryResult: (Uri?) -> Unit,
    onPermissionDenied: (String) -> Unit
) {
    // Flag para rastrear se uma ação está em andamento
    var actionInProgress by remember { mutableStateOf(false) }

    // Launcher da câmera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        Timber.d("cameraLauncher resultado: success=$success")
        actionInProgress = false
        onCameraResult(success)
    }

    // Launcher da galeria
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        Timber.d("galleryLauncher resultado: uri=$uri")
        actionInProgress = false
        onGalleryResult(uri)
    }

    // Permissão da câmera
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA) { granted ->
        Timber.d("Permissão câmera: granted=$granted")
        if (granted) {
            cameraUri?.let { uri ->
                Timber.d("Lançando câmera com uri=$uri")
                cameraLauncher.launch(uri)
            } ?: run {
                actionInProgress = false
                onPermissionDenied("Erro ao preparar câmera. Tente novamente.")
            }
        } else {
            actionInProgress = false
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
                Timber.d("Câmera selecionada. permissao=${cameraPermissionState.status.isGranted}, cameraUri=$cameraUri")
                actionInProgress = true
                if (cameraPermissionState.status.isGranted) {
                    cameraUri?.let { uri ->
                        Timber.d("Lançando câmera direto")
                        cameraLauncher.launch(uri)
                    } ?: run {
                        actionInProgress = false
                        onPermissionDenied("Erro ao preparar câmera. Tente novamente.")
                    }
                } else {
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
