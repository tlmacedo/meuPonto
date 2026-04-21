// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/FotoComprovante.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Modelo de domínio para foto de comprovante de ponto.
 *
 * Representa uma foto vinculada a um registro de ponto, contendo todos os
 * metadados capturados no momento do registro para fins de rastreabilidade,
 * auditoria e comprovação.
 *
 * ## Estrutura de Metadados:
 *
 * ### Dados do Ponto (Snapshot)
 * - ID, data, hora
 * - NSR (Número Sequencial de Registro) - crucial para rastreabilidade
 * - Índice do ponto no dia (para determinar tipo: entrada/saída)
 *
 * ### Localização (Opcional)
 * - Coordenadas GPS (latitude, longitude, altitude)
 * - Precisão em metros
 * - Endereço formatado (geocoding reverso)
 *
 * ### Dados da Jornada (Snapshot)
 * - Versão da jornada vigente
 * - Tipo do dia (normal, feriado, folga, etc.)
 * - Horas trabalhadas no dia
 * - Saldo do dia e banco de horas
 *
 * ### Dados da Foto
 * - Caminho do arquivo
 * - Timestamp de captura
 * - Origem (câmera ou galeria)
 * - Tamanho e hash MD5 para integridade
 *
 * @author Thiago
 * @since 10.0.0
 */
