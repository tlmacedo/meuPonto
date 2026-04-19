package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.configuracoes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LocalizacaoTrabalhoScreen(
    onNavigateBack: () -> Unit,
    onConfirmSelection: (Double, Double, Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LocalizacaoTrabalhoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is LocalizacaoTrabalhoEvent.Confirmado -> {
                    onConfirmSelection(evento.latitude, evento.longitude, evento.raio)
                }
                LocalizacaoTrabalhoEvent.Voltar -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Local de Trabalho",
                subtitle = uiState.nomeEmprego.uppercase(),
                showBackButton = true,
                onBackClick = { viewModel.onAction(LocalizacaoTrabalhoAction.Voltar) }
            )
        },
        floatingActionButton = {
            if (uiState.localizacaoSelecionada != null) {
                FloatingActionButton(
                    onClick = { viewModel.onAction(LocalizacaoTrabalhoAction.Confirmar) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Confirmar")
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LocalizacaoTrabalhoContent(
                uiState = uiState,
                onAction = viewModel::onAction,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun LocalizacaoTrabalhoContent(
    uiState: LocalizacaoTrabalhoUiState,
    onAction: (LocalizacaoTrabalhoAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            uiState.localizacaoInicial ?: LatLng(-15.793889, -47.882778), // Brasília como fallback
            15f
        )
    }

    val uiSettings = remember {
        MapUiSettings(
            myLocationButtonEnabled = true,
            zoomControlsEnabled = false
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
            onMapClick = { onAction(LocalizacaoTrabalhoAction.SelecionarLocalizacao(it)) }
        ) {
            uiState.localizacaoSelecionada?.let { loc ->
                Marker(
                    state = MarkerState(position = loc),
                    title = "Local de Trabalho",
                    draggable = true
                )
                Circle(
                    center = loc,
                    radius = uiState.raioMetros.toDouble(),
                    fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    strokeColor = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2f
                )
            }
        }

        // Card de controle do Raio
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Raio do Geofencing: ${uiState.raioMetros} metros",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = uiState.raioMetros.toFloat(),
                    onValueChange = { onAction(LocalizacaoTrabalhoAction.AlterarRaio(it.toInt())) },
                    valueRange = 50f..1000f,
                    steps = 18 // Passos de 50 metros (1000-50)/50 = 19 intervalos -> 18 steps
                )
                Text(
                    text = "O registro automático/validação ocorrerá dentro deste círculo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (uiState.localizacaoSelecionada == null) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))
            ) {
                Text(
                    text = "Toque no mapa para selecionar o local",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
