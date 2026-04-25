package br.com.tlmacedo.meuponto.presentation.screen.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.presentation.components.NumberPicker
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 7 })
    val scope = rememberCoroutineScope()

    // Gerenciador de Permissões
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Avança independentemente do resultado (o usuário pode negar e configurar depois)
        scope.launch { pagerState.animateScrollToPage(6) }
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
                        2 -> scope.launch { pagerState.animateScrollToPage(3) }
                        3 -> scope.launch { pagerState.animateScrollToPage(4) }
                        4 -> scope.launch { pagerState.animateScrollToPage(5) }
                        5 -> {
                            val permissions = mutableListOf<String>()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
                            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                            permissionLauncher.launch(permissions.toTypedArray())
                        }

                        6 -> viewModel.concluirOnboarding()
                    }
                },
                onBack = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                },
                canContinue = when (pagerState.currentPage) {
                    1 -> uiState.nomeEmprego.isNotBlank()
                    2 -> uiState.diasTrabalho.isNotEmpty()
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

                2 -> WorkingDaysPage(
                    diasSelecionados = uiState.diasTrabalho,
                    onToggleDia = viewModel::onDiaTrabalhoToggle
                )

                3 -> RegistrationOptionsPage(
                    uiState = uiState,
                    onFotoChange = viewModel::onFotoHabilitadaChange,
                    onLocalizacaoChange = viewModel::onLocalizacaoHabilitadaChange,
                    onNsrChange = viewModel::onNsrHabilitadoChange
                )

                4 -> RhInfoPage(
                    uiState = uiState,
                    onDiaFechamentoChange = viewModel::onDiaFechamentoRHChange,
                    onBancoHorasChange = viewModel::onBancoHorasHabilitadoChange
                )

                5 -> CloudSyncPage(
                    habilitado = uiState.backupNuvemHabilitado,
                    onHabilitadoChange = viewModel::onBackupNuvemHabilitadoChange
                )

                6 -> ScheduleSetupPage(
                    cargaHoraria = uiState.cargaHorariaDiaria,
                    onCargaHorariaChange = viewModel::onCargaHorariaDiariaChange
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
            text = String.format(
                Locale.getDefault(),
                "%02d:%02d",
                cargaHoraria / 60,
                cargaHoraria % 60
            ),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun WorkingDaysPage(
    diasSelecionados: Set<DiaSemana>,
    onToggleDia: (DiaSemana) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Quais dias você trabalha?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Selecione os dias da semana que fazem parte da sua jornada padrão.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DiaSemana.entries.forEach { dia ->
                val selecionado = diasSelecionados.contains(dia)
                Surface(
                    onClick = { onToggleDia(dia) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selecionado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selecionado,
                            onCheckedChange = { onToggleDia(dia) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = dia.descricao,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    OnboardingPageContent(
        icon = Icons.Default.Timer,
        title = "Bem-vindo ao MeuPonto",
        description = "Sua jornada de trabalho organizada e transparente.\n\nRegistre seus pontos com precisão, gerencie seus horários e tenha relatórios detalhados na palma da sua mão."
    )
}

@Composable
private fun RegistrationOptionsPage(
    uiState: OnboardingUiState,
    onFotoChange: (Boolean) -> Unit,
    onLocalizacaoChange: (Boolean) -> Unit,
    onNsrChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Opções de Registro",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))

        OnboardingSwitchItem(
            title = "Foto de Comprovante",
            description = "Solicitar foto ao registrar o ponto.",
            checked = uiState.fotoHabilitada,
            onCheckedChange = onFotoChange
        )
        OnboardingSwitchItem(
            title = "Localização",
            description = "Registrar coordenadas GPS do ponto.",
            checked = uiState.localizacaoHabilitada,
            onCheckedChange = onLocalizacaoChange
        )
        OnboardingSwitchItem(
            title = "Habilitar NSR",
            description = "Número Sequencial de Registro (REP).",
            checked = uiState.nsrHabilitado,
            onCheckedChange = onNsrChange
        )
    }
}

@Composable
private fun RhInfoPage(
    uiState: OnboardingUiState,
    onDiaFechamentoChange: (Int) -> Unit,
    onBancoHorasChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "RH e Banco de Horas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Dia de fechamento do RH:",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )
        NumberPicker(
            value = uiState.diaFechamentoRH,
            onValueChange = onDiaFechamentoChange,
            range = 1..28,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        OnboardingSwitchItem(
            title = "Banco de Horas",
            description = "Compensar horas extras e atrasos.",
            checked = uiState.bancoHorasHabilitado,
            onCheckedChange = onBancoHorasChange
        )
    }
}

@Composable
private fun CloudSyncPage(
    habilitado: Boolean,
    onHabilitadoChange: (Boolean) -> Unit
) {
    OnboardingPageContent(
        icon = Icons.Default.CloudUpload,
        title = "Sincronização na Nuvem",
        description = "Mantenha seus dados seguros e sincronizados entre dispositivos. Recomendamos manter o backup ativado.",
        content = {
            Spacer(modifier = Modifier.height(32.dp))
            OnboardingSwitchItem(
                title = "Backup Automático",
                description = "Sincronizar fotos e registros na nuvem.",
                checked = habilitado,
                onCheckedChange = onHabilitadoChange
            )
        }
    )
}

@Composable
private fun OnboardingSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        onClick = { onCheckedChange(!checked) },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
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
    description: String,
    content: @Composable ColumnScope.() -> Unit = {}
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
        content()
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
            Text(if (currentPage == 6) "Começar" else "Próximo")
            if (currentPage < 6) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}
