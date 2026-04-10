package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File

@Composable
fun LocalImage(
    imagePath: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    placeholder: @Composable (() -> Unit)? = null,
    errorPlaceholder: Int? = null
) {
    val context = LocalContext.current
    
    if (imagePath == null) {
        placeholder?.invoke()
        return
    }

    val imageData = if (imagePath.startsWith("content://") || imagePath.startsWith("file://") || imagePath.startsWith("http")) {
        imagePath
    } else {
        // Assume path relativo ao filesDir/logos
        File(context.filesDir, "logos/$imagePath")
    }

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageData)
            .crossfade(true)
            .apply {
                if (errorPlaceholder != null) {
                    error(errorPlaceholder)
                }
            }
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}
