// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/emprego/CopiarConfiguracaoEmpregoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.emprego

import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Caso de uso para copiar configurações de um emprego para outro.
 *
 * Permite reutilizar configurações existentes (jornada, horários por dia
 * da semana, etc.) ao criar um novo emprego semelhante. Útil para usuários
 * com múltiplos empregos que seguem padrões similares.
 *
 * @property empregoRepository Repositório de empregos para validação
 * @property configuracaoEmpregoRepository Repositório de configurações de emprego
 * @property horarioDiaSemanaRepository Repositório de horários por dia da semana
 *
 * @author Thiago
 * @since 2.0.0
 */
class CopiarConfiguracaoEmpregoUseCase @Inject constructor(
    private val empregoRepository: EmpregoRepository,
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository
) {

    /**
     * Resultado da operação de cópia.
     */
    sealed class Resultado {
        /**
         * Configurações copiadas com sucesso.
         */
        data object Sucesso : Resultado()

        /**
         * Emprego de origem não encontrado.
         *
         * @property empregoId ID do emprego que não foi encontrado
         */
        data class OrigemNaoEncontrada(val empregoId: Long) : Resultado()

        /**
         * Emprego de destino não encontrado.
         *
         * @property empregoId ID do emprego que não foi encontrado
         */
        data class DestinoNaoEncontrado(val empregoId: Long) : Resultado()

        /**
         * Os IDs de origem e destino são iguais.
         */
        data object MesmoEmprego : Resultado()

        /**
         * Erro durante a operação.
         *
         * @property mensagem Descrição do erro ocorrido
         */
        data class Erro(val mensagem: String) : Resultado()
    }

    /**
     * Parâmetros para a operação de cópia.
     *
     * @property empregoOrigemId ID do emprego de onde copiar as configurações
     * @property empregoDestinoId ID do emprego para onde copiar as configurações
     * @property copiarHorarios Se deve copiar os horários por dia da semana
     * @property substituirExistente Se deve substituir configurações existentes no destino
     */
    data class Parametros(
        val empregoOrigemId: Long,
        val empregoDestinoId: Long,
        val copiarHorarios: Boolean = true,
        val substituirExistente: Boolean = true
    )

    /**
     * Copia as configurações de um emprego para outro.
     *
     * Copia a configuração geral do emprego e, opcionalmente, os horários
     * configurados para cada dia da semana.
     *
     * @param parametros Parâmetros da operação de cópia
     * @return Resultado da operação
     */
    suspend operator fun invoke(parametros: Parametros): Resultado {
        return try {
            // Valida se origem e destino são diferentes
            if (parametros.empregoOrigemId == parametros.empregoDestinoId) {
                return Resultado.MesmoEmprego
            }

            // Valida se os empregos existem
            empregoRepository.buscarPorId(parametros.empregoOrigemId)
                ?: return Resultado.OrigemNaoEncontrada(parametros.empregoOrigemId)

            empregoRepository.buscarPorId(parametros.empregoDestinoId)
                ?: return Resultado.DestinoNaoEncontrado(parametros.empregoDestinoId)

            // Copia a configuração principal
            copiarConfiguracaoPrincipal(parametros)

            // Copia os horários por dia da semana, se solicitado
            if (parametros.copiarHorarios) {
                copiarHorariosDiaSemana(parametros)
            }

            Resultado.Sucesso
        } catch (e: Exception) {
            Resultado.Erro("Erro ao copiar configurações: ${e.message}")
        }
    }

    /**
     * Copia a configuração principal do emprego.
     */
    private suspend fun copiarConfiguracaoPrincipal(parametros: Parametros) {
        val configOrigem = configuracaoEmpregoRepository
            .buscarPorEmpregoId(parametros.empregoOrigemId)
            ?: return // Sem configuração para copiar

        val configDestino = configuracaoEmpregoRepository
            .buscarPorEmpregoId(parametros.empregoDestinoId)

        // Cria a nova configuração baseada na origem
        val novaConfig = configOrigem.copy(
            id = configDestino?.id ?: 0,
            empregoId = parametros.empregoDestinoId,
            criadoEm = configDestino?.criadoEm ?: LocalDateTime.now(),
            atualizadoEm = LocalDateTime.now()
        )

        if (configDestino != null) {
            if (parametros.substituirExistente) {
                configuracaoEmpregoRepository.atualizar(novaConfig)
            }
        } else {
            configuracaoEmpregoRepository.inserir(novaConfig)
        }
    }

    /**
     * Copia os horários por dia da semana.
     */
    private suspend fun copiarHorariosDiaSemana(parametros: Parametros) {
        val horariosOrigem: List<HorarioDiaSemana> = horarioDiaSemanaRepository
            .buscarPorEmprego(parametros.empregoOrigemId)

        if (horariosOrigem.isEmpty()) {
            return // Sem horários para copiar
        }

        // Remove horários existentes no destino se deve substituir
        if (parametros.substituirExistente) {
            horarioDiaSemanaRepository.excluirPorEmprego(parametros.empregoDestinoId)
        }

        // Copia cada horário
        horariosOrigem.forEach { horario ->
            val novoHorario = horario.copy(
                id = 0, // Novo registro
                empregoId = parametros.empregoDestinoId,
                criadoEm = LocalDateTime.now(),
                atualizadoEm = LocalDateTime.now()
            )
            horarioDiaSemanaRepository.inserir(novoHorario)
        }
    }
}
