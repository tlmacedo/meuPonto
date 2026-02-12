package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.tlmacedo.meuponto.data.local.database.entity.PontoEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PontoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(ponto: PontoEntity): Long

    @Update
    suspend fun atualizar(ponto: PontoEntity)

    @Delete
    suspend fun excluir(ponto: PontoEntity)

    @Query("SELECT * FROM pontos WHERE id = :id")
    suspend fun buscarPorId(id: Long): PontoEntity?

    @Query("SELECT * FROM pontos WHERE data = :data ORDER BY dataHora ASC")
    fun listarPorData(data: LocalDate): Flow<List<PontoEntity>>

    @Query("SELECT * FROM pontos ORDER BY dataHora DESC")
    fun listarTodos(): Flow<List<PontoEntity>>

    @Query("SELECT * FROM pontos WHERE data BETWEEN :dataInicio AND :dataFim ORDER BY dataHora ASC")
    fun listarPorPeriodo(dataInicio: LocalDate, dataFim: LocalDate): Flow<List<PontoEntity>>
}
