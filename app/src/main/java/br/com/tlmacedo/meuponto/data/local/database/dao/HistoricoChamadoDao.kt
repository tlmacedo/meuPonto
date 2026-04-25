// path: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/dao/HistoricoChamadoDao.kt
package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.tlmacedo.meuponto.data.local.database.entity.HistoricoChamadoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoricoChamadoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(historico: HistoricoChamadoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodos(historicos: List<HistoricoChamadoEntity>)

    @Query("SELECT * FROM historico_chamados WHERE chamadoId = :chamadoId ORDER BY criadoEm DESC")
    fun listarPorChamado(chamadoId: Long): Flow<List<HistoricoChamadoEntity>>

    @Query("SELECT * FROM historico_chamados WHERE chamadoId = :chamadoId ORDER BY criadoEm DESC")
    suspend fun buscarPorChamado(chamadoId: Long): List<HistoricoChamadoEntity>

    @Query("SELECT * FROM historico_chamados WHERE id = :id")
    suspend fun buscarPorId(id: Long): HistoricoChamadoEntity?

    @Query("DELETE FROM historico_chamados WHERE chamadoId = :chamadoId")
    suspend fun excluirPorChamado(chamadoId: Long)
}