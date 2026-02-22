// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/banco/NotificarTransicaoCicloUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.banco

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import br.com.tlmacedo.meuponto.R
import br.com.tlmacedo.meuponto.domain.model.FechamentoPeriodo
import br.com.tlmacedo.meuponto.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UseCase responsável por notificar o usuário sobre transições de ciclo do banco de horas.
 *
 * @author Thiago
 * @since 3.0.0
 */
@Singleton
class NotificarTransicaoCicloUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val CHANNEL_ID = "banco_horas_ciclo"
        private const val CHANNEL_NAME = "Ciclo do Banco de Horas"
        private const val NOTIFICATION_ID_BASE = 3000
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    init {
        criarCanalNotificacao()
    }

    private fun criarCanalNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações sobre fechamentos de ciclo do banco de horas"
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Notifica o usuário sobre um ou mais fechamentos de ciclo realizados.
     */
    operator fun invoke(ciclosFechados: List<FechamentoPeriodo>) {
        if (ciclosFechados.isEmpty()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "banco_horas")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val titulo = if (ciclosFechados.size == 1) {
            "Ciclo do Banco de Horas Fechado"
        } else {
            "${ciclosFechados.size} Ciclos do Banco de Horas Fechados"
        }

        val mensagem = buildString {
            if (ciclosFechados.size == 1) {
                val ciclo = ciclosFechados.first()
                append("Período: ${ciclo.dataInicioPeriodo.format(dateFormatter)} ~ ${ciclo.dataFimPeriodo.format(dateFormatter)}")
                append("\nSaldo final: ${ciclo.saldoAnteriorFormatado}")
            } else {
                append("Foram realizados ${ciclosFechados.size} fechamentos automáticos.")
                append("\nToque para ver detalhes.")
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Usar ícone existente
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensagem))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID_BASE + ciclosFechados.hashCode(), notification)
    }
}
