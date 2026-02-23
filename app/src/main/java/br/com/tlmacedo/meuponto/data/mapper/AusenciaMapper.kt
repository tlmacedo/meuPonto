// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/mapper/AusenciaMapper.kt
package br.com.tlmacedo.meuponto.data.mapper

import br.com.tlmacedo.meuponto.data.local.database.entity.AusenciaEntity
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import java.time.LocalDateTime

/**
 * Extensões de mapeamento entre Ausencia (domain) e AusenciaEntity (data).
 *
 * @author Thiago
 * @since 4.0.0
 * @updated 5.5.0 - Removido SubTipoFolga
 * @updated 5.7.0 - Corrigido mapeamento de tipoFolga
 */

/**
 * Converte Ausencia (domain) para AusenciaEntity (data).
 */
fun Ausencia.toEntity(): AusenciaEntity {
    return AusenciaEntity(
        id = id,
        empregoId = empregoId,
        tipo = tipo,
        tipoFolga = tipoFolga,  // ✅ ADICIONADO
        dataInicio = dataInicio,
        dataFim = dataFim,
        descricao = descricao,
        observacao = observacao,
        horaInicio = horaInicio,
        duracaoDeclaracaoMinutos = duracaoDeclaracaoMinutos,
        duracaoAbonoMinutos = duracaoAbonoMinutos,
        periodoAquisitivo = periodoAquisitivo,
        imagemUri = imagemUri,
        ativo = ativo,
        criadoEm = criadoEm,
        atualizadoEm = LocalDateTime.now()
    )
}

/**
 * Converte AusenciaEntity (data) para Ausencia (domain).
 */
fun AusenciaEntity.toDomain(): Ausencia {
    return Ausencia(
        id = id,
        empregoId = empregoId,
        tipo = tipo,
        tipoFolga = tipoFolga,  // ✅ ADICIONADO
        dataInicio = dataInicio,
        dataFim = dataFim,
        descricao = descricao,
        observacao = observacao,
        horaInicio = horaInicio,
        duracaoDeclaracaoMinutos = duracaoDeclaracaoMinutos,
        duracaoAbonoMinutos = duracaoAbonoMinutos,
        periodoAquisitivo = periodoAquisitivo,
        imagemUri = imagemUri,
        ativo = ativo,
        criadoEm = criadoEm,
        atualizadoEm = atualizadoEm
    )
}
