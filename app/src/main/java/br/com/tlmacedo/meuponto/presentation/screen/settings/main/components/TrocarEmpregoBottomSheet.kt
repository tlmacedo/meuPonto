package br.com.tlmacedo.meuponto.presentation.screen.settings.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.presentation.components.LocalImage

/**
 * BottomSheet para troca rápida de emprego ativo.
 *
 * Permite ao usuário trocar o emprego ativo sem entrar
 * na tela de gerenciamento completo.
 *
 * @param empregos Lista de empregos disponíveis (não arquivados)
 * @param empregoAtivoId ID do emprego atualmente ativo
 * @param sheetState Estado do BottomSheet
 * @param onEmpregoSelecionado Callback quando um emprego é selecionado
 * @param onGerenciarEmpregos Callback para navegar à tela de gerenciamento
 * @param onDismiss Callback para fechar o BottomSheet
 *
 * @author Thiago
 * @since 9.0.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrocarEmpregoBottomSheet(
    empregos: List<Emprego>,
    empregoAtivoId: Long?,
    sheetState: SheetState,
    onEmpregoSelecionado: (Emprego) -> Unit,
    onGerenciarEmpregos: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Título
            Text(
                text = "Trocar Emprego Ativo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            HorizontalDivider()

            if (empregos.isEmpty()) {
                // Estado vazio
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nenhum emprego disponível",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                // Lista de empregos
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(
                        items = empregos,
                        key = { it.id }
                    ) { emprego ->
                        val isAtivo = emprego.id == empregoAtivoId

                        EmpregoSelectionItem(
                            emprego = emprego,
                            isAtivo = isAtivo,
                            onClick = {
                                if (!isAtivo) {
                                    onEmpregoSelecionado(emprego)
                                }
                                onDismiss()
                            }
                        )
                    }
                }
            }

            HorizontalDivider()

            // Link para gerenciar empregos
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismiss()
                        onGerenciarEmpregos()
                    }
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Gerenciar Empregos",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Item de seleção de emprego na lista.
 */
@Composable
private fun EmpregoSelectionItem(
    emprego: Emprego,
    isAtivo: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Ícone de seleção
        Icon(
            imageVector = if (isAtivo) {
                Icons.Default.CheckCircle
            } else {
                Icons.Default.RadioButtonUnchecked
            },
            contentDescription = if (isAtivo) "Selecionado" else "Não selecionado",
            tint = if (isAtivo) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            }
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Logo do emprego
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (emprego.logo != null) {
                LocalImage(
                    imagePath = emprego.logo,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isAtivo) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Nome do emprego
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = emprego.nome,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isAtivo) FontWeight.Bold else FontWeight.Normal,
                color = if (isAtivo) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            if (isAtivo) {
                Text(
                    text = "Emprego atual",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
