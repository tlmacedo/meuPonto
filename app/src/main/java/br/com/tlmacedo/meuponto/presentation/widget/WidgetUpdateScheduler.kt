// path: app/src/main/java/br/com/tlmacedo/meuponto/presentation/widget/WidgetUpdateScheduler.kt
package br.com.tlmacedo.meuponto.presentation.widget

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * Centraliza o agendamento do WidgetUpdateWorker.
 */
object WidgetUpdateScheduler {

    fun scheduleNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WidgetUpdateWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}