package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import java.time.LocalDateTime

/**
 * Entidade Room que representa um registro de ponto no banco de dados.
 *
 * Esta classe é responsável pelo mapeamento entre o modelo de domínio
 * e a tabela do banco de dados SQLite.
 *
 * @property id Identificador único do registro (auto-gerado)
 * @property dataHora Data e hora da batida de ponto
 * @property tipo Tipo da batida (ENTRADA, SAIDA_ALMOCO, RETORNO_ALMOCO, SAIDA)
 * @property editadoManualmente Indica se o registro foi editado manualmente
 * @property observacao Observação opcional do usuário
 * @property criadoEm Data/hora de criação do registro
 * @property atualizadoEm Data/hora da última atualização
 *
 * @author Thiago
 * @since 1.0.0
 */
@Entity(tableName = "pontos")
data class PontoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "data_hora")
    val dataHora: LocalDateTime,

    @ColumnInfo(name = "tipo")
    val tipo: TipoPonto,

    @ColumnInfo(name = "editado_manualmente")
    val editadoManualmente: Boolean = false,

    @ColumnInfo(name = "observacao")
    val observacao: String? = null,

    @ColumnInfo(name = "criado_em")
    val criadoEm: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "atualizado_em")
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Converte a entidade do banco de dados para o modelo de domínio.
     *
     * @return Modelo de domínio Ponto
     */
    fun toDomain(): Ponto {
        return Ponto(
            id = id,
            dataHora = dataHora,
            tipo = tipo,
            editadoManualmente = editadoManualmente,
            observacao = observacao,
            criadoEm = criadoEm,
            atualizadoEm = atualizadoEm
        )
    }

    companion object {
        /**
         * Converte um modelo de domínio para entidade do banco de dados.
         *
         * @param ponto Modelo de domínio
         * @return Entidade para persistência
         */
        fun fromDomain(ponto: Ponto): PontoEntity {
            return PontoEntity(
                id = ponto.id,
                dataHora = ponto.dataHora,
                tipo = ponto.tipo,
                editadoManualmente = ponto.editadoManualmente,
                observacao = ponto.observacao,
                criadoEm = ponto.criadoEm,
                atualizadoEm = ponto.atualizadoEm
            )
        }
    }
}
