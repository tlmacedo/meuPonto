// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/AusenciaBanner.kt
package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.tlmacedo.meuponto.R
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.ferias.MetadataFerias
import br.com.tlmacedo.meuponto.domain.usecase.feriado.VerificarDiaEspecialUseCase
import br.com.tlmacedo.meuponto.presentation.mapper.toTipoAusenciaCor
import br.com.tlmacedo.meuponto.presentation.model.TipoAusenciaCor
import br.com.tlmacedo.meuponto.presentation.theme.Error
import br.com.tlmacedo.meuponto.presentation.theme.ErrorLight
import br.com.tlmacedo.meuponto.presentation.theme.Info
import br.com.tlmacedo.meuponto.presentation.theme.InfoLight
import br.com.tlmacedo.meuponto.presentation.theme.SidiaDarkGreen
import br.com.tlmacedo.meuponto.presentation.theme.SidiaSoftGreen
import br.com.tlmacedo.meuponto.util.helper.dateFormatterCompleto
import br.com.tlmacedo.meuponto.util.helper.formatarAquisitivoFerias
import br.com.tlmacedo.meuponto.util.helper.formatarComoDuracaoCurta
import br.com.tlmacedo.meuponto.util.helper.formatarGozoFerias
import br.com.tlmacedo.meuponto.util.helper.horaFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

