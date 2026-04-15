package br.com.tlmacedo.meuponto.presentation.screen.settings.sobre

import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Launch
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar

/**
 * Tela de Ajuda e Suporte.
 *
 * Oferece canais de suporte, tutoriais e links para documentação.
 *
 * @author Thiago
 * @since 12.0.0
 */
@Composable
fun AjudaScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SobreViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Ajuda e Suporte",
                subtitle = (uiState.empregoApelido ?: uiState.empregoNome)?.uppercase(),
                logo = uiState.empregoLogo,
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Como podemos ajudar?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Selecione uma das opções abaixo para obter suporte ou aprender mais sobre o Meu Ponto.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ══════════════════════════════════════════════════════════════
            // CANAIS DE SUPORTE
            // ══════════════════════════════════════════════════════════════
            Text(
                text = "Canais de Atendimento",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            SupportItem(
                icon = Icons.Outlined.Email,
                title = "E-mail de Suporte",
                subtitle = "Envie suas dúvidas ou sugestões",
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = "mailto:tl.macedo@hotmail.com".toUri()
                        putExtra(Intent.EXTRA_SUBJECT, "Meu Ponto - Suporte")
                    }
                    context.startActivity(intent)
                }
            )

            SupportItem(
                icon = Icons.Outlined.QuestionAnswer,
                title = "Perguntas Frequentes",
                subtitle = "Respostas para as dúvidas mais comuns",
                onClick = { /* Link para FAQ futuro */ }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ══════════════════════════════════════════════════════════════
            // RECURSOS E APRENDIZADO
            // ══════════════════════════════════════════════════════════════
            Text(
                text = "Recursos e Aprendizado",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            SupportItem(
                icon = Icons.Outlined.PlayCircle,
                title = "Tutoriais em Vídeo",
                subtitle = "Assista como configurar e usar o app",
                onClick = { /* Link para vídeos */ }
            )

            SupportItem(
                icon = Icons.Outlined.Description,
                title = "Manual do Usuário",
                subtitle = "Documentação detalhada das funcionalidades",
                onClick = { /* Link para manual */ }
            )
        }
    }
}

@Composable
private fun SupportItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Launch,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
