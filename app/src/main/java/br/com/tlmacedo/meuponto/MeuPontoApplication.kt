// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/MeuPontoApplication.kt
package br.com.tlmacedo.meuponto

import android.app.Application
import br.com.tlmacedo.meuponto.domain.service.MigracaoManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Classe principal da aplicação MeuPonto.
 *
 * Responsável pela inicialização do Hilt para injeção de dependências
 * e configuração inicial do aplicativo.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 7.1.0 - Adicionado suporte a migrações automáticas
 */
@HiltAndroidApp
class MeuPontoApplication : Application() {

    @Inject
    lateinit var migracaoManager: MigracaoManager

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("MeuPonto Application iniciada")

        // Executa migrações pendentes em background
        executarMigracoes()
    }

    private fun executarMigracoes() {
        applicationScope.launch {
            try {
                migracaoManager.executarMigracoesPendentes()
            } catch (e: Exception) {
                Timber.e(e, "Erro ao executar migrações")
            }
        }
    }
}
