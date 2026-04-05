// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/foto/ComprovanteThumbnail.kt
package br.com.tlmacedo.meuponto.presentation.components.foto

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import java.io.File

/**
 * Miniatura de comprovante para exibição em listas e cards.
 *
 * Carrega a imagem de forma assíncrona com estados de loading e erro.
 *
 * @param imagePath Caminho relativo da imagem
 * @param imageBaseDir Diretório base onde as imagens são armazenadas
 * @param onClick Callback ao clicar na miniatura
 * @param modifier Modificador do componente
 * @param size Tamanho da miniatura
 *
 * @author Thiago
 * @since 9.0.0
 */
@Composable
fun ComprovanteThumbnail(
    imagePath: String?,
    imageBaseDir: File?,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    if (imagePath.isNullOrBlank() || imageBaseDir == null) {
        // Placeholder quando não há imagem
        EmptyThumbnail(
            modifier = modifier,
            size = size
        )
        return
    }

    val imageFile = File(imageBaseDir, imagePath)

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageFile)
                .diskCacheKey("${imageFile.absolutePath}_${imageFile.lastModified()}")
                .memoryCacheKey("${imageFile.absolutePath}_${imageFile.lastModified()}")
                .crossfade(true)
                .size(size.value.toInt() * 2) // 2x para melhor qualidade
                .build(),
            contentDescription = "Comprovante",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(size / 2),
                        strokeWidth = 2.dp
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BrokenImage,
                        contentDescription = "Erro ao carregar",
                        modifier = Modifier.size(size / 2),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        )
    }
}

/**
 * Placeholder para quando não há imagem.
 */
@Composable
private fun EmptyThumbnail(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = null,
            modifier = Modifier.size(size / 2),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
