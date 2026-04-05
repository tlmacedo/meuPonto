package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.*
import br.com.tlmacedo.meuponto.data.local.database.entity.AjusteSalarialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AjusteSalarialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(ajuste: AjusteSalarialEntity): Long

    @Update
    suspend fun atualizar(ajuste: AjusteSalarialEntity)

    @Delete
    suspend fun excluir(ajuste: AjusteSalarialEntity)

    @Query("SELECT * FROM ajustes_salariais WHERE historicoCargoId = :historicoCargoId ORDER BY dataAjuste DESC")
    fun listarPorHistoricoCargo(historicoCargoId: Long): Flow<List<AjusteSalarialEntity>>

    @Query("SELECT * FROM ajustes_salariais WHERE id = :id")
    suspend fun buscarPorId(id: Long): AjusteSalarialEntity?
}
