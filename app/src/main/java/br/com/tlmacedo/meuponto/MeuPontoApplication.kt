package br.com.tlmacedo.meuponto

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Classe principal da aplicação MeuPonto.
 *
 * Responsável pela inicialização do Hilt para injeção de dependências
 * e configuração inicial do aplicativo.
 *
 * @author Thiago
 * @since 1.0.0
 */
@HiltAndroidApp
class MeuPontoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        Timber.d("MeuPonto Application iniciada")
    }
}
