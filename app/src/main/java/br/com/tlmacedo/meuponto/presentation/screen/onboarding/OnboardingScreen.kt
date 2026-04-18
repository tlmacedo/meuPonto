package br.com.tlmacedo.meuponto.presentation.screen.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.tlmacedo.meuponto.presentation.components.NumberPicker
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    // Gerenciador de Permissões
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Avança independentemente do resultado (o usuário pode negar e configurar depois)
        scope.launch { pagerState.animateScrollToPage(3) }
    }

    LaunchedEffect(uiState.isConcluido) {
        if (uiState.isConcluido) {
            onFinish()
        }
    }

    Scaffold(
        bottomBar = {
            OnboardingBottomBar(
                currentPage = pagerState.currentPage,
                onNext = {
                    when (pagerState.currentPage) {
                        0 -> scope.launch { pagerState.animateScrollToPage(1) }
                        1 -> scope.launch { pagerState.animateScrollToPage(2) }
                        2 -> {
                            val permissions = mutableListOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                            permissionLauncher.launch(permissions.toTypedArray())
                        }
                        3 -> viewModel.nextStep()
                    }
                },
                onBack = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                },
                canContinue = when (pagerState.currentPage) {
                    1 -> uiState.nomeEmprego.isNotBlank()
                    else -> true
                }
            )
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            userScrollEnabled = false // Controlado pelos botões
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> JobSetupPage(
                    nomeEmprego = uiState.nomeEmprego,
                    onNomeChange = viewModel::onNomeEmpregoChange
                )
                2 -> PermissionsPage()
                3 -> ScheduleSetupPage(
                    cargaHoraria = uiState.cargaHorariaDiaria,
                    onCargaHorariaChange = viewModel::onCargaHorariaChange
                )
            }
        }
    }
}

@Composable
private fun ScheduleSetupPage(
    cargaHoraria: Int,
    onCargaHorariaChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Sua Jornada",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Qual é a sua carga horária diária padrão? Você poderá ajustar os horários detalhados depois.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            NumberPicker(
                value = cargaHoraria / 60,
                onValueChange = { onCargaHorariaChange(it * 60 + (cargaHoraria % 60)) },
                range = 0..23,
                suffix = "h"
            )
            Spacer(modifier = Modifier.width(16.dp))
            NumberPicker(
                value = cargaHoraria % 60,
                onValueChange = { onCargaHorariaChange((cargaHoraria / 60) * 60 + it) },
                range = 0..59,
                suffix = "m"
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = String.format(Locale.getDefault(), "%02d:%02d", cargaHoraria / 60, cargaHoraria % 60),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun WelcomePage() {
    OnboardingPageContent(
        icon = Icons.Default.Timer,
        title = "Bem-vindo ao MeuPonto",
        description = "Sua jornada de trabalho organizada e transparente. Registre seus pontos, gerencie ausências e tenha o controle total das suas horas."
    )
}

@Composable
private fun JobSetupPage(
    nomeEmprego: String,
    onNomeChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Business,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Onde você trabalha?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Para começar, informe o nome da empresa ou do seu emprego atual.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = nomeEmprego,
            onValueChange = onNomeChange,
            label = { Text("Nome do Emprego") },
            placeholder = { Text("Ex: Empresa X, Projeto Freelance") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
private fun PermissionsPage() {
    OnboardingPageContent(
        icon = Icons.Default.NotificationsActive,
        title = "Quase pronto!",
        description = "O MeuPonto funciona melhor com notificações para lembretes de registro e localização para o geofencing automático. Você poderá configurar as permissões a seguir."
    )
}

@Composable
private fun OnboardingPageContent(
    icon: ImageVector,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OnboardingBottomBar(
    currentPage: Int,
    onNext: () -> Unit,
    onBack: () -> Unit,
    canContinue: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentPage > 0) {
            TextButton(onClick = onBack) {
                Text("Voltar")
            }
        } else {
            Spacer(modifier = Modifier.width(80.dp))
        }

        Button(
            onClick = onNext,
            enabled = canContinue,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(if (currentPage == 3) "Começar" else "Próximo")
            if (currentPage < 3) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}
