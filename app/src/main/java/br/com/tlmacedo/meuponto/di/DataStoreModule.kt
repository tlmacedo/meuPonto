package br.com.tlmacedo.meuponto.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// PreferencesDataStore foi migrado para PreferenciasGlobaisDataStore (13.0.0).
// PreferenciasGlobaisDataStore é injetado automaticamente via @Singleton + @Inject.
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule
