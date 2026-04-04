package br.com.tlmacedo.meuponto.presentation.screen.auth.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is RegisterEvent.CadastroSucesso -> onNavigateBack()
                is RegisterEvent.MostrarErro -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }
                RegisterEvent.NavegacaoParaLogin -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Criar Conta") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onAction(RegisterAction.NavegarParaLogin) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = uiState.nome,
                onValueChange = { viewModel.onAction(RegisterAction.NomeAlterado(it)) },
                label = { Text("Nome Completo") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                isError = uiState.nomeErro != null,
                supportingText = uiState.nomeErro?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.onAction(RegisterAction.EmailAlterado(it)) },
                label = { Text("E‑mail") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                isError = uiState.emailErro != null,
                supportingText = uiState.emailErro?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.senha,
                onValueChange = { viewModel.onAction(RegisterAction.SenhaAlterada(it)) },
                label = { Text("Senha") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    val image = if (uiState.isSenhaVisivel) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { viewModel.onAction(RegisterAction.AlternarSenhaVisibilidade) }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                },
                visualTransformation = if (uiState.isSenhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                isError = uiState.senhaErro != null,
                supportingText = uiState.senhaErro?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.confirmarSenha,
                onValueChange = { viewModel.onAction(RegisterAction.ConfirmarSenhaAlterada(it)) },
                label = { Text("Confirmar Senha") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    val image = if (uiState.isSenhaVisivel) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { viewModel.onAction(RegisterAction.AlternarSenhaVisibilidade) }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                },
                visualTransformation = if (uiState.isSenhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                isError = uiState.confirmarSenhaErro != null,
                supportingText = uiState.confirmarSenhaErro?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.erro != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = uiState.erro!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.onAction(RegisterAction.ClicarCadastrar) },
                enabled = !uiState.isCarregando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (uiState.isCarregando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Cadastrar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { viewModel.onAction(RegisterAction.NavegarParaLogin) }) {
                Text("Já tem conta? Entrar")
            }
        }
    }
}
