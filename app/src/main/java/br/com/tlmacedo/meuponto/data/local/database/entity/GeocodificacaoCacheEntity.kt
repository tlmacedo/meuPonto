package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Entidade para cache de geocodificação reversa.
 * Evita chamadas repetidas ao serviço de Geocoder para o mesmo local.
 */
@Entity(
    tableName = "geocodificacao_cache",
    indices = [
        Index(value = ["latitude", "longitude"], unique = true)
    ]
)
data class GeocodificacaoCacheEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val endereco: String,
    val dataCriacao: LocalDateTime = LocalDateTime.now()
)
