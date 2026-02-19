// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/LocationPickerDialog.kt
package br.com.tlmacedo.meuponto.presentation.components

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.com.tlmacedo.meuponto.data.service.LocationService
import br.com.tlmacedo.meuponto.domain.model.FonteLocalizacao
import br.com.tlmacedo.meuponto.domain.model.Localizacao
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

/**
 * Dialog para seleção de localização no mapa.
 *
 * @author Thiago
 * @since 3.5.0
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerDialog(
    localizacaoInicial: Localizacao?,
    onConfirm: (Localizacao) -> Unit,
    onDismiss: () -> Unit,
    locationService: LocationService
) {
    val scope = rememberCoroutineScope()

    // Estado da localização selecionada
    var localizacaoSelecionada by remember { mutableStateOf(localizacaoInicial) }
    var endereco by remember { mutableStateOf(localizacaoInicial?.endereco ?: "") }
    var isLoadingEndereco by remember { mutableStateOf(false) }
    var isLoadingGps by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }

    // Posição inicial do mapa (Manaus como padrão ou localização inicial)
    val posicaoInicial = localizacaoInicial?.let {
        LatLng(it.latitude, it.longitude)
    } ?: LatLng(-3.1190, -60.0217) // Manaus

    // Estado do mapa
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(posicaoInicial, 15f)
    }

    // Permissões de localização
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Função para buscar endereço
    fun buscarEndereco(lat: Double, lng: Double) {
        scope.launch {
            isLoadingEndereco = true
            try {
                val novoEndereco = locationService.getAddressFromLocation(lat, lng)
                endereco = novoEndereco ?: ""
            } catch (e: Exception) {
                endereco = ""
            }
            isLoadingEndereco = false
        }
    }

    // Função para capturar localização GPS
    fun capturarGps() {
        if (!locationPermissions.allPermissionsGranted) {
            locationPermissions.launchMultiplePermissionRequest()
            return
        }

        scope.launch {
            isLoadingGps = true
            erro = null
            try {
                val localizacao = locationService.getCurrentLocation()
                if (localizacao != null) {
                    localizacaoSelecionada = localizacao
                    val novaPos = LatLng(localizacao.latitude, localizacao.longitude)
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(novaPos, 17f),
                        durationMs = 500
                    )
                    buscarEndereco(localizacao.latitude, localizacao.longitude)
                } else {
                    erro = "Não foi possível obter a localização. Verifique se o GPS está ativado."
                }
            } catch (e: Exception) {
                erro = "Erro ao obter localização: ${e.message}"
            }
            isLoadingGps = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                TopAppBar(
                    title = { Text("Selecionar Localização") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                localizacaoSelecionada?.let { loc ->
                                    onConfirm(loc.copy(endereco = endereco.ifBlank { null }))
                                }
                            },
                            enabled = localizacaoSelecionada != null
                        ) {
                            Text("Confirmar")
                        }
                    }
                )

                // Conteúdo principal com mapa
                Box(modifier = Modifier.weight(1f)) {
                    // Mapa
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(
                            isMyLocationEnabled = locationPermissions.allPermissionsGranted,
                            mapType = MapType.NORMAL
                        ),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                            myLocationButtonEnabled = false,
                            compassEnabled = true
                        ),
                        onMapClick = { latLng ->
                            localizacaoSelecionada = Localizacao(
                                latitude = latLng.latitude,
                                longitude = latLng.longitude,
                                fonte = FonteLocalizacao.MANUAL
                            )
                            buscarEndereco(latLng.latitude, latLng.longitude)
                        }
                    ) {
                        // Marcador da localização selecionada
                        localizacaoSelecionada?.let { loc ->
                            Marker(
                                state = MarkerState(position = LatLng(loc.latitude, loc.longitude)),
                                title = "Local selecionado",
                                snippet = loc.enderecoOuCoordenadas
                            )
                        }
                    }

                    // Indicador central
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .offset(y = 20.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        )
                    }

                    // Botão de GPS flutuante
                    FloatingActionButton(
                        onClick = { capturarGps() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        if (isLoadingGps) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Usar localização atual"
                            )
                        }
                    }

                    // Botões de zoom
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SmallFloatingActionButton(
                            onClick = {
                                scope.launch {
                                    cameraPositionState.animate(CameraUpdateFactory.zoomIn())
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Zoom in")
                        }
                        SmallFloatingActionButton(
                            onClick = {
                                scope.launch {
                                    cameraPositionState.animate(CameraUpdateFactory.zoomOut())
                                }
                            }
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Zoom out")
                        }
                    }

                    // Erro (movido para dentro de uma Column para evitar problemas com AnimatedVisibility)
                    if (erro != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = erro ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { erro = null }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Fechar"
                                    )
                                }
                            }
                        }
                    }
                }

                // Footer com informações da localização
                Surface(
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Coordenadas
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PinDrop,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = localizacaoSelecionada?.coordenadasFormatadas
                                    ?: "Toque no mapa para selecionar",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Endereço
                        if (localizacaoSelecionada != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (isLoadingEndereco) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Buscando endereço...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = endereco.ifBlank { "Endereço não disponível" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (endereco.isBlank())
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Precisão
                        localizacaoSelecionada?.precisao?.let { precisao ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when {
                                        precisao <= 10f -> Icons.Default.GpsFixed
                                        precisao <= 30f -> Icons.Default.GpsNotFixed
                                        else -> Icons.Default.GpsOff
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        precisao <= 10f -> MaterialTheme.colorScheme.primary
                                        precisao <= 30f -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.error
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Precisão: ${precisao.toInt()}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Dica
                        if (localizacaoSelecionada == null) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TouchApp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Toque no mapa para selecionar um local ou use o botão de GPS",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Solicitação de permissão
                if (!locationPermissions.allPermissionsGranted) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Permissão de localização",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "Conceda permissão para usar o GPS",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            TextButton(
                                onClick = { locationPermissions.launchMultiplePermissionRequest() }
                            ) {
                                Text("Permitir")
                            }
                        }
                    }
                }
            }
        }
    }
}
