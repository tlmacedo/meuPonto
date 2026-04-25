package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.tlmacedo.meuponto.data.local.database.entity.GeocodificacaoCacheEntity

@Dao
interface GeocodificacaoCacheDao {

    @Query("SELECT * FROM geocodificacao_cache WHERE latitude = :latitude AND longitude = :longitude LIMIT 1")
    suspend fun buscarPorCoordenadas(
        latitude: Double,
        longitude: Double
    ): GeocodificacaoCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(cache: GeocodificacaoCacheEntity)

    @Query("DELETE FROM geocodificacao_cache WHERE dataCriacao < :dataLimite")
    suspend fun limparCacheAntigo(dataLimite: java.time.LocalDateTime)
}
