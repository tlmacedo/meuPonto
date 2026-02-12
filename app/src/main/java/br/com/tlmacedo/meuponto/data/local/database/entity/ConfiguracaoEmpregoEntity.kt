// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/ConfiguracaoEmpregoEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Entidade Room que armazena as configurações específicas de cada emprego.
 * 
 * Contém todas as configurações relacionadas a jornada de trabalho,
 * banco de horas, validações e preferências do emprego.
 *
 * @property id Identificador único auto-gerado
 * @property empregoId FK para o emprego associado
 * @property jornadaMaximaDiariaMinutos Jornada máxima diária em minutos (default: 600 = 10h)
 * @property intervaloMinimoInterjornadaMinutos Intervalo mínimo entre jornadas em minutos (default: 660 = 11h)
 * @property exigeJustificativaInconsistencia Se true, exige justificativa para registros inconsistentes
 * @property habilitarNsr Se true, habilita o campo NSR no registro de ponto
 * @property tipoNsr Tipo do campo NSR (NUMERICO ou ALFANUMERICO)
 * @property habilitarLocalizacao Se true, habilita captura de localização
 * @property localizacaoAutomatica Se true, captura localização automaticamente
 * @property exibirLocalizacaoDetalhes Se true, exibe localização nos detalhes do ponto
 * @property exibirDuracaoTurno Se true, exibe duração do turno na timeline
 * @property exibirDuracaoIntervalo Se true, exibe duração do intervalo na timeline
 * @property primeiroDiaSemana Primeiro dia da semana para cálculos
 * @property primeiroDiaMes Dia do mês que inicia o período (1-28)
 * @property zerarSaldoSemanal Se true, zera o saldo a cada semana
 * @property zerarSaldoMensal Se true, zera o saldo a cada mês
 * @property ocultarSaldoTotal Se true, oculta o saldo total na interface
 * @property periodoBancoHorasMeses Período do banco de horas em meses (0 = sem banco)
 * @property ultimoFechamentoBanco Data do último fechamento do banco de horas
 * @property diasUteisLembreteFechamento Dias úteis antes do fechamento para notificar
 * @property habilitarSugestaoAjuste Se true, sugere ajuste de horas antes do fechamento
 * @property criadoEm Timestamp de criação
 * @property atualizadoEm Timestamp da última atualização
 *
 * @author Thiago
 * @since 2.0.0
 */
@Entity(
    tableName = "configuracoes_emprego",
    foreignKeys = [
        ForeignKey(
            entity = EmpregoEntity::class,
            parentColumns = ["id"],
            childColumns = ["empregoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["empregoId"], unique = true)]
)
data class ConfiguracaoEmpregoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val empregoId: Long,
    
    // Jornada de Trabalho
    val jornadaMaximaDiariaMinutos: Int = 600, // 10 horas
    val intervaloMinimoInterjornadaMinutos: Int = 660, // 11 horas
    
    // Validações
    val exigeJustificativaInconsistencia: Boolean = false,
    
    // NSR
    val habilitarNsr: Boolean = false,
    val tipoNsr: TipoNsr = TipoNsr.NUMERICO,
    
    // Localização
    val habilitarLocalizacao: Boolean = false,
    val localizacaoAutomatica: Boolean = false,
    val exibirLocalizacaoDetalhes: Boolean = true,
    
    // Exibição
    val exibirDuracaoTurno: Boolean = true,
    val exibirDuracaoIntervalo: Boolean = true,
    
    // Período
    val primeiroDiaSemana: DiaSemana = DiaSemana.SEGUNDA,
    val primeiroDiaMes: Int = 1, // Dia 1 a 28
    
    // Saldo
    val zerarSaldoSemanal: Boolean = false,
    val zerarSaldoMensal: Boolean = false,
    val ocultarSaldoTotal: Boolean = false,
    
    // Banco de Horas
    val periodoBancoHorasMeses: Int = 0, // 0 = sem banco de horas
    val ultimoFechamentoBanco: LocalDate? = null,
    val diasUteisLembreteFechamento: Int = 3,
    val habilitarSugestaoAjuste: Boolean = false,
    
    // Auditoria
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
)
