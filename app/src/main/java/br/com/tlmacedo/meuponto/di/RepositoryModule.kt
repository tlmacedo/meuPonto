package br.com.tlmacedo.meuponto.di

import br.com.tlmacedo.meuponto.data.repository.*
import br.com.tlmacedo.meuponto.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindPontoRepository(impl: PontoRepositoryImpl): PontoRepository
    @Binds @Singleton abstract fun bindEmpregoRepository(impl: EmpregoRepositoryImpl): EmpregoRepository
    @Binds @Singleton abstract fun bindConfiguracaoEmpregoRepository(impl: ConfiguracaoEmpregoRepositoryImpl): ConfiguracaoEmpregoRepository
    @Binds @Singleton abstract fun bindHorarioDiaSemanaRepository(impl: HorarioDiaSemanaRepositoryImpl): HorarioDiaSemanaRepository
    @Binds @Singleton abstract fun bindHorarioPadraoRepository(impl: HorarioPadraoRepositoryImpl): HorarioPadraoRepository
    @Binds @Singleton abstract fun bindAjusteSaldoRepository(impl: AjusteSaldoRepositoryImpl): AjusteSaldoRepository
    @Binds @Singleton abstract fun bindFechamentoPeriodoRepository(impl: FechamentoPeriodoRepositoryImpl): FechamentoPeriodoRepository
    @Binds @Singleton abstract fun bindMarcadorRepository(impl: MarcadorRepositoryImpl): MarcadorRepository
    @Binds @Singleton abstract fun bindAuditLogRepository(impl: AuditLogRepositoryImpl): AuditLogRepository
    @Binds @Singleton abstract fun bindVersaoJornadaRepository(impl: VersaoJornadaRepositoryImpl): VersaoJornadaRepository
    @Binds @Singleton abstract fun bindAusenciaRepository(impl: AusenciaRepositoryImpl): AusenciaRepository
    @Binds @Singleton abstract fun bindLixeiraRepository(impl: LixeiraRepositoryImpl): LixeiraRepository
}
