package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.tlmacedo.meuponto.data.local.database.entity.HistoricoCargoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoricoCargoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(historico: HistoricoCargoEntity): Long

    @Update
    suspend fun atualizar(historico: HistoricoCargoEntity)

    @Delete
    suspend fun excluir(historico: HistoricoCargoEntity)

    @Query("SELECT * FROM historico_cargos WHERE empregoId = :empregoId ORDER BY dataInicio DESC")
    fun listarPorEmprego(empregoId: Long): Flow<List<HistoricoCargoEntity>>

    @Query("SELECT * FROM historico_cargos WHERE id = :id")
    suspend fun buscarPorId(id: Long): HistoricoCargoEntity?
}
