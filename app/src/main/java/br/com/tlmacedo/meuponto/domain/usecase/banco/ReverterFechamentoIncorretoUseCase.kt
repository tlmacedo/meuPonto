// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/banco/ReverterFechamentoIncorretoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.banco

import br.com.tlmacedo.meuponto.domain.model.TipoFechamento
import br.com.tlmacedo.meuponto.domain.repository.AjusteSaldoRepository
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.FechamentoPeriodoRepository
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * UseCase para reverter fechamentos de ciclo incorretos.
 * 
 * Remove fechamentos e ajustes criados erroneamente,
 * restaurando a configuração do ciclo.
 *
 * @author Thiago
 * @since 6.3.0
 */
class ReverterFechamentoIncorretoUseCase @Inject constructor(
    private val fechamentoRepository: FechamentoPeriodoRepository,
    private val ajusteSaldoRepository: AjusteSaldoRepository,
    private val configuracaoRepository: ConfiguracaoEmpregoRepository
) {

    /**
     * Reverte todos os fechamentos de ciclo de banco de horas de um emprego.
     * 
     * @param empregoId ID do emprego
     * @param dataInicioCicloCorreta Data correta de início do ciclo atual
     * @return Resultado da operação
     */
    suspend operator fun invoke(
        empregoId: Long,
        dataInicioCicloCorreta: LocalDate
    ): Resultado {
        return try {
            android.util.Log.d("REVERTER_DEBUG", "Iniciando reversão para emprego $empregoId")

            // 1. Buscar todos os fechamentos de banco de horas
            val fechamentos = fechamentoRepository.buscarFechamentosBancoHoras(empregoId)
            android.util.Log.d("REVERTER_DEBUG", "Fechamentos encontrados: ${fechamentos.size}")

            // 2. Buscar todos os ajustes de zeramento (justificativa contém "Zeramento de ciclo")
            val todosAjustes = ajusteSaldoRepository.buscarPorEmprego(empregoId)
            val ajustesZeramento = todosAjustes.filter {
                it.justificativa.contains("Zeramento de ciclo", ignoreCase = true) ||
                        it.justificativa.contains("Saldo transferido", ignoreCase = true)
            }
            android.util.Log.d("REVERTER_DEBUG", "Ajustes de zeramento encontrados: ${ajustesZeramento.size}")

            // 3. Excluir ajustes de zeramento
            ajustesZeramento.forEach { ajuste ->
                android.util.Log.d("REVERTER_DEBUG", "Excluindo ajuste: ${ajuste.id} - ${ajuste.justificativa}")
                ajusteSaldoRepository.excluir(ajuste)
            }

            // 4. Excluir fechamentos de banco de horas
            fechamentos.forEach { fechamento ->
                android.util.Log.d("REVERTER_DEBUG", "Excluindo fechamento: ${fechamento.id} - ${fechamento.dataInicioPeriodo} ~ ${fechamento.dataFimPeriodo}")
                fechamentoRepository.excluir(fechamento)
            }

            // 5. Restaurar configuração do ciclo
            val configuracao = configuracaoRepository.buscarPorEmpregoId(empregoId)
            if (configuracao != null) {
                val novaConfiguracao = configuracao.copy(
                    dataInicioCicloBancoAtual = dataInicioCicloCorreta,
                    atualizadoEm = LocalDateTime.now()
                )
                configuracaoRepository.atualizar(novaConfiguracao)
                android.util.Log.d("REVERTER_DEBUG", "Configuração atualizada: dataInicioCiclo = $dataInicioCicloCorreta")
            }

            Resultado.Sucesso(
                fechamentosRemovidos = fechamentos.size,
                ajustesRemovidos = ajustesZeramento.size
            )
        } catch (e: Exception) {
            android.util.Log.e("REVERTER_DEBUG", "Erro ao reverter: ${e.message}", e)
            Resultado.Erro(e.message ?: "Erro desconhecido")
        }
    }

    sealed class Resultado {
        data class Sucesso(
            val fechamentosRemovidos: Int,
            val ajustesRemovidos: Int
        ) : Resultado()

        data class Erro(val mensagem: String) : Resultado()
    }
}
