// path: app/src/main/java/br/com/tlmacedo/meuponto/presentation/widget/MeuPontoWidget.kt
package br.com.tlmacedo.meuponto.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import br.com.tlmacedo.meuponto.presentation.MainActivity

/**
 * Widget principal do MeuPonto.
 *
 * Lê as chaves do DataStore gravadas pelo WidgetUpdateWorker e exibe
 * até 5 layouts diferentes conforme o tamanho configurado no provider XML.
 *
 * Todas as keys são definidas no companion object [Keys] e lidas via
 * `prefs[Keys.X]` — nunca passando a Key diretamente onde String é esperada.
 */
class MeuPontoWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*>
        get() = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            GlanceTheme {
                WidgetContent(prefs = prefs, context = context)
            }
        }
    }

    // =========================================================================
    // CHAVES DO DATASTORE — único ponto de verdade
    // Devem bater EXATAMENTE com as keys gravadas no WidgetUpdateWorker
    // =========================================================================

    object Keys {
        // identificação
        val KEY_APELIDO = stringPreferencesKey("apelido_emprego")

        // horas trabalhadas e saldo do dia
        val KEY_HORAS_HOJE = stringPreferencesKey("horas_hoje")
        val KEY_SALDO_DIA = stringPreferencesKey("saldo_dia")
        val KEY_SALDO_DIA_NEG = stringPreferencesKey("saldo_dia_negativo")

        // saldo total (banco de horas)
        val KEY_SALDO_TOTAL = stringPreferencesKey("saldo_total")
        val KEY_SALDO_TOTAL_NEG = stringPreferencesKey("saldo_total_negativo")

        // saldo semanal e mensal (widget 4x2)
        val KEY_SALDO_SEMANA = stringPreferencesKey("saldo_semana")
        val KEY_SALDO_SEMANA_NEG = stringPreferencesKey("saldo_semana_negativo")
        val KEY_SALDO_MES = stringPreferencesKey("saldo_mes")
        val KEY_SALDO_MES_NEG = stringPreferencesKey("saldo_mes_negativo")

        // registros do dia
        val KEY_REG1 = stringPreferencesKey("registro_1")
        val KEY_REG2 = stringPreferencesKey("registro_2")
        val KEY_REG3 = stringPreferencesKey("registro_3")
        val KEY_REG4 = stringPreferencesKey("registro_4")

        // previsão de saída e atualização
        val KEY_PREVISAO = stringPreferencesKey("previsao_saida")
        val KEY_ATUALIZACAO = stringPreferencesKey("ultima_atualizacao")
    }
}

// =============================================================================
// CONTEÚDO DO WIDGET
// =============================================================================

