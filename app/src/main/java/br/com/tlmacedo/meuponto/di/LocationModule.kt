package br.com.tlmacedo.meuponto.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {
    // LocationService is provided via @Inject constructor and @Singleton annotation on the class itself.
    // No explicit @Provides is needed here unless custom configuration is required.
}
