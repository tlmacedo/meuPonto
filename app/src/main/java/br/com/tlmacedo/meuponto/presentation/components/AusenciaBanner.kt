// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/AusenciaBanner.kt
package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.MetadataFerias
import br.com.tlmacedo.meuponto.domain.usecase.feriado.VerificarDiaEspecialUseCase
import br.com.tlmacedo.meuponto.presentation.theme.Error
import br.com.tlmacedo.meuponto.presentation.theme.ErrorLight
import br.com.tlmacedo.meuponto.presentation.theme.Info
import br.com.tlmacedo.meuponto.presentation.theme.InfoLight
import br.com.tlmacedo.meuponto.presentation.theme.OnWarning
import br.com.tlmacedo.meuponto.presentation.theme.SidiaBlue
import br.com.tlmacedo.meuponto.presentation.theme.SidiaDarkGreen
import br.com.tlmacedo.meuponto.presentation.theme.SidiaSoftGreen
import br.com.tlmacedo.meuponto.presentation.theme.SurfaceVariant
import br.com.tlmacedo.meuponto.presentation.theme.WarningLight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val horaFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dateFormatterCompleto =
    DateTimeFormatter.ofPattern("dd/MM/yyyy (EEE)", Locale.forLanguageTag("pt-BR"))
private val dateFormatterSimples = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun AusenciaBanner(
    ausencia: Ausencia,
    metadataFerias: MetadataFerias? = null,
    verificarDiaEspecialUseCase: VerificarDiaEspecialUseCase? = null,
    ausenciaRepository: AusenciaRepository? = null,
    modifier: Modifier = Modifier
) {
    val backgroundColor = ausencia.tipo.getBackgroundColor()
    val contentColor = ausencia.tipo.getContentColor()

    // Cálculo assíncrono do dia de retorno
    val dataRetornoState = if (verificarDiaEspecialUseCase != null && ausenciaRepository != null) {
        produceState<LocalDate?>(initialValue = null, ausencia.dataFim) {
            value = withContext(Dispatchers.IO) {
                var dataCandidata = ausencia.dataFim.plusDays(1)
                var encontrado = false
                // Busca nos próximos 30 dias para evitar loop infinito em caso de erro de dados
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
    } else null

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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Ícone do tipo de ausência
                Icon(
                    imageVector = ausencia.tipo.getIcon(),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Tipo da ausência
                val textoTipo = buildString {
                    append(stringResource(R.string.banner_ausencia_hoje, ausencia.tipo.descricao))
                    dataRetornoState?.value?.let { retorno ->
                        append(", retorno é dia ")
                        append(retorno.format(dateFormatterCompleto))
                    }
                }

                Text(
                    text = textoTipo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    modifier = Modifier.weight(1f)
                )


                // Ícone de anexo (se houver imagem)
                if (ausencia.imagemUri != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = contentColor.copy(alpha = 0.12f),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Anexo",
                            tint = contentColor,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(16.dp)
                        )
                    }
                }

//                // Badge de status
//                AusenciaStatusBadge(
//                    isJustificada = ausencia.isJustificada,
//                    contentColor = contentColor
//                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Conteúdo específico por tipo
            when (ausencia.tipo) {
                TipoAusencia.FERIAS -> FeriasContent(
                    ausencia = ausencia,
                    metadata = metadataFerias,
                    contentColor = contentColor
                )

                TipoAusencia.DECLARACAO -> DeclaracaoContent(
                    ausencia = ausencia,
                    contentColor = contentColor
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
 * Formata período de gozo de férias no formato completo.
 */
private fun formatarGozoFerias(dataInicio: LocalDate, dataFim: LocalDate): String {
    return "${dataInicio.format(dateFormatterCompleto)} ~ ${dataFim.format(dateFormatterCompleto)}"
}

/**
 * Formata período aquisitivo no formato simples.
 */
private fun formatarAquisitivoFerias(inicio: LocalDate?, fim: LocalDate?): String? {
    if (inicio == null || fim == null) return null
    return "${inicio.format(dateFormatterSimples)} ~ ${fim.format(dateFormatterSimples)}"
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
@Composable
private fun DeclaracaoContent(
    ausencia: Ausencia,
    contentColor: Color
) {
    // Linha 1: Horário e Duração
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Chip: Horário (início - fim)
        ausencia.horaInicio?.let { inicio ->
            val horaFim = ausencia.horaFimDeclaracao
            InfoChip(
                icon = Icons.Default.Schedule,
                text = if (horaFim != null) {
                    "${inicio.format(horaFormatter)} - ${horaFim.format(horaFormatter)}"
                } else {
                    inicio.format(horaFormatter)
                },
                contentColor = contentColor
            )
        }

        // Chip: Duração total
        ausencia.duracaoDeclaracaoMinutos?.let { duracao ->
            InfoChip(
                icon = Icons.Default.Timer,
                text = formatarMinutos(duracao),
                contentColor = contentColor
            )
        }

        // Chip: Tempo abonado (destaque)
        ausencia.duracaoAbonoMinutos?.let { abono ->
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = contentColor.copy(alpha = 0.2f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${formatarMinutos(abono)} abonado",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }
        }
    }

//    // Linha 2: Motivo/Descrição (se houver)
//    ausencia.descricao?.let { motivo ->
//        Spacer(modifier = Modifier.height(6.dp))
//        Text(
//            text = motivo,
//            style = MaterialTheme.typography.bodySmall,
//            color = contentColor.copy(alpha = 0.85f),
//            maxLines = 2,
//            overflow = TextOverflow.Ellipsis,
//            lineHeight = 16.sp
//        )
//    }

    // Linha 3: Observação adicional (se houver e diferente do motivo)
    ausencia.observacao?.takeIf { it != ausencia.descricao }?.let { obs ->
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

/**
 * Formata minutos para exibição (ex: 90 -> "1h30")
 */
private fun formatarMinutos(minutos: Int): String {
    val horas = minutos / 60
    val mins = minutos % 60
    return when {
        horas == 0 -> "${mins}min"
        mins == 0 -> "${horas}h"
        else -> "${horas}h${mins.toString().padStart(2, '0')}"
    }
}

// ============================================================================
// Extensões para TipoAusencia
// ============================================================================

/**
 * Retorna a cor de fundo apropriada para cada tipo de ausência.
 */
private fun TipoAusencia.getBackgroundColor(): Color = when (this) {
    TipoAusencia.FERIAS -> SurfaceVariant
    TipoAusencia.ATESTADO -> ErrorLight
    TipoAusencia.DECLARACAO -> WarningLight
    TipoAusencia.FOLGA -> SidiaSoftGreen
    TipoAusencia.FALTA_JUSTIFICADA -> InfoLight
    TipoAusencia.FALTA_INJUSTIFICADA -> ErrorLight
}

/**
 * Retorna a cor de conteúdo apropriada para cada tipo de ausência.
 */
private fun TipoAusencia.getContentColor(): Color = when (this) {
    TipoAusencia.FERIAS -> SidiaBlue
    TipoAusencia.ATESTADO -> Error
    TipoAusencia.DECLARACAO -> OnWarning
    TipoAusencia.FOLGA -> SidiaDarkGreen
    TipoAusencia.FALTA_JUSTIFICADA -> Info
    TipoAusencia.FALTA_INJUSTIFICADA -> Error
}

/**
 * Retorna o ícone apropriado para cada tipo de ausência.
 */
private fun TipoAusencia.getIcon(): ImageVector = when (this) {
    TipoAusencia.FERIAS -> Icons.Default.BeachAccess
    TipoAusencia.ATESTADO -> Icons.Default.LocalHospital
    TipoAusencia.DECLARACAO -> Icons.Default.Receipt
    TipoAusencia.FOLGA -> Icons.Default.Home
    TipoAusencia.FALTA_JUSTIFICADA -> Icons.Default.EventBusy
    TipoAusencia.FALTA_INJUSTIFICADA -> Icons.Default.EventBusy
}
