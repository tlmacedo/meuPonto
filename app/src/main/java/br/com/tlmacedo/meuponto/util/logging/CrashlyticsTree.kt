package br.com.tlmacedo.meuponto.util.logging

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Árvore do Timber que envia logs e exceções para o Firebase Crashlytics.
 * Apenas logs de erro e warning são enviados em produção.
 */
class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return
        }

        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCustomKey("priority", priority)
        tag?.let { crashlytics.setCustomKey("tag", it) }
        crashlytics.log(message)

        if (t != null) {
            crashlytics.recordException(t)
        } else if (priority >= Log.ERROR) {
            crashlytics.recordException(Exception(message))
        }
    }
}