@Composable
fun AusenciaBanner(
    ausencia: Ausencia,
    metadataFerias: MetadataFerias? = null,
    verificarDiaEspecialUseCase: VerificarDiaEspecialUseCase? = null,
    ausenciaRepository: AusenciaRepository? = null,
    modifier: Modifier = Modifier,
    onVerAnexo: (() -> Unit)? = null,
    onAdicionarImagemCamera: (() -> Unit)? = null,
    onAdicionarImagemGaleria: (() -> Unit)? = null,
    onRemoverImagem: (() -> Unit)? = null,
    onExcluir: (() -> Unit)? = null
) {
    val backgroundColor = ausencia.tipo.getBackgroundColor()
    val contentColor = ausencia.tipo.getContentColor()

    val isDeclaracao = ausencia.tipo == TipoAusencia.Declaracao
    val possuiImagem = !ausencia.imagemUri.isNullOrBlank()

    val titulo = when {
        isDeclaracao -> "Declaração"
        else -> ausencia.tipo.descricao
    }

    val subtituloPrincipal = when {
        isDeclaracao -> {
            val declarado = (ausencia.duracaoDeclaracaoMinutos ?: 0).formatarComoDuracaoCurta()
            val abonado = (ausencia.duracaoAbonoMinutos ?: 0).formatarComoDuracaoCurta()
            "Declarado $declarado • Abonado $abonado"
        }

        else -> null
    }

    val textoComprovante = when {
        possuiImagem -> "Comprovante anexado"
        isDeclaracao -> "Comprovante obrigatório pendente"
        else -> "Sem comprovante"
    }

    // Cálculo assíncrono do dia de retorno
    val deveMostrarRetorno = ausencia.tipo != TipoAusencia.Declaracao

    val dataRetornoState =
        if (
            deveMostrarRetorno &&
            verificarDiaEspecialUseCase != null &&
            ausenciaRepository != null
        ) {
            produceState<LocalDate?>(initialValue = null, ausencia.dataFim) {
                value = withContext(Dispatchers.IO) {
                    var dataCandidata = ausencia.dataFim.plusDays(1)
                    var encontrado = false

                    for (i in 0..30) {
                        val diaEspecial = verificarDiaEspecialUseCase(
                            data = dataCandidata,
                            empregoId = ausencia.empregoId
                        )

                        val temAusencia = ausenciaRepository.existeAusenciaEmData(
                            empregoId = ausencia.empregoId,
                            data = dataCandidata
                        )

                        if (diaEspecial.isDiaUtil && !temAusencia) {
                            encontrado = true
                            break
                        }

                        dataCandidata = dataCandidata.plusDays(1)
                    }

                    if (encontrado) dataCandidata else null
                }
            }
        } else {
            null
        }

    val textoTipo = buildString {
        append(titulo)

        if (deveMostrarRetorno) {
            dataRetornoState?.value?.let { retorno ->
                append(" • retorno ")
                append(retorno.format(dateFormatterCompleto))
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header com ícone, tipo e badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = contentColor.copy(alpha = 0.14f)
                ) {
                    Icon(
                        imageVector = ausencia.tipo.getIcon(),
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = textoTipo,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isDeclaracao) {
                        Text(
                            text = "Ausência parcial com abono de jornada",
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.76f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.banner_ausencia_hoje, ausencia.tipo.descricao),
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.76f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                AusenciaBannerActions(
                    possuiImagem = possuiImagem,
                    contentColor = contentColor,
                    onVerAnexo = onVerAnexo,
                    onAdicionarImagemCamera = onAdicionarImagemCamera,
                    onAdicionarImagemGaleria = onAdicionarImagemGaleria,
                    onRemoverImagem = onRemoverImagem,
                    onExcluir = onExcluir
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (ausencia.tipo) {
                TipoAusencia.Ferias -> FeriasContent(
                    ausencia = ausencia,
                    metadata = metadataFerias,
                    contentColor = contentColor
                )

                TipoAusencia.Declaracao -> DeclaracaoContent(
                    ausencia = ausencia,
                    contentColor = contentColor,
                    possuiImagem = possuiImagem
                )

                else -> DefaultAusenciaContent(
                    ausencia = ausencia,
                    contentColor = contentColor
                )
            }
        }
    }
}

/**
 * Conteúdo específico para Férias - exibe detalhes do período e saldo.
 */
@Composable
private fun FeriasContent(
    ausencia: Ausencia,
    metadata: MetadataFerias?,
    contentColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Linha 1: Período de Gozo Completo
        Text(
            text = formatarGozoFerias(ausencia.dataInicio, ausencia.dataFim),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )

        // Linha 2: Período Aquisitivo e Sequência
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chip: Período Aquisitivo
            formatarAquisitivoFerias(
                ausencia.dataInicioPeriodoAquisitivo,
                ausencia.dataFimPeriodoAquisitivo
            )?.let { periodo ->
                InfoChip(
                    text = stringResource(R.string.ausencia_periodo_aquisitivo) + ": $periodo",
                    icon = Icons.Default.Schedule,
                    contentColor = contentColor
                )
            }

            // Chip: Sequência (ex: 1º período)
            metadata?.let {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = contentColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = stringResource(
                            R.string.ausencia_sequencia_ferias,
                            it.sequenciaPeriodo
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Nova Grade de Informações de Saldo
        metadata?.let { m ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Primeira linha: Marcados e Aproveitados
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SaldoItem(
                        label = stringResource(R.string.ausencia_ferias_saldo_marcados),
                        valor = "${m.diasMarcados}/${m.diasGanhos} d",
                        contentColor = contentColor,
                        modifier = Modifier.weight(1f)
                    )
                    SaldoItem(
                        label = stringResource(R.string.ausencia_ferias_saldo_aproveitados),
                        valor = "${m.diasAproveitados} d",
                        contentColor = contentColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Segunda linha: Restantes para Marcar e Aproveitar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SaldoItem(
                        label = stringResource(R.string.ausencia_ferias_saldo_restante_marcar),
                        valor = "${m.diasRestantesParaMarcar} d",
                        contentColor = contentColor,
                        modifier = Modifier.weight(1f)
                    )
                    SaldoItem(
                        label = stringResource(R.string.ausencia_ferias_saldo_restante_aproveitar),
                        valor = "${m.diasRestantesParaAproveitar} d",
                        contentColor = contentColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SaldoItem(
    label: String,
    valor: String,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = contentColor.copy(alpha = 0.05f),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

/**
 * Conteúdo específico para Declaração - layout compacto com todas as informações.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DeclaracaoContent(
    ausencia: Ausencia,
    contentColor: Color,
    possuiImagem: Boolean
) {
    val inicio = ausencia.horaInicio
    val fim = ausencia.horaFimDeclaracao

    val tempoDeclarado = ausencia.duracaoDeclaracaoMinutos ?: 0
    val tempoAbonado = ausencia.duracaoAbonoMinutos ?: 0

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (inicio != null) {
                InfoChip(
                    icon = Icons.Default.Schedule,
                    text = if (fim != null) {
                        "${inicio.format(horaFormatter)} - ${fim.format(horaFormatter)}"
                    } else {
                        inicio.format(horaFormatter)
                    },
                    contentColor = contentColor
                )
            }

            if (tempoDeclarado > 0) {
                InfoChip(
                    icon = Icons.Default.Timer,
                    text = "Declarado ${tempoDeclarado.formatarComoDuracaoCurta()}",
                    contentColor = contentColor
                )
            }

            if (tempoAbonado > 0) {
                DestaqueChip(
                    icon = Icons.Default.AccessTime,
                    text = "Abonado ${tempoAbonado.formatarComoDuracaoCurta()}",
                    contentColor = contentColor
                )
            }
        }

        ausencia.observacao
            ?.takeIf { it.isNotBlank() }
            ?.let { observacao ->
                Text(
                    text = observacao,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.82f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (possuiImagem) {
                contentColor.copy(alpha = 0.12f)
            } else {
                Error.copy(alpha = 0.10f)
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = if (possuiImagem) {
                        Icons.Outlined.CheckCircle
                    } else {
                        Icons.Outlined.Cancel
                    },
                    contentDescription = null,
                    tint = if (possuiImagem) contentColor else Error,
                    modifier = Modifier.size(13.dp)
                )

                Text(
                    text = if (possuiImagem) {
                        "Comprovante anexado"
                    } else {
                        "Comprovante obrigatório pendente"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (possuiImagem) contentColor else Error,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DestaqueChip(
    text: String,
    icon: ImageVector,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = contentColor.copy(alpha = 0.20f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AusenciaBannerActions(
    possuiImagem: Boolean,
    contentColor: Color,
    onVerAnexo: (() -> Unit)?,
    onAdicionarImagemCamera: (() -> Unit)?,
    onAdicionarImagemGaleria: (() -> Unit)?,
    onRemoverImagem: (() -> Unit)?,
    onExcluir: (() -> Unit)?
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (possuiImagem) {
            IconButton(
                onClick = { onVerAnexo?.invoke() },
                enabled = onVerAnexo != null,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "Ver comprovante",
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = { onRemoverImagem?.invoke() },
                enabled = onRemoverImagem != null,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RemoveCircleOutline,
                    contentDescription = "Remover comprovante",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            IconButton(
                onClick = { onAdicionarImagemCamera?.invoke() },
                enabled = onAdicionarImagemCamera != null,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Adicionar pela câmera",
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = { onAdicionarImagemGaleria?.invoke() },
                enabled = onAdicionarImagemGaleria != null,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Adicionar da galeria",
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        IconButton(
            onClick = { onExcluir?.invoke() },
            enabled = onExcluir != null,
            modifier = Modifier.size(34.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = "Excluir ausência",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Conteúdo padrão para outros tipos de ausência.
 */
@Composable
private fun DefaultAusenciaContent(
    ausencia: Ausencia,
    contentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Chip com período
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = contentColor.copy(alpha = 0.12f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EventBusy,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = ausencia.formatarPeriodo(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
            }
        }

        // Quantidade de dias (se for período)
        if (ausencia.isPeriodo) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = contentColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "${ausencia.quantidadeDias} dias",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }

    // Descrição (se houver e diferente do tipo)
    ausencia.descricao?.takeIf { it != ausencia.tipo.descricao }?.let { desc ->
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

    // Observação (se houver)
    ausencia.observacao?.let { obs ->
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = obs,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Chip de informação reutilizável.
 */
@Composable
private fun InfoChip(
    text: String,
    icon: ImageVector,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = contentColor.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

/**
 * Badge de status (justificada ou não).
 */
@Composable
private fun AusenciaStatusBadge(
    isJustificada: Boolean,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val icon = if (isJustificada) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel
    val texto = if (isJustificada) "Justificada" else "Injustificada"

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = contentColor.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = contentColor,
                fontSize = 10.sp
            )
        }
    }
}

// ============================================================================
// UI Helper Extensions for TipoAusencia
// ============================================================================

/**
 * Retorna a cor de fundo apropriada para cada tipo de ausência.
 */
private fun TipoAusencia.getBackgroundColor(): Color = when (this.toTipoAusenciaCor()) {
    TipoAusenciaCor.VERDE -> SidiaSoftGreen
    TipoAusenciaCor.AZUL -> InfoLight
    TipoAusenciaCor.VERMELHO -> ErrorLight
}

/**
 * Retorna a cor de conteúdo apropriada para cada tipo de ausência.
 */
private fun TipoAusencia.getContentColor(): Color = when (this.toTipoAusenciaCor()) {
    TipoAusenciaCor.VERDE -> SidiaDarkGreen
    TipoAusenciaCor.AZUL -> Info
    TipoAusenciaCor.VERMELHO -> Error
}

/**
 * Retorna o ícone apropriado para cada tipo de ausência.
 */
private fun TipoAusencia.getIcon(): ImageVector = when (this) {
    TipoAusencia.Ferias -> Icons.Default.BeachAccess
    TipoAusencia.Atestado -> Icons.Default.LocalHospital
    TipoAusencia.Declaracao -> Icons.Default.Receipt
    TipoAusencia.DayOff,
    TipoAusencia.Folga -> Icons.Default.Home

    TipoAusencia.Feriado.Oficial,
    TipoAusencia.Feriado.DiaPonte,
    TipoAusencia.Feriado.Facultativo,
    TipoAusencia.CompensacaoBanco,
    TipoAusencia.Falta.Justificada,
    TipoAusencia.Falta.Injustificada -> Icons.Default.EventBusy
}
