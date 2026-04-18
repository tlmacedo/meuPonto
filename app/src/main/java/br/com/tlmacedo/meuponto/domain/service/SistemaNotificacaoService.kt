package br.com.tlmacedo.meuponto.domain.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import br.com.tlmacedo.meuponto.R
import br.com.tlmacedo.meuponto.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serviço centralizado para notificações do sistema (erros críticos, alertas de infra).
 */
@Singleton
class SistemaNotificacaoService @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val CHANNEL_ID = "sistema_alertas"
        private const val CHANNEL_NAME = "Alertas do Sistema"
        private const val NOTIFICATION_ID_BACKUP_ERRO = 1001
    }

    init {
        criarCanalNotificacao()
    }

    private fun criarCanalNotificacao() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alertas críticos sobre o funcionamento do aplicativo"
        }
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    fun notificarErroBackup(detalhes: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "configuracoes_backup")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, NOTIFICATION_ID_BACKUP_ERRO, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mensagem = "Não foi possível realizar o backup: $detalhes. Verifique sua conexão ou conta Google."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Falha no Backup na Nuvem")
            .setContentText(mensagem)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensagem))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID_BACKUP_ERRO, notification)
    }
}
