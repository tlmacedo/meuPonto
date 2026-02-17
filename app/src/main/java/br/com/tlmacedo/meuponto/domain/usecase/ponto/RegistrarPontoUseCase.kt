// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/RegistrarPontoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Caso de uso para registrar um novo ponto.
 *
 * Gerencia a lógica de registro de entrada/saída, detectando automaticamente
 * o tipo do ponto com base nos registros existentes no dia. Suporta registro
 * manual (com horário específico) e automático (horário atual).
 *
 * Todos os horários são truncados para minutos (segundos e nanossegundos zerados)
 * para garantir consistência nos cálculos e exibição.
 *
 * @property pontoRepository Repositório de pontos para persistência
 * @property preferenciasRepository Repositório de preferências para obter emprego ativo
 *
 * @author Thiago
 * @since 2.0.0
 */
class RegistrarPontoUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val preferenciasRepository: PreferenciasRepository
) {

    /**
     * Resultado da operação de registro de ponto.
     */
    sealed class Resultado {
        /**
         * Ponto registrado com sucesso.
         *
         * @property ponto Ponto registrado
         * @property pontoId ID do ponto registrado (conveniência para ViewModels)
         * @property mensagem Mensagem descritiva do registro
         */
        data class Sucesso(
            val ponto: Ponto,
            val mensagem: String
        ) : Resultado() {
            val pontoId: Long get() = ponto.id
        }

        /**
         * Nenhum emprego ativo selecionado.
         */
        data object SemEmpregoAtivo : Resultado()

        /**
         * Horário inválido ou conflitante.
         *
         * @property motivo Descrição do conflito
         */
        data class HorarioInvalido(val motivo: String) : Resultado()

        /**
         * Limite de pontos do dia atingido.
         *
         * @property limite Número máximo de pontos permitidos
         */
        data class LimiteAtingido(val limite: Int) : Resultado()

        /**
         * Erros de validação dos dados de entrada.
         *
         * @property erros Lista de mensagens de erro de validação
         */
        data class Validacao(val erros: List<String>) : Resultado()

        /**
         * Erro durante a operação.
         *
         * @property mensagem Descrição do erro ocorrido
         */
        data class Erro(val mensagem: String) : Resultado()
    }

    /**
     * Parâmetros para registro de ponto.
     *
     * @property empregoId ID do emprego (opcional, usa o ativo se não informado)
     * @property dataHora Data e hora do registro (opcional, usa agora se não informado)
     * @property tipo Tipo do ponto (opcional, detecta automaticamente se não informado)
     * @property observacao Observação opcional sobre o registro
     * @property latitude Latitude da localização (opcional)
     * @property longitude Longitude da localização (opcional)
     */
    data class Parametros(
        val empregoId: Long? = null,
        val dataHora: LocalDateTime? = null,
        val tipo: TipoPonto? = null,
        val observacao: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null
    )

    /**
     * Registra um ponto com os parâmetros especificados.
     *
     * O horário é automaticamente truncado para minutos (segundos = 0, nanos = 0)
     * para garantir consistência nos cálculos de jornada e exibição.
     *
     * @param parametros Parâmetros do registro
     * @return Resultado da operação
     */
    suspend operator fun invoke(parametros: Parametros = Parametros()): Resultado {
        return try {
            // Obtém o emprego ativo
            val empregoId = parametros.empregoId
                ?: preferenciasRepository.obterEmpregoAtivoId()
                ?: return Resultado.SemEmpregoAtivo

            // Trunca o horário para minutos (remove segundos e nanossegundos)
            val dataHoraOriginal = parametros.dataHora ?: LocalDateTime.now()
            val dataHora = dataHoraOriginal.truncatedTo(ChronoUnit.MINUTES)
            val data = dataHora.toLocalDate()

            // Busca pontos existentes no dia
            val pontosNoDia = pontoRepository.buscarPorEmpregoEData(empregoId, data)

            // Verifica limite de pontos
            if (pontosNoDia.size >= TipoPonto.MAX_PONTOS) {
                return Resultado.LimiteAtingido(TipoPonto.MAX_PONTOS)
            }

            // Valida horário (não pode ser antes do último ponto)
            val ultimoPonto = pontosNoDia.maxByOrNull { it.dataHora }
            if (ultimoPonto != null && dataHora.isBefore(ultimoPonto.dataHora)) {
                return Resultado.HorarioInvalido(
                    "O horário não pode ser anterior ao último registro (${ultimoPonto.horaFormatada})"
                )
            }

            // Determina o tipo do ponto
            val tipo = parametros.tipo ?: TipoPonto.getProximoTipo(ultimoPonto?.tipo)

            // Cria o ponto (timestamps de auditoria também truncados)
            val agora = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
            val ponto = Ponto(
                empregoId = empregoId,
                dataHora = dataHora,
                tipo = tipo,
                observacao = parametros.observacao,
                latitude = parametros.latitude,
                longitude = parametros.longitude,
                criadoEm = agora,
                atualizadoEm = agora
            )

            // Persiste o ponto
            val id = pontoRepository.inserir(ponto)
            val pontoSalvo = ponto.copy(id = id)

            // Monta mensagem de sucesso
            val mensagem = "${tipo.descricao} registrada às ${pontoSalvo.horaFormatada}"

            Resultado.Sucesso(pontoSalvo, mensagem)
        } catch (e: Exception) {
            Resultado.Erro("Erro ao registrar ponto: ${e.message}")
        }
    }

    /**
     * Registra ponto rapidamente usando horário atual e detecção automática de tipo.
     *
     * Atalho conveniente para o registro rápido do dia a dia.
     * O horário é automaticamente truncado para minutos.
     *
     * @param empregoId ID do emprego (opcional)
     * @return Resultado da operação
     */
    suspend fun registrarAgora(empregoId: Long? = null): Resultado {
        return invoke(Parametros(empregoId = empregoId))
    }

    /**
     * Registra ponto com data e hora específicos.
     *
     * Útil para registro manual ou correção de horários.
     * O horário é automaticamente truncado para minutos.
     *
     * @param data Data do registro
     * @param hora Hora do registro
     * @param empregoId ID do emprego (opcional)
     * @param tipo Tipo do ponto (opcional, detecta automaticamente)
     * @return Resultado da operação
     */
    suspend fun registrarManual(
        data: LocalDate,
        hora: LocalTime,
        empregoId: Long? = null,
        tipo: TipoPonto? = null
    ): Resultado {
        return invoke(
            Parametros(
                empregoId = empregoId,
                dataHora = LocalDateTime.of(data, hora),
                tipo = tipo
            )
        )
    }
}
