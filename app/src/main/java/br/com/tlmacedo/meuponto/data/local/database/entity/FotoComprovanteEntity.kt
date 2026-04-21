// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/FotoComprovanteEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.tlmacedo.meuponto.domain.model.FotoComprovante
import br.com.tlmacedo.meuponto.domain.model.FotoOrigem
import br.com.tlmacedo.meuponto.domain.model.TipoJornadaDia
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Entidade Room para armazenamento de metadados das fotos de comprovante.
 *
 * Esta entidade armazena informações completas (snapshot) no momento do registro,
 * permitindo rastreabilidade, auditoria e verificação de integridade.
 *
 * ## Metadados Armazenados:
 * - **Dados do Ponto**: ID, data, hora, índice (para tipo dinâmico), NSR
 * - **Localização**: Coordenadas GPS, altitude, precisão, endereço
 * - **Jornada**: Versão, tipo do dia, horas trabalhadas, saldos
 * - **Foto**: Path, timestamp, origem, tamanho, hash MD5
 * - **Controle**: Sincronização com nuvem, auditoria
 *
 * ## Tipo de Ponto Dinâmico:
 * O tipo (entrada/saída) é calculado dinamicamente através do campo `indicePontoDia`:
 * - Ímpar (1, 3, 5...) = Entrada
 * - Par (2, 4, 6...) = Saída
 *
 * Isso permite ajustes posteriores sem inconsistências.
 *
 * ## Importância do NSR:
 * O NSR (Número Sequencial de Registro) é capturado do ponto vinculado e
 * armazenado como snapshot para fins de:
 * - Rastreabilidade com o sistema de ponto do empregador
 * - Auditoria e verificação de registros
 * - Conformidade com requisitos legais (Portaria 671/MTE)
 * - Comprovação em caso de disputas trabalhistas
 *
 * @author Thiago
 * @since 10.0.0
 * @see PontoEntity
 */