data class FotoComprovante(
    val id: Long = 0,

    // Vínculo
    val pontoId: Long,
    val empregoId: Long,

    // Dados do Ponto
    val data: LocalDate,
    val diaSemana: DayOfWeek,
    val hora: LocalTime,

    /**
     * Índice do ponto no dia (1-based).
     * Usado para determinar dinamicamente o tipo:
     * - Ímpar (1, 3, 5...) = Entrada
     * - Par (2, 4, 6...) = Saída
     */
    val indicePontoDia: Int,

    val nsr: String? = null,

    // Localização
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val precisaoMetros: Float? = null,
    val enderecoFormatado: String? = null,

    // Jornada
    val versaoJornada: Int,
    val tipoJornadaDia: TipoJornadaDia,
    val horasTrabalhadasDiaMinutos: Long,
    val saldoDiaMinutos: Long,
    val saldoBancoHorasMinutos: Long,

    // Foto
    val fotoPath: String,
    val fotoTimestamp: Instant,
    val fotoOrigem: FotoOrigem,
    val fotoTamanhoBytes: Long,
    val fotoHashMd5: String,

    // Observação do Ponto (opcional)
    val observacao: String? = null,

    // Sincronização
    val sincronizadoNuvem: Boolean = false,
    val sincronizadoEm: Instant? = null,
    val cloudFileId: String? = null,

    // Auditoria
    val criadoEm: Instant = Instant.now(),
    val atualizadoEm: Instant = Instant.now()
) {
    // ════════════════════════════════════════════════════════════════════════
    // PROPRIEDADES DERIVADAS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Determina se é entrada ou saída baseado no índice do ponto no dia.
     * Ímpar = Entrada, Par = Saída
     */
    val isEntrada: Boolean
        get() = indicePontoDia % 2 == 1

    /**
     * Determina se é entrada ou saída baseado no índice do ponto no dia.
     * Par = Saída, Ímpar = Entrada
     */
    val isSaida: Boolean
        get() = indicePontoDia % 2 == 0

    /**
     * Texto descritivo do tipo de ponto.
     */
    val tipoPontoDescricao: String
        get() = if (isEntrada) "Entrada" else "Saída"

    /** Verifica se tem localização completa */
    val temLocalizacao: Boolean
        get() = latitude != null && longitude != null

    /** Verifica se tem NSR */
    val temNsr: Boolean
        get() = !nsr.isNullOrBlank()

    /** Verifica se tem observação */
    val temObservacao: Boolean
        get() = !observacao.isNullOrBlank()

    /** Verifica se tem endereço formatado */
    val temEndereco: Boolean
        get() = !enderecoFormatado.isNullOrBlank()

    /** Verifica se está sincronizado com a nuvem */
    val estaSincronizado: Boolean
        get() = sincronizadoNuvem && cloudFileId != null

    /** Horas trabalhadas como Duration */
    val horasTrabalhadasDia: Duration
        get() = Duration.ofMinutes(horasTrabalhadasDiaMinutos)

    /** Saldo do dia como Duration */
    val saldoDia: Duration
        get() = Duration.ofMinutes(saldoDiaMinutos)

    /** Saldo do banco de horas como Duration */
    val saldoBancoHoras: Duration
        get() = Duration.ofMinutes(saldoBancoHorasMinutos)

    /** Tamanho da foto em KB */
    val fotoTamanhoKb: Double
        get() = fotoTamanhoBytes / 1024.0

    /** Tamanho da foto em MB */
    val fotoTamanhoMb: Double
        get() = fotoTamanhoBytes / (1024.0 * 1024.0)

    // ════════════════════════════════════════════════════════════════════════
    // FORMATADORES
    // ════════════════════════════════════════════════════════════════════════

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

    /** Data formatada (ex: "11/03/2026") */
    val dataFormatada: String
        get() = data.format(dateFormatter)

    /** Hora formatada (ex: "15:16") */
    val horaFormatada: String
        get() = hora.format(timeFormatter)

    /** Dia da semana formatado em português (ex: "Quarta-feira") */
    val diaSemanaFormatado: String
        get() = diaSemana.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"))
            .replaceFirstChar { it.uppercase() }

    /** Timestamp da foto formatado */
    val fotoTimestampFormatado: String
        get() = fotoTimestamp
            .atZone(ZoneId.systemDefault())
            .format(dateTimeFormatter)

    /** Horas trabalhadas formatadas (ex: "4h 36min") */
    val horasTrabalhadasFormatada: String
        get() = formatarDuracao(horasTrabalhadasDia)

    /** Saldo do dia formatado (ex: "+1h 30min" ou "-2h 15min") */
    val saldoDiaFormatado: String
        get() = formatarDuracaoComSinal(saldoDia)

    /** Saldo do banco formatado (ex: "+10h 45min" ou "-5h 30min") */
    val saldoBancoHorasFormatado: String
        get() = formatarDuracaoComSinal(saldoBancoHoras)

    /** Tamanho da foto formatado (ex: "256.5 KB" ou "1.2 MB") */
    val fotoTamanhoFormatado: String
        get() = when {
            fotoTamanhoBytes < 1024 -> "$fotoTamanhoBytes B"
            fotoTamanhoBytes < 1024 * 1024 -> String.format("%.1f KB", fotoTamanhoKb)
            else -> String.format("%.2f MB", fotoTamanhoMb)
        }

    /** Coordenadas formatadas (ex: "-3.119028, -60.021731") */
    val coordenadasFormatadas: String?
        get() = if (temLocalizacao) {
            String.format("%.6f, %.6f", latitude, longitude)
        } else null

    /** Precisão formatada (ex: "±10m") */
    val precisaoFormatada: String?
        get() = precisaoMetros?.let { "±${it.toInt()}m" }

    // ════════════════════════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES
    // ════════════════════════════════════════════════════════════════════════

    private fun formatarDuracao(duracao: Duration): String {
        val horas = duracao.toHours()
        val minutos = duracao.toMinutes() % 60
        return when {
            horas == 0L && minutos == 0L -> "0min"
            horas == 0L -> "${minutos}min"
            minutos == 0L -> "${horas}h"
            else -> "${horas}h ${minutos}min"
        }
    }

    private fun formatarDuracaoComSinal(duracao: Duration): String {
        val sinal = if (duracao.isNegative) "-" else "+"
        val duracaoAbsoluta = duracao.abs()
        return "$sinal${formatarDuracao(duracaoAbsoluta)}"
    }

    /**
     * Gera um JSON compacto com os metadados principais.
     * Usado para embutir no EXIF da imagem.
     */
    fun toMetadataJson(): String {
        return buildString {
            append("{")
            append("\"app\":\"MeuPonto\",")
            append("\"ponto\":{")
            append("\"id\":$pontoId,")
            append("\"data\":\"$data\",")
            append("\"hora\":\"$hora\",")
            append("\"idx\":$indicePontoDia,")
            append("\"tipo\":\"${if (isEntrada) "E" else "S"}\"")
            nsr?.let { append(",\"nsr\":\"$it\"") }
            append("},")

            if (temLocalizacao) {
                append("\"loc\":{")
                append("\"lat\":$latitude,")
                append("\"lon\":$longitude")
                altitude?.let { append(",\"alt\":$it") }
                append("},")
            }

            append("\"jornada\":{")
            append("\"ver\":$versaoJornada,")
            append("\"tipo\":\"${tipoJornadaDia.name}\",")
            append("\"trab\":$horasTrabalhadasDiaMinutos,")
            append("\"saldoDia\":$saldoDiaMinutos,")
            append("\"saldoTotal\":$saldoBancoHorasMinutos")
            append("},")

            append("\"hash\":\"$fotoHashMd5\"")
            append("}")
        }
    }

    /**
     * Gera o UserComment para EXIF.
     * Formato: "ponto:ID;emprego:ID;idx:N;nsr:VALOR"
     */
    fun toExifUserComment(): String {
        return buildString {
            append("ponto:$pontoId")
            append(";emprego:$empregoId")
            append(";idx:$indicePontoDia")
            nsr?.let { append(";nsr:$it") }
        }
    }

    companion object {
        /**
         * Cria uma instância com dados mínimos para testes.
         */
        fun criarParaTeste(
            pontoId: Long,
            empregoId: Long,
            fotoPath: String,
            indicePontoDia: Int = 1
        ): FotoComprovante = FotoComprovante(
            pontoId = pontoId,
            empregoId = empregoId,
            data = LocalDate.now(),
            diaSemana = LocalDate.now().dayOfWeek,
            hora = LocalTime.now(),
            indicePontoDia = indicePontoDia,
            versaoJornada = 1,
            tipoJornadaDia = TipoJornadaDia.NORMAL,
            horasTrabalhadasDiaMinutos = 0,
            saldoDiaMinutos = 0,
            saldoBancoHorasMinutos = 0,
            fotoPath = fotoPath,
            fotoTimestamp = Instant.now(),
            fotoOrigem = FotoOrigem.CAMERA,
            fotoTamanhoBytes = 0,
            fotoHashMd5 = ""
        )
    }
}
