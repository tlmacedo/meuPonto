package br.com.tlmacedo.meuponto.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import br.com.tlmacedo.meuponto.R
import br.com.tlmacedo.meuponto.presentation.MainActivity
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition

/**
 * Widget de tela inicial para o aplicativo MeuPonto.
 * 
 * Exibe o saldo de horas do dia e permite o registro rápido.
 *
 * @author Thiago
 * @since 13.0.0
 */
class MeuPontoWidget : GlanceAppWidget() {

    // Define onde o estado do widget será armazenado (DataStore de Preferências do Glance)
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }

    @Composable
    private fun WidgetContent() {
        // Recupera os dados persistidos no estado do widget
        val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
        val horasHoje = prefs[androidx.datastore.preferences.core.stringPreferencesKey("horas_hoje")] ?: "00:00"
        val proximoPonto = prefs[androidx.datastore.preferences.core.stringPreferencesKey("proximo_ponto")] ?: "Entrada"
        val apelidoEmprego = prefs[androidx.datastore.preferences.core.stringPreferencesKey("apelido_emprego")] ?: "Meu Ponto"

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header: Ícone + Nome do Emprego Ativo
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.icone_meu_ponto),
                    contentDescription = "Logo App",
                    modifier = GlanceModifier.size(20.dp)
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = apelidoEmprego,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
            }

            Spacer(modifier = GlanceModifier.height(10.dp))

            // Body: Resumo das Horas Trabalhadas Hoje
            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(GlanceTheme.colors.secondaryContainer)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Trabalhado Hoje",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSecondaryContainer,
                        fontSize = 10.sp
                    )
                )
                Text(
                    text = horasHoje,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSecondaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(10.dp))

            // Ação Rápida: Botão para Registrar o Próximo Ponto
            Button(
                text = "Registrar $proximoPonto",
                onClick = actionStartActivity<MainActivity>(),
                modifier = GlanceModifier.fillMaxWidth()
            )
        }
    }
}

class MeuPontoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MeuPontoWidget()
}