@Entity(
    tableName = "fotos_comprovante",
    foreignKeys = [
        ForeignKey(
            entity = PontoEntity::class,
            parentColumns = ["id"],
            childColumns = ["pontoId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["pontoId"], unique = true),
        Index(value = ["empregoId"]),
        Index(value = ["data"]),
        Index(value = ["sincronizadoNuvem"])
    ]
)
data class FotoComprovanteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // ════════════════════════════════════════════════════════════════════════
    // VÍNCULO COM PONTO
    // ════════════════════════════════════════════════════════════════════════

    /**
     * ID do registro de ponto vinculado.
     * Relacionamento 1:1 com a tabela pontos.
     */
    val pontoId: Long,

    /**
     * ID do emprego (denormalizado para consultas eficientes).
     */
    val empregoId: Long,

    // ════════════════════════════════════════════════════════════════════════
    // DADOS DO PONTO (Snapshot no momento do registro)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Data do registro de ponto.
     * Formato: LocalDate (armazenado como TEXT via TypeConverter)
     */
    val data: LocalDate,

    /**
     * Dia da semana do registro.
     * Ex: MONDAY, TUESDAY, etc.
     */
    val diaSemana: DayOfWeek,

    /**
     * Hora do registro de ponto.
     * Formato: LocalTime (armazenado como TEXT via TypeConverter)
     */
    val hora: LocalTime,

    /**
     * Índice do ponto no dia (1-based).
     * Usado para determinar dinamicamente o tipo:
     * - Ímpar (1, 3, 5...) = Entrada
     * - Par (2, 4, 6...) = Saída
     *
     * Isso permite que ajustes posteriores (inserção/remoção de pontos)
     * não causem inconsistências no tipo armazenado.
     */
    val indicePontoDia: Int,

    /**
     * NSR - Número Sequencial de Registro.
     *
     * Identificador sequencial gerado pelo sistema de ponto eletrônico do empregador.
     * Este campo é CRUCIAL para:
     * - Rastreabilidade: Vincula o registro do app ao registro oficial
     * - Auditoria: Permite verificação e conciliação de registros
     * - Comprovação: Serve como evidência em caso de disputas
     * - Conformidade: Atende requisitos da Portaria 671/MTE
     *
     * O NSR é copiado do PontoEntity no momento da criação da foto,
     * garantindo que mesmo se o ponto for alterado, o snapshot permanece.
     *
     * Pode ser nulo se a funcionalidade NSR não estiver habilitada no emprego.
     */
    val nsr: String? = null,

    // ════════════════════════════════════════════════════════════════════════
    // LOCALIZAÇÃO (Se disponível no momento do registro)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Latitude GPS em graus decimais.
     * Range válido: -90.0 a +90.0
     */
    val latitude: Double? = null,

    /**
     * Longitude GPS em graus decimais.
     * Range válido: -180.0 a +180.0
     */
    val longitude: Double? = null,

    /**
     * Altitude em metros acima do nível do mar.
     */
    val altitude: Double? = null,

    /**
     * Precisão da localização em metros.
     * Quanto menor, mais precisa a localização.
     */
    val precisaoMetros: Float? = null,

    /**
     * Endereço formatado obtido via geocoding reverso.
     * Ex: "Av. Eduardo Ribeiro, 520 - Centro, Manaus - AM"
     */
    val enderecoFormatado: String? = null,

    // ════════════════════════════════════════════════════════════════════════
    // DADOS DA JORNADA (Snapshot no momento do registro)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Versão da configuração de jornada vigente no momento do registro.
     * Permite identificar qual configuração de jornada estava ativa.
     */
    val versaoJornada: Int,

    /**
     * Tipo de jornada do dia: NORMAL, FERIADO, FOLGA, etc.
     */
    val tipoJornadaDia: TipoJornadaDia,

    /**
     * Total de horas trabalhadas no dia em MINUTOS.
     * Calculado considerando todos os pontos do dia até o momento.
     */
    val horasTrabalhadasDiaMinutos: Long,

    /**
     * Saldo do dia em MINUTOS (pode ser negativo).
     * Diferença entre horas trabalhadas e carga horária esperada.
     */
    val saldoDiaMinutos: Long,

    /**
     * Saldo total acumulado do banco de horas em MINUTOS (pode ser negativo).
     * Inclui o saldo do dia atual.
     */
    val saldoBancoHorasMinutos: Long,

    // ════════════════════════════════════════════════════════════════════════
    // METADADOS DA FOTO
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Caminho relativo do arquivo da foto no armazenamento interno.
     * Ex: "comprovantes/emprego_1/2026/03/ponto_124398_1741716969.jpg"
     */
    val fotoPath: String,

    /**
     * Timestamp UTC do momento exato da captura/seleção da foto.
     * Armazenado como Instant para precisão e universalidade.
     */
    val fotoTimestamp: Instant,

    /**
     * Origem da foto: CAMERA ou GALERIA.
     */
    val fotoOrigem: FotoOrigem,

    /**
     * Tamanho do arquivo da foto em bytes.
     */
    val fotoTamanhoBytes: Long,

    /**
     * Hash MD5 do arquivo para verificação de integridade.
     * Formato: String hexadecimal de 32 caracteres.
     * Ex: "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"
     */
    val fotoHashMd5: String,

    /**
     * Observação do registro de ponto vinculado.
     * Copiado do PontoEntity para evitar JOINs custosos na galeria.
     */
    val observacao: String? = null,

    // ════════════════════════════════════════════════════════════════════════
    // CONTROLE DE SINCRONIZAÇÃO COM NUVEM
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Indica se a foto foi sincronizada com a nuvem.
     */
    val sincronizadoNuvem: Boolean = false,

    /**
     * Timestamp da última sincronização bem-sucedida.
     * Nulo se nunca foi sincronizado.
     */
    val sincronizadoEm: Instant? = null,

    /**
     * Identificador do arquivo na nuvem (Google Drive, etc).
     * Nulo se não sincronizado.
     */
    val cloudFileId: String? = null,

    // ════════════════════════════════════════════════════════════════════════
    // AUDITORIA
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Timestamp de criação do registro.
     */
    val criadoEm: Instant = Instant.now(),

    /**
     * Timestamp da última atualização.
     */
    val atualizadoEm: Instant = Instant.now()
)

// ════════════════════════════════════════════════════════════════════════════
// MAPPERS
// ════════════════════════════════════════════════════════════════════════════

/**
 * Converte FotoComprovanteEntity para FotoComprovante (domain).
 */
fun FotoComprovanteEntity.toDomain(): FotoComprovante = FotoComprovante(
    id = id,
    pontoId = pontoId,
    empregoId = empregoId,
    data = data,
    diaSemana = diaSemana,
    hora = hora,
    indicePontoDia = indicePontoDia,
    nsr = nsr,
    latitude = latitude,
    longitude = longitude,
    altitude = altitude,
    precisaoMetros = precisaoMetros,
    enderecoFormatado = enderecoFormatado,
    versaoJornada = versaoJornada,
    tipoJornadaDia = tipoJornadaDia,
    horasTrabalhadasDiaMinutos = horasTrabalhadasDiaMinutos,
    saldoDiaMinutos = saldoDiaMinutos,
    saldoBancoHorasMinutos = saldoBancoHorasMinutos,
    fotoPath = fotoPath,
    fotoTimestamp = fotoTimestamp,
    fotoOrigem = fotoOrigem,
    fotoTamanhoBytes = fotoTamanhoBytes,
    fotoHashMd5 = fotoHashMd5,
    observacao = observacao,
    sincronizadoNuvem = sincronizadoNuvem,
    sincronizadoEm = sincronizadoEm,
    cloudFileId = cloudFileId,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm
)

/**
 * Converte FotoComprovante (domain) para FotoComprovanteEntity.
 */
fun FotoComprovante.toEntity(): FotoComprovanteEntity = FotoComprovanteEntity(
    id = id,
    pontoId = pontoId,
    empregoId = empregoId,
    data = data,
    diaSemana = diaSemana,
    hora = hora,
    indicePontoDia = indicePontoDia,
    nsr = nsr,
    latitude = latitude,
    longitude = longitude,
    altitude = altitude,
    precisaoMetros = precisaoMetros,
    enderecoFormatado = enderecoFormatado,
    versaoJornada = versaoJornada,
    tipoJornadaDia = tipoJornadaDia,
    horasTrabalhadasDiaMinutos = horasTrabalhadasDiaMinutos,
    saldoDiaMinutos = saldoDiaMinutos,
    saldoBancoHorasMinutos = saldoBancoHorasMinutos,
    fotoPath = fotoPath,
    fotoTimestamp = fotoTimestamp,
    fotoOrigem = fotoOrigem,
    fotoTamanhoBytes = fotoTamanhoBytes,
    fotoHashMd5 = fotoHashMd5,
    observacao = observacao,
    sincronizadoNuvem = sincronizadoNuvem,
    sincronizadoEm = sincronizadoEm,
    cloudFileId = cloudFileId,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm
)