@Composable
private fun WidgetContent(prefs: Preferences, context: Context) {
    val keys = MeuPontoWidget.Keys

    // Leitura correta: prefs[Key] → String?
    val apelido = prefs[keys.KEY_APELIDO] ?: "Meu emprego"
    val horasHoje = prefs[keys.KEY_HORAS_HOJE] ?: "--h --min"
    val saldoDia = prefs[keys.KEY_SALDO_DIA] ?: "+00h 00min"
    val saldoDiaNeg = prefs[keys.KEY_SALDO_DIA_NEG] == "true"
    val saldoTotal = prefs[keys.KEY_SALDO_TOTAL] ?: "+00h 00min"
    val saldoTotalNeg = prefs[keys.KEY_SALDO_TOTAL_NEG] == "true"
    prefs[keys.KEY_SALDO_SEMANA] ?: "+00h 00min"
    prefs[keys.KEY_SALDO_SEMANA_NEG] == "true"
    prefs[keys.KEY_SALDO_MES] ?: "+00h 00min"
    prefs[keys.KEY_SALDO_MES_NEG] == "true"
    prefs[keys.KEY_REG1] ?: "--:--"
    prefs[keys.KEY_REG2] ?: "--:--"
    prefs[keys.KEY_REG3] ?: "--:--"
    prefs[keys.KEY_REG4] ?: "--:--"
    val previsao = prefs[keys.KEY_PREVISAO] ?: "--:--"
    val atualizacao = prefs[keys.KEY_ATUALIZACAO] ?: "--/--/---- --:--"

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cabeçalho: nome do emprego
            Text(
                text = apelido.uppercase(),
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                ),
                maxLines = 1
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Linha principal: 3 colunas
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Coluna 1: Trab. no dia
                InfoColuna(
                    titulo = "Trab. no dia",
                    valor = horasHoje,
                    isNegativo = false,
                    modifier = GlanceModifier.defaultWeight()
                )

                Spacer(modifier = GlanceModifier.width(4.dp))

                // Coluna 2: Saldo do dia
                InfoColuna(
                    titulo = "Saldo do dia",
                    valor = saldoDia,
                    isNegativo = saldoDiaNeg,
                    modifier = GlanceModifier.defaultWeight()
                )

                Spacer(modifier = GlanceModifier.width(4.dp))

                // Coluna 3: Saldo total
                InfoColuna(
                    titulo = "Saldo total",
                    valor = saldoTotal,
                    isNegativo = saldoTotalNeg,
                    modifier = GlanceModifier.defaultWeight()
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Rodapé: previsão de saída
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Previsão de saída: $previsao",
                    style = TextStyle(
                        fontSize = 9.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Botão refresh
                Box(
                    modifier = GlanceModifier
                        .clickable {
                            WorkManager.getInstance(context).enqueueUniqueWork(
                                WidgetUpdateWorker.WORK_NAME,
                                ExistingWorkPolicy.REPLACE,
                                OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build()
                            )
                        }
                ) {
                    Text(
                        text = "↻",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = GlanceTheme.colors.primary
                        )
                    )
                }
            }

            // Última atualização
            Text(
                text = "Atualizado: $atualizacao",
                style = TextStyle(
                    fontSize = 8.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
    }
}

// Widget: saldo total (2x1)
class MeuPontoWidgetSaldoTotal : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*>
        get() = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val keys = MeuPontoWidget.Keys
            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.widgetBackground)
                        .padding(12.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    InfoColuna(
                        titulo = "Saldo total",
                        valor = prefs[keys.KEY_SALDO_TOTAL] ?: "+00h 00min",
                        isNegativo = prefs[keys.KEY_SALDO_TOTAL_NEG] == "true"
                    )
                }
            }
        }
    }
}

// Widget: trab. no dia (2x1)
class MeuPontoWidgetTrabDia : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*>
        get() = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val keys = MeuPontoWidget.Keys
            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.widgetBackground)
                        .padding(12.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    InfoColuna(
                        titulo = "Trab. no dia",
                        valor = prefs[keys.KEY_HORAS_HOJE] ?: "--h --min",
                        isNegativo = false
                    )
                }
            }
        }
    }
}

