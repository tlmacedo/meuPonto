package br.com.tlmacedo.meuponto.domain.usecase.foto

import br.com.tlmacedo.meuponto.domain.extensions.toTipoJornadaDia
import br.com.tlmacedo.meuponto.domain.model.TipoJornadaDia
import br.com.tlmacedo.meuponto.domain.extensions.toTipoJornadaDia
import br.com.tlmacedo.meuponto.domain.extensions.isAusenciaOrFalse
import br.com.tlmacedo.meuponto.domain.model.FotoComprovante
import br.com.tlmacedo.meuponto.domain.model.FotoOrigem
import br.com.tlmacedo.meuponto.domain.repository.FotoComprovanteRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularBancoHorasUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ObterResumoDiaCompletoUseCase
import br.com.tlmacedo.meuponto.util.ComprovanteImageStorage
import br.com.tlmacedo.meuponto.util.foto.ImageHashCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.time.Instant
import javax.inject.Inject

/**
 * Caso de uso para reconciliar fotos locais com o banco de dados.
 * Varre o diretório de comprovantes e importa arquivos órfãos que possuam
 * um registro de ponto correspondente no banco.
 */
class ReconciliarFotosUseCase @Inject constructor(
    private val fotoRepository: FotoComprovanteRepository,
    private val pontoRepository: PontoRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val obterResumoUseCase: ObterResumoDiaCompletoUseCase,
    private val calcularBancoUseCase: CalcularBancoHorasUseCase,
    private val storage: ComprovanteImageStorage,
    private val hashCalculator: ImageHashCalculator
) {

    data class ReconciliacaoResult(
        val importados: Int = 0,
        val falhas: Int = 0,
        val jaExistentes: Int = 0
    )

    suspend fun invoke(): ReconciliacaoResult = withContext(Dispatchers.IO) {
        val rootDir = storage.getComprovantesDirectory()
        if (!rootDir.exists()) return@withContext ReconciliacaoResult()

        var importados = 0
        var falhas = 0
        var jaExistentes = 0

        val regexArquivo = Regex("""ponto_(\d+)_.*\.jpg""")
        val regexEmprego = Regex("""emprego_(\d+)""")

        try {
            rootDir.walkTopDown()
                .filter { it.isFile && it.extension.lowercase() == "jpg" }
                .forEach { file ->
                    val relativePath = file.relativeTo(rootDir).path

                    // Verifica se já existe no banco pelo path
                    // Nota: O repositório não tem busca por path direto, mas podemos verificar o hash
                    val hash = hashCalculator.calculateMd5(file) ?: ""
                    if (hash.isNotEmpty() && fotoRepository.buscarPorHash(hash) != null) {
                        jaExistentes++
                        return@forEach
                    }

                    // Extrair IDs do caminho
                    val matchArquivo = regexArquivo.find(file.name)
                    val pontoId =
                        matchArquivo?.groupValues?.get(1)?.toLongOrNull() ?: return@forEach

                    val matchEmprego = regexEmprego.find(file.absolutePath)
                    val empregoId =
                        matchEmprego?.groupValues?.get(1)?.toLongOrNull() ?: return@forEach

                    val ponto = pontoRepository.buscarPorId(pontoId)
                    if (ponto != null) {
                        try {
                            val importado = importarFoto(file, relativePath, hash, ponto, empregoId)
                            if (importado) importados++ else falhas++
                        } catch (e: Exception) {
                            Timber.e(e, "Falha ao importar foto órfã: $relativePath")
                            falhas++
                        }
                    } else {
                        Timber.w("Ponto $pontoId não encontrado para foto órfã: $relativePath")
                        falhas++
                    }
                }
        } catch (e: Exception) {
            Timber.e(e, "Erro durante varredura de reconciliação")
        }

        ReconciliacaoResult(importados, falhas, jaExistentes)
    }

    private suspend fun importarFoto(
        file: File,
        relativePath: String,
        hash: String,
        ponto: br.com.tlmacedo.meuponto.domain.model.Ponto,
        empregoId: Long
    ): Boolean {
        // Obter metadados para o Snapshot
        val versao = versaoJornadaRepository.buscarPorEmpregoEData(empregoId, ponto.data)
            ?: versaoJornadaRepository.buscarVigente(empregoId)
            ?: return false

        val resumo = obterResumoUseCase.invoke(empregoId, ponto.data)
        val banco = calcularBancoUseCase.invoke(empregoId, ponto.data).first()

        // Calcular índice do ponto no dia
        val pontosDoDia = pontoRepository.buscarPorEmpregoEData(empregoId, ponto.data)
            .sortedBy { it.dataHora }
        val indice = pontosDoDia.indexOfFirst { it.id == ponto.id } + 1
        if (indice <= 0) return false

        val fotoComprovante = FotoComprovante(
            pontoId = ponto.id,
            empregoId = empregoId,
            data = ponto.data,
            diaSemana = ponto.data.dayOfWeek,
            hora = ponto.hora,
            indicePontoDia = indice,
            nsr = ponto.nsr,
            latitude = ponto.latitude,
            longitude = ponto.longitude,
            enderecoFormatado = ponto.endereco,
            versaoJornada = versao.id.toInt(),
            tipoJornadaDia = resumo.tipoAusencia!!.toTipoJornadaDia(),
            horasTrabalhadasDiaMinutos = resumo.horasTrabalhadasMinutos.toLong(),

            saldoDiaMinutos = resumo.saldoDiaMinutos.toLong(),
            saldoBancoHorasMinutos = banco.saldoTotal.toMinutes(),
            fotoPath = relativePath,
            fotoTimestamp = Instant.ofEpochMilli(file.lastModified()),
            fotoOrigem = ponto.fotoOrigem.takeIf { it != FotoOrigem.NENHUMA } ?: FotoOrigem.CAMERA,
            fotoTamanhoBytes = file.length(),
            fotoHashMd5 = hash,
            observacao = ponto.observacao
        )

        val id = fotoRepository.salvar(fotoComprovante)

        // Atualiza o ponto se ele não tiver o path
        if (ponto.fotoComprovantePath == null) {
            pontoRepository.atualizar(ponto.copy(fotoComprovantePath = relativePath))
        }

        return id > 0
    }
}
