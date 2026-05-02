// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/RecalcularToleranciasPontosUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso responsável pelo recálculo massivo da horaConsiderada dos pontos existentes.
 *
 * É acionado geralmente após atualizações do app que mudam a lógica de tolerância
 * ou quando o usuário altera configurações críticas de jornada que retroagem.
 *
 * A lógica segue a regra:
 * 1. horaConsiderada = hora real (reset padrão);
 * 2. Identifica retornos de intervalo elegíveis para tolerância;
 * 3. Aplica no máximo uma tolerância por dia;
 * 4. Critério de desempate: proximidade com o horário de saída ideal.
 *
 * @author Thiago
 * @since 7.1.0
 */
class RecalcularToleranciasPontosUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val empregoRepository: EmpregoRepository,
    private val recalcularHoraConsideradaPontosDiaUseCase: RecalcularHoraConsideradaPontosDiaUseCase
) {

    data class Resultado(
        val totalDiasProcessados: Int,
        val totalPontosProcessados: Int,
        val totalPontosAtualizados: Int,
        val erros: List<String>
    ) {
        val sucesso: Boolean get() = erros.isEmpty()
    }

    fun interface ProgressCallback {
        fun onProgress(atual: Int, total: Int, mensagem: String)
    }

    suspend operator fun invoke(
        progressCallback: ProgressCallback? = null
    ): Resultado {
        val erros = mutableListOf<String>()

        var totalDiasProcessados = 0
        var totalPontosProcessados = 0
        var totalPontosAtualizados = 0

        return try {
            val empregos = empregoRepository.observarTodos().first()

            if (empregos.isEmpty()) {
                Timber.d("Nenhum emprego encontrado para recalcular tolerâncias.")
                return Resultado(
                    totalDiasProcessados = 0,
                    totalPontosProcessados = 0,
                    totalPontosAtualizados = 0,
                    erros = emptyList()
                )
            }

            Timber.i("Iniciando recálculo de tolerâncias para ${empregos.size} emprego(s).")

            empregos.forEachIndexed { index, emprego ->
                progressCallback?.onProgress(
                    atual = index + 1,
                    total = empregos.size,
                    mensagem = "Recalculando ${emprego.nome}..."
                )

                try {
                    val resultadoEmprego = recalcularPorEmprego(
                        empregoId = emprego.id,
                        progressCallback = progressCallback
                    )

                    totalDiasProcessados += resultadoEmprego.totalDiasProcessados
                    totalPontosProcessados += resultadoEmprego.totalPontosProcessados
                    totalPontosAtualizados += resultadoEmprego.totalPontosAtualizados
                } catch (e: Exception) {
                    val erro = "Erro no emprego ${emprego.id} (${emprego.nome}): ${e.message}"
                    Timber.e(e, erro)
                    erros.add(erro)
                }
            }

            Timber.i(
                "Recálculo finalizado: dias=%d, pontos=%d, atualizados=%d, erros=%d",
                totalDiasProcessados,
                totalPontosProcessados,
                totalPontosAtualizados,
                erros.size
            )

            Resultado(
                totalDiasProcessados = totalDiasProcessados,
                totalPontosProcessados = totalPontosProcessados,
                totalPontosAtualizados = totalPontosAtualizados,
                erros = erros
            )
        } catch (e: Exception) {
            val erro = "Erro geral no recálculo de tolerâncias: ${e.message}"
            Timber.e(e, erro)

            Resultado(
                totalDiasProcessados = totalDiasProcessados,
                totalPontosProcessados = totalPontosProcessados,
                totalPontosAtualizados = totalPontosAtualizados,
                erros = erros + erro
            )
        }
    }

    data class ResultadoEmprego(
        val totalDiasProcessados: Int,
        val totalPontosProcessados: Int,
        val totalPontosAtualizados: Int
    )

    suspend fun recalcularPorEmprego(
        empregoId: Long,
        progressCallback: ProgressCallback? = null
    ): ResultadoEmprego {
        val primeiraData = pontoRepository.buscarPrimeiraData(empregoId)
            ?: return ResultadoEmprego(
                totalDiasProcessados = 0,
                totalPontosProcessados = 0,
                totalPontosAtualizados = 0
            )

        var totalDiasProcessados = 0
        var totalPontosProcessados = 0
        var totalPontosAtualizados = 0

        var dataAtual = primeiraData
        val dataFim = LocalDate.now()

        while (!dataAtual.isAfter(dataFim)) {
            val resultadoDia = recalcularDia(
                empregoId = empregoId,
                data = dataAtual
            )

            totalDiasProcessados++
            totalPontosProcessados += resultadoDia.totalPontos
            totalPontosAtualizados += resultadoDia.pontosAtualizados

            progressCallback?.onProgress(
                atual = totalDiasProcessados,
                total = -1,
                mensagem = "Processando $dataAtual..."
            )

            dataAtual = dataAtual.plusDays(1)
        }

        return ResultadoEmprego(
            totalDiasProcessados = totalDiasProcessados,
            totalPontosProcessados = totalPontosProcessados,
            totalPontosAtualizados = totalPontosAtualizados
        )
    }

    /**
     * Recalcula um dia inteiro.
     *
     * Esta função não calcula ponto por ponto.
     * Ela delega para o use case diário, que olha todos os pontos do dia
     * e escolhe no máximo um retorno de intervalo para receber tolerância.
     */
    suspend fun recalcularDia(
        empregoId: Long,
        data: LocalDate
    ): RecalcularHoraConsideradaPontosDiaUseCase.Resultado.Sucesso {
        return when (
            val resultado = recalcularHoraConsideradaPontosDiaUseCase(
                empregoId = empregoId,
                data = data
            )
        ) {
            is RecalcularHoraConsideradaPontosDiaUseCase.Resultado.Sucesso -> {
                resultado
            }

            is RecalcularHoraConsideradaPontosDiaUseCase.Resultado.SemPontos -> {
                RecalcularHoraConsideradaPontosDiaUseCase.Resultado.Sucesso(
                    empregoId = empregoId,
                    data = data,
                    totalPontos = 0,
                    pontosAtualizados = 0,
                    pontoComToleranciaId = null,
                    detalhes = emptyList()
                )
            }

            is RecalcularHoraConsideradaPontosDiaUseCase.Resultado.ContextoNaoEncontrado -> {
                Timber.w(
                    "Contexto não encontrado ao recalcular tolerância: empregoId=%d, data=%s, motivo=%s",
                    empregoId,
                    data,
                    resultado.motivo
                )

                RecalcularHoraConsideradaPontosDiaUseCase.Resultado.Sucesso(
                    empregoId = empregoId,
                    data = data,
                    totalPontos = 0,
                    pontosAtualizados = 0,
                    pontoComToleranciaId = null,
                    detalhes = emptyList()
                )
            }

            is RecalcularHoraConsideradaPontosDiaUseCase.Resultado.Erro -> {
                Timber.e(
                    "Erro ao recalcular tolerância: empregoId=%d, data=%s, erro=%s",
                    empregoId,
                    data,
                    resultado.mensagem
                )

                RecalcularHoraConsideradaPontosDiaUseCase.Resultado.Sucesso(
                    empregoId = empregoId,
                    data = data,
                    totalPontos = 0,
                    pontosAtualizados = 0,
                    pontoComToleranciaId = null,
                    detalhes = emptyList()
                )
            }
        }
    }
}