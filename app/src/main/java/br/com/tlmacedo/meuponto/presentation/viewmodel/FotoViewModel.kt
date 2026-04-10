// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/viewmodel/FotoViewModel.kt
package br.com.tlmacedo.meuponto.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.FotoOrigem
import br.com.tlmacedo.meuponto.domain.usecase.foto.CreateTempImageFileUseCase
import br.com.tlmacedo.meuponto.domain.usecase.foto.DeleteComprovanteImageUseCase
import br.com.tlmacedo.meuponto.domain.usecase.foto.LoadComprovanteImageUseCase
import br.com.tlmacedo.meuponto.domain.usecase.foto.SaveComprovanteImageUseCase
import br.com.tlmacedo.meuponto.domain.usecase.foto.ValidateImageUseCase
import br.com.tlmacedo.meuponto.domain.usecase.foto.ImageValidationResult
import br.com.tlmacedo.meuponto.util.foto.SavePhotoResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * ViewModel para gerenciamento de fotos de comprovante.
 *
 * @author Thiago
 * @since 10.0.0
 * @updated 10.1.0 - Refatorado prepareCamera para ser chamado ao abrir diálogo
 */
@HiltViewModel
class FotoViewModel @Inject constructor(
    private val saveImageUseCase: SaveComprovanteImageUseCase,
    private val loadImageUseCase: LoadComprovanteImageUseCase,
    private val deleteImageUseCase: DeleteComprovanteImageUseCase,
    private val createTempFileUseCase: CreateTempImageFileUseCase,
    private val validateImageUseCase: ValidateImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FotoUiState())
    val uiState: StateFlow<FotoUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<FotoEvent>()
    val events: SharedFlow<FotoEvent> = _events.asSharedFlow()

    private var currentTempUri: Uri? = null

    /**
     * Abre o diálogo de seleção de fonte e prepara o URI da câmera.
     * O URI é criado antecipadamente para evitar problemas de sincronização.
     */
    fun showSourceDialog() {
        val cameraUri = createTempFileUseCase()
        currentTempUri = cameraUri
        _uiState.update {
            it.copy(
                showSourceDialog = true,
                cameraUri = cameraUri
            )
        }
    }

    /**
     * Fecha o diálogo de seleção de fonte.
     */
    fun dismissSourceDialog() {
        _uiState.update {
            it.copy(
                showSourceDialog = false,
                cameraUri = null
            )
        }
    }

    /**
     * Processa resultado da câmera.
     */
    fun onCameraResult(success: Boolean, origem: FotoOrigem) {
        dismissSourceDialog()
        if (success && currentTempUri != null) {
            validateAndPreview(currentTempUri!!, origem)
        } else {
            currentTempUri = null
        }
    }

    /**
     * Processa resultado da galeria.
     */
    fun onGalleryResult(uri: Uri?, origem: FotoOrigem) {
        dismissSourceDialog()
        if (uri != null) {
            validateAndPreview(uri, origem)
        }
    }

    /**
     * Valida imagem e mostra preview.
     */
    private fun validateAndPreview(uri: Uri, origem: FotoOrigem) {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val validation = validateImageUseCase(uri)

            when (validation) {
                is ImageValidationResult.Valid -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            previewUri = uri,
                            previewSource = origem,
                            showPreviewDialog = true,
                            imageInfo = ImageInfo(
                                width = validation.width,
                                height = validation.height,
                                sizeBytes = validation.sizeBytes,
                                mimeType = validation.mimeType
                            )
                        )
                    }
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                    emitEvent(FotoEvent.ValidationError(
                        validation.getErrorMessage() ?: "Imagem inválida"
                    ))
                    currentTempUri = null
                }
            }
        }
    }

    /**
     * Confirma e salva a foto.
     */
    fun confirmAndSave(
        ponto: Ponto,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        val previewUri = _uiState.value.previewUri ?: return

        _uiState.update { it.copy(isLoading = true, showPreviewDialog = false) }

        viewModelScope.launch {
            val result = saveImageUseCase(
                sourceUri = previewUri,
                ponto = ponto,
                latitude = latitude,
                longitude = longitude
            )

            when (result) {
                is SavePhotoResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            previewUri = null,
                            previewSource = null,
                            savedFilePath = result.relativePath,
                            savedFileHash = result.hashMd5
                        )
                    }
                    emitEvent(FotoEvent.SaveSuccess(result.relativePath, result.hashMd5))
                }
                is SavePhotoResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    emitEvent(FotoEvent.Error(result.message))
                }
            }

            currentTempUri = null
        }
    }

    /**
     * Cancela preview e limpa estado.
     */
    fun cancelPreview() {
        currentTempUri = null
        _uiState.update {
            it.copy(
                showPreviewDialog = false,
                previewUri = null,
                previewSource = null,
                imageInfo = null
            )
        }
    }

    /**
     * Exclui foto de um ponto.
     */
    fun deletePhoto(ponto: Ponto) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val success = deleteImageUseCase(ponto)

            _uiState.update { it.copy(isLoading = false) }

            if (success) {
                emitEvent(FotoEvent.DeleteSuccess)
            } else {
                emitEvent(FotoEvent.Error("Erro ao excluir foto"))
            }
        }
    }

    /**
     * Verifica se ponto tem foto.
     */
    fun hasPhoto(ponto: Ponto): Boolean {
        return loadImageUseCase.exists(ponto)
    }

    /**
     * Obtém arquivo da foto para compartilhar.
     */
    fun getPhotoFile(ponto: Ponto): File? {
        return loadImageUseCase.getFile(ponto)
    }

    fun showDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = true) }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }

    fun clearSavedResult() {
        _uiState.update {
            it.copy(savedFilePath = null, savedFileHash = null)
        }
    }

    private fun emitEvent(event: FotoEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    override fun onCleared() {
        super.onCleared()
        createTempFileUseCase.cleanupOldTempFiles()
    }
}

data class FotoUiState(
    val isLoading: Boolean = false,
    val showSourceDialog: Boolean = false,
    val showPreviewDialog: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val previewUri: Uri? = null,
    val previewSource: FotoOrigem? = null,
    val imageInfo: ImageInfo? = null,
    val savedFilePath: String? = null,
    val savedFileHash: String? = null,
    /** URI preparado para captura de câmera */
    val cameraUri: Uri? = null
)

data class ImageInfo(
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
    val mimeType: String
) {
    val dimensions: String get() = "${width}x${height}"
    val sizeFormatted: String
        get() = when {
            sizeBytes < 1024 -> "$sizeBytes B"
            sizeBytes < 1024 * 1024 -> String.format("%.1f KB", sizeBytes / 1024.0)
            else -> String.format("%.2f MB", sizeBytes / (1024.0 * 1024.0))
        }
}

sealed class FotoEvent {
    data class SaveSuccess(val filePath: String, val hash: String) : FotoEvent()
    data object DeleteSuccess : FotoEvent()
    data class ValidationError(val message: String) : FotoEvent()
    data class Error(val message: String) : FotoEvent()
}
