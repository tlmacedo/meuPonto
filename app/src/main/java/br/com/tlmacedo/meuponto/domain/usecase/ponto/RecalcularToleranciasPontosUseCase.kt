// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/RecalcularToleranciasPontosUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para recalcular as tolerâncias de todos os pontos existentes.
 *
 * Útil após correções na lógica de cálculo de tolerância ou quando
 * a configuração de tolerância é alterada.
 *
 * @author Thiago
 * @since 7.1.0
 */
class RecalcularToleranciasPontosUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val empregoRepository: EmpregoRepository,
    private val calcularHoraConsideradaUseCase: CalcularHoraConsideradaUseCase
) {

    /**
     * Resultado do recálculo.
     */
    data class Resultado(
        val totalProcessados: Int,
        val totalAtualizados: Int,
        val erros: List<String>
    ) {
        val sucesso: Boolean get() = erros.isEmpty()
    }

    /**
     * Callback para acompanhar o progresso.
     */
    fun interface ProgressCallback {
        fun onProgress(atual: Int, total: Int, mensagem: String)
    }

    /**
     * Recalcula as tolerâncias de todos os pontos de todos os empregos.
     *
     * @param progressCallback Callback opcional para acompanhar o progresso
     * @return Resultado com estatísticas do processamento
     */
    suspend operator fun invoke(progressCallback: ProgressCallback? = null): Resultado {
        val erros = mutableListOf<String>()
        var totalProcessados = 0
        var totalAtualizados = 0

        try {
            // Busca todos os empregos
            val empregos = empregoRepository.observarTodos().first()

            if (empregos.isEmpty()) {
                Timber.d("Nenhum emprego encontrado para recalcular")
                return Resultado(0, 0, emptyList())
            }

            Timber.i("Iniciando recálculo de tolerâncias para ${empregos.size} emprego(s)")

            for (emprego in empregos) {
                try {
                    val resultado = recalcularPorEmprego(emprego.id, progressCallback)
                    totalProcessados += resultado.first
                    totalAtualizados += resultado.second
                } catch (e: Exception) {
                    val erro = "Erro no emprego ${emprego.id} (${emprego.nome}): ${e.message}"
                    Timber.e(e, erro)
                    erros.add(erro)
                }
            }

            Timber.i(
                "Recálculo finalizado: %d processados, %d atualizados, %d erros",
                totalProcessados, totalAtualizados, erros.size
            )

        } catch (e: Exception) {
            val erro = "Erro geral no recálculo: ${e.message}"
            Timber.e(e, erro)
            erros.add(erro)
        }

        return Resultado(totalProcessados, totalAtualizados, erros)
    }

    /**
     * Recalcula as tolerâncias de todos os pontos de um emprego específico.
     *
     * @param empregoId ID do emprego
     * @param progressCallback Callback opcional para progresso
     * @return Par com (totalProcessados, totalAtualizados)
     */
    suspend fun recalcularPorEmprego(
        empregoId: Long,
        progressCallback: ProgressCallback? = null
    ): Pair<Int, Int> {
        var totalProcessados = 0
        var totalAtualizados = 0

        // Busca a primeira data com registro
        val primeiraData = pontoRepository.buscarPrimeiraData(empregoId)
            ?: return Pair(0, 0)

        // Itera por cada dia desde a primeira data até hoje
        var dataAtual = primeiraData
        val dataFim = LocalDate.now()

        while (!dataAtual.isAfter(dataFim)) {
            val resultado = recalcularDia(empregoId, dataAtual)
            totalProcessados += resultado.first
            totalAtualizados += resultado.second

            progressCallback?.onProgress(
                totalProcessados,
                -1, // Total desconhecido
                "Processando $dataAtual..."
            )

            dataAtual = dataAtual.plusDays(1)
        }

        return Pair(totalProcessados, totalAtualizados)
    }

    /**
     * Recalcula as tolerâncias de todos os pontos de um dia específico.
     *
     * IMPORTANTE: Os pontos devem ser processados em ordem cronológica,
     * pois a tolerância da volta do intervalo depende da hora considerada
     * da saída anterior.
     *
     * @param empregoId ID do emprego
     * @param data Data a ser processada
     * @return Par com (totalProcessados, totalAtualizados)
     */
    private suspend fun recalcularDia(empregoId: Long, data: LocalDate): Pair<Int, Int> {
        // Busca os pontos do dia ordenados cronologicamente
        val pontosNoDia = pontoRepository.buscarPorEmpregoEData(empregoId, data)
            .sortedBy { it.dataHora }

        if (pontosNoDia.isEmpty()) {
            return Pair(0, 0)
        }

        var totalAtualizados = 0

        // Processa cada ponto em ordem
        for ((index, ponto) in pontosNoDia.withIndex()) {
            val novaHoraConsiderada = calcularHoraConsideradaUseCase(
                empregoId = empregoId,
                dataHora = ponto.dataHora,
                indicePonto = index
            )

            // Só atualiza se a hora considerada mudou
            if (novaHoraConsiderada != ponto.horaConsiderada) {
                val pontoAtualizado = ponto.comHoraConsiderada(novaHoraConsiderada)
                pontoRepository.atualizar(pontoAtualizado)

                Timber.d(
                    "Ponto %d atualizado: %s → horaConsiderada: %s → %s",
                    ponto.id,
                    ponto.horaFormatada,
                    ponto.horaConsideradaFormatada,
                    pontoAtualizado.horaConsideradaFormatada
                )

                totalAtualizados++
            }
        }

        return Pair(pontosNoDia.size, totalAtualizados)
    }
}
