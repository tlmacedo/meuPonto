package br.com.tlmacedo.meuponto.presentation.screen.auth.login

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    // Configuração do BiometricPrompt
    val biometricPrompt = remember(activity) {
        activity?.let {
            BiometricPrompt(
                it,
                ContextCompat.getMainExecutor(it),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        viewModel.onBiometriaSucesso()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        // Silencia erro de cancelamento pelo usuário
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                            errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                        ) {
                            // viewModel.onAction(LoginAction.ErroBiometria(errString.toString()))
                        }
                    }
                }
            )
        }
    }

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticação Biométrica")
            .setSubtitle("Use sua biometria para entrar")
            .setNegativeButtonText("Usar senha")
            .build()
    }

    // Verifica disponibilidade da biometria ao iniciar
    LaunchedEffect(Unit) {
        val biometricManager = BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        viewModel.onAction(
            LoginAction.BiometriaDisponibilidadeAlterada(
                canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
            )
        )
    }

    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { event ->
            when (event) {
                is LoginEvent.LoginSucesso -> onNavigateToHome()
                is LoginEvent.MostrarErro -> snackbarHostState.showSnackbar(event.mensagem)
                is LoginEvent.SolicitarBiometria -> {
                    biometricPrompt?.authenticate(promptInfo)
                }

                else -> {}
            }
        }
    }

    // Dialog para habilitar biometria
    if (uiState.showDialogHabilitarBiometria) {
        AlertDialog(
            onDismissRequest = { viewModel.onAction(LoginAction.HabilitarBiometriaCancelado) },
            title = { Text("Habilitar Biometria") },
            text = { Text("Deseja utilizar biometria para seus próximos acessos?") },
            confirmButton = {
                Button(onClick = { viewModel.onAction(LoginAction.HabilitarBiometriaConfirmado) }) {
                    Text("Sim, habilitar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onAction(LoginAction.HabilitarBiometriaCancelado) }) {
                    Text("Agora não")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Meu Ponto",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.onAction(LoginAction.EmailAlterado(it)) },
                label = { Text("E-mail") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.emailErro != null,
                supportingText = uiState.emailErro?.let { { Text(it) } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.senha,
                onValueChange = { viewModel.onAction(LoginAction.SenhaAlterada(it)) },
                label = { Text("Senha") },
                visualTransformation = if (uiState.isSenhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                trailingIcon = {
                    IconButton(onClick = { viewModel.onAction(LoginAction.AlternarSenhaVisibilidade) }) {
                        Icon(
                            imageVector = if (uiState.isSenhaVisivel) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Alternar visibilidade da senha"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.senhaErro != null,
                supportingText = uiState.senhaErro?.let { { Text(it) } }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = uiState.lembrarMe,
                        onCheckedChange = { viewModel.onAction(LoginAction.LembrarMeAlterado(it)) }
                    )
                    Text(
                        text = "Lembrar-me",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                TextButton(onClick = onNavigateToForgotPassword) {
                    Text("Esqueci minha senha")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.onAction(LoginAction.ClicarEntrar) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = uiState.isFormValido && !uiState.isCarregando
            ) {
                if (uiState.isCarregando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Entrar")
                }
            }

            if (uiState.biometriaDisponivel) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { viewModel.onAction(LoginAction.LoginBiometriaClick) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Entrar com Biometria")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Não tem uma conta?", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onNavigateToRegister) {
                    Text("Cadastre‑se")
                }
            }
        }
    }
}
