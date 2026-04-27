package br.com.tlmacedo.meuponto.di.wear

import br.com.tlmacedo.meuponto.data.service.wear.WearSyncServiceImpl
import br.com.tlmacedo.meuponto.domain.service.wear.WearSyncService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WearModule {

    @Binds
    @Singleton
    abstract fun bindWearSyncService(
        wearSyncServiceImpl: WearSyncServiceImpl
    ): WearSyncService
}
