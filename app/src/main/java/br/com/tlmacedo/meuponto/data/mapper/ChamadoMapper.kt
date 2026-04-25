// path: app/src/main/java/br/com/tlmacedo/meuponto/data/mapper/ChamadoMapper.kt
package br.com.tlmacedo.meuponto.data.mapper

import br.com.tlmacedo.meuponto.data.local.database.entity.ChamadoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.HistoricoChamadoEntity
import br.com.tlmacedo.meuponto.domain.model.chamado.*
import java.time.LocalDateTime

fun ChamadoEntity.toDomain(): Chamado = Chamado(
    id = id,
    identificador = identificador,
    titulo = titulo,
    descricao = descricao,
    categoria = runCatching { CategoriaChamado.valueOf(categoria) }
        .getOrDefault(CategoriaChamado.OUTRO),
    status = runCatching { StatusChamado.valueOf(status) }
        .getOrDefault(StatusChamado.ABERTO),
    prioridade = runCatching { PrioridadeChamado.valueOf(prioridade) }
        .getOrDefault(PrioridadeChamado.MEDIA),
    empregoId = empregoId,
    usuarioEmail = usuarioEmail,
    usuarioNome = usuarioNome,
    resposta = resposta,
    anexos = anexos
        ?.removeSurrounding("[", "]")
        ?.split(",")
        ?.map { it.trim().removeSurrounding("\"") }
        ?.filter { it.isNotEmpty() }
        ?.let { ArrayList(it) },
    avaliacaoNota = if (avaliacaoNota != null) AvaliacaoChamado(
        nota = avaliacaoNota.toIntOrNull() ?: 0,
        comentario = avaliacaoComentario,
        avaliadoEm = avaliadoEm?.let {
            runCatching { LocalDateTime.parse(it) }.getOrDefault(LocalDateTime.now())
        } ?: LocalDateTime.now()
    ) else null,
    avaliacaoComentario = avaliacaoComentario,
    avaliadoEm = avaliadoEm?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() },
    resolvidoEm = resolvidoEm?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() },
    criadoEm = runCatching { LocalDateTime.parse(criadoEm) }.getOrDefault(LocalDateTime.now()),
    atualizadoEm = runCatching { LocalDateTime.parse(atualizadoEm) }.getOrDefault(LocalDateTime.now())
)

fun Chamado.toEntity(): ChamadoEntity = ChamadoEntity(
    id = id,
    identificador = identificador,
    titulo = titulo,
    descricao = descricao,
    categoria = categoria.name,
    status = status.name,
    prioridade = prioridade.name,
    empregoId = empregoId,
    usuarioNome = usuarioNome,
    usuarioEmail = usuarioEmail,
    resposta = resposta,
    anexos = anexos?.let { list ->
        "[${list.joinToString(",") { "\"$it\"" }}]"
    },
    avaliacaoNota = avaliacaoNota?.nota?.toString(),
    avaliacaoComentario = avaliacaoComentario,
    avaliadoEm = avaliadoEm?.toString(),
    resolvidoEm = resolvidoEm?.toString(),
    criadoEm = criadoEm.toString(),
    atualizadoEm = atualizadoEm.toString()
)

fun List<ChamadoEntity>.toDomain(): List<Chamado> = map { it.toDomain() }
fun List<Chamado>.toEntity(): List<ChamadoEntity> = map { it.toEntity() }

fun HistoricoChamadoEntity.toDomain(): HistoricoChamado = HistoricoChamado(
    id = id,
    chamadoId = chamadoId,
    statusAnterior = statusAnterior?.let { StatusChamado.valueOf(it) },
    statusNovo = StatusChamado.valueOf(statusNovo),
    mensagem = mensagem,
    autor = autor,
    criadoEm = LocalDateTime.parse(criadoEm)
)

fun HistoricoChamado.toEntity(): HistoricoChamadoEntity = HistoricoChamadoEntity(
    id = id,
    chamadoId = chamadoId,
    chamadoIdentificador = "", // Preencher se necessário
    statusAnterior = statusAnterior?.name,
    statusNovo = statusNovo.name,
    mensagem = mensagem,
    autor = autor,
    criadoEm = criadoEm.toString()
)