// Widget: últimos registros (4x1)
class MeuPontoWidgetRegistros : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*>
        get() = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val keys = MeuPontoWidget.Keys
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.widgetBackground)
                        .padding(12.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Registros de hoje",
                        style = TextStyle(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onSurface
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        listOf(
                            prefs[keys.KEY_REG1] ?: "--:--",
                            prefs[keys.KEY_REG2] ?: "--:--",
                            prefs[keys.KEY_REG3] ?: "--:--",
                            prefs[keys.KEY_REG4] ?: "--:--"
                        ).forEach { reg ->
                            Box(
                                modifier = GlanceModifier.defaultWeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = reg,
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GlanceTheme.colors.primary
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = "Prev. saída: ${prefs[keys.KEY_PREVISAO] ?: "--:--"}",
                        style = TextStyle(
                            fontSize = 8.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

// Widget: detalhado (4x2)
class MeuPontoWidgetDetalhado : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*>
        get() = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val keys = MeuPontoWidget.Keys
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.widgetBackground)
                        .padding(12.dp)
                        .clickable(actionStartActivity<MainActivity>())
                ) {
                    // Cabeçalho
                    Text(
                        text = (prefs[keys.KEY_APELIDO] ?: "Meu emprego").uppercase(),
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onSurface
                        ),
                        maxLines = 1
                    )

                    Spacer(modifier = GlanceModifier.height(6.dp))

                    // Linha 1: trab. dia / saldo dia / saldo total
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        InfoColuna(
                            titulo = "Trab. no dia",
                            valor = prefs[keys.KEY_HORAS_HOJE] ?: "--h --min",
                            isNegativo = false,
                            modifier = GlanceModifier.defaultWeight()
                        )
                        Spacer(modifier = GlanceModifier.width(4.dp))
                        InfoColuna(
                            titulo = "Saldo dia",
                            valor = prefs[keys.KEY_SALDO_DIA] ?: "+00h 00min",
                            isNegativo = prefs[keys.KEY_SALDO_DIA_NEG] == "true",
                            modifier = GlanceModifier.defaultWeight()
                        )
                        Spacer(modifier = GlanceModifier.width(4.dp))
                        InfoColuna(
                            titulo = "Saldo total",
                            valor = prefs[keys.KEY_SALDO_TOTAL] ?: "+00h 00min",
                            isNegativo = prefs[keys.KEY_SALDO_TOTAL_NEG] == "true",
                            modifier = GlanceModifier.defaultWeight()
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    // Linha 2: saldo semana / saldo mês
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        InfoColuna(
                            titulo = "Saldo semana",
                            valor = prefs[keys.KEY_SALDO_SEMANA] ?: "+00h 00min",
                            isNegativo = prefs[keys.KEY_SALDO_SEMANA_NEG] == "true",
                            modifier = GlanceModifier.defaultWeight()
                        )
                        Spacer(modifier = GlanceModifier.width(4.dp))
                        InfoColuna(
                            titulo = "Saldo mês",
                            valor = prefs[keys.KEY_SALDO_MES] ?: "+00h 00min",
                            isNegativo = prefs[keys.KEY_SALDO_MES_NEG] == "true",
                            modifier = GlanceModifier.defaultWeight()
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    // Linha 3: registros do dia
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        listOf(
                            prefs[keys.KEY_REG1] ?: "--:--",
                            prefs[keys.KEY_REG2] ?: "--:--",
                            prefs[keys.KEY_REG3] ?: "--:--",
                            prefs[keys.KEY_REG4] ?: "--:--"
                        ).forEach { reg ->
                            Box(
                                modifier = GlanceModifier.defaultWeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = reg,
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GlanceTheme.colors.primary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    // Rodapé
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        Text(
                            text = "Saída: ${prefs[keys.KEY_PREVISAO] ?: "--:--"}",
                            style = TextStyle(
                                fontSize = 8.sp,
                                color = GlanceTheme.colors.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        Text(
                            text = prefs[keys.KEY_ATUALIZACAO] ?: "--",
                            style = TextStyle(
                                fontSize = 8.sp,
                                color = GlanceTheme.colors.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// COMPONENTE REUTILIZÁVEL
// =============================================================================

@Composable
private fun InfoColuna(
    titulo: String,
    valor: String,
    isNegativo: Boolean,
    modifier: GlanceModifier = GlanceModifier
) {
    val corValor = if (isNegativo) {
        ColorProvider(androidx.compose.ui.graphics.Color(0xFFD32F2F))
    } else {
        GlanceTheme.colors.primary
    }

    Column(
        modifier = modifier.wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = titulo,
            style = TextStyle(
                fontSize = 8.sp,
                color = GlanceTheme.colors.onSurfaceVariant
            ),
            maxLines = 1
        )
        Spacer(modifier = GlanceModifier.height(2.dp))
        Text(
            text = valor,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = corValor
            ),
            maxLines = 1
        )
    }
}

// =============================================================================
// RECEIVERS
// =============================================================================

class MeuPontoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MeuPontoWidget()
}

class MeuPontoWidgetSaldoTotalReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MeuPontoWidgetSaldoTotal()
}

class MeuPontoWidgetTrabDiaReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MeuPontoWidgetTrabDia()
}

class MeuPontoWidgetRegistrosReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MeuPontoWidgetRegistros()
}

class MeuPontoWidgetDetalhadoReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MeuPontoWidgetDetalhado()
}