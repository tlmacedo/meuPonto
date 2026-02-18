// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/di/RepositoryModule.kt
package br.com.tlmacedo.meuponto.di

import br.com.tlmacedo.meuponto.data.repository.AjusteSaldoRepositoryImpl
import br.com.tlmacedo.meuponto.data.repository.AuditLogRepositoryImpl
import br.com.tlmacedo.meuponto.data.repository.ConfiguracaoEmpregoRepositoryImpl
import br.com.tlmacedo.meuponto.data.repository.EmpregoRepositoryImpl
import br.com.tlmacedo.meuponto.data.repository.FechamentoPeriodoRepositoryImpl
import br.com.tlmacedo.meuponto.data.repository.HorarioDiaSemanaRepositoryImpl
import br.com.tlmacedo.meuponto.data.repository.HorarioPadraoRepositoryImpl
import br.com.tlmacedo.meuponto.data.repository.MarcadorRepositoryImpl
import br.com.tlmacedo.meuponto.data.repository.PontoRepositoryImpl
import br.com.tlmacedo.meuponto.data.repository.VersaoJornadaRepositoryImpl
import br.com.tlmacedo.meuponto.domain.repository.AjusteSaldoRepository
import br.com.tlmacedo.meuponto.domain.repository.AuditLogRepository
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.FechamentoPeriodoRepository
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.HorarioPadraoRepository
import br.com.tlmacedo.meuponto.domain.repository.MarcadorRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para injeção de dependências dos repositórios.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.7.0 - Adicionado VersaoJornadaRepository
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPontoRepository(impl: PontoRepositoryImpl): PontoRepository

    @Binds
    @Singleton
    abstract fun bindEmpregoRepository(impl: EmpregoRepositoryImpl): EmpregoRepository

    @Binds
    @Singleton
    abstract fun bindConfiguracaoEmpregoRepository(impl: ConfiguracaoEmpregoRepositoryImpl): ConfiguracaoEmpregoRepository

    @Binds
    @Singleton
    abstract fun bindHorarioDiaSemanaRepository(impl: HorarioDiaSemanaRepositoryImpl): HorarioDiaSemanaRepository

    @Binds
    @Singleton
    abstract fun bindHorarioPadraoRepository(impl: HorarioPadraoRepositoryImpl): HorarioPadraoRepository

    @Binds
    @Singleton
    abstract fun bindAjusteSaldoRepository(impl: AjusteSaldoRepositoryImpl): AjusteSaldoRepository

    @Binds
    @Singleton
    abstract fun bindFechamentoPeriodoRepository(impl: FechamentoPeriodoRepositoryImpl): FechamentoPeriodoRepository

    @Binds
    @Singleton
    abstract fun bindMarcadorRepository(impl: MarcadorRepositoryImpl): MarcadorRepository

    @Binds
    @Singleton
    abstract fun bindAuditLogRepository(impl: AuditLogRepositoryImpl): AuditLogRepository

    @Binds
    @Singleton
    abstract fun bindVersaoJornadaRepository(impl: VersaoJornadaRepositoryImpl): VersaoJornadaRepository
}
