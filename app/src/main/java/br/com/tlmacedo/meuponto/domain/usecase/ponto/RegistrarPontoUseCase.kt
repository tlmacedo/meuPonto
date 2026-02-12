package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Caso de uso para registrar uma nova batida de ponto.
 *
 * Responsável por validar e inserir um novo registro de ponto,
 * determinando automaticamente o tipo de batida quando não especificado.
 *
 * @property repository Repositório de pontos para persistência
 *
 * @author Thiago
 * @since 1.0.0
 */
class RegistrarPontoUseCase @Inject constructor(
    private val repository: PontoRepository
) {
    /**
     * Registra uma nova batida de ponto.
     *
     * @param dataHora Data e hora da batida (padrão: agora)
     * @param tipo Tipo da batida (padrão: automático baseado no último ponto)
     * @param observacao Observação opcional
     * @return Result com o Ponto criado ou erro
     */
    suspend operator fun invoke(
        dataHora: LocalDateTime = LocalDateTime.now(),
        tipo: TipoPonto? = null,
        observacao: String? = null
    ): Result<Ponto> {
        return try {
            if (dataHora.isAfter(LocalDateTime.now())) {
                return Result.failure(
                    IllegalArgumentException("Não é permitido registrar ponto no futuro")
                )
            }

            val tipoFinal = tipo ?: determinarTipoAutomatico(dataHora)

            val ponto = Ponto(
                dataHora = dataHora,
                tipo = tipoFinal,
                observacao = observacao
            )

            val id = repository.inserir(ponto)
            val pontoSalvo = ponto.copy(id = id)
            
            Timber.d("Ponto registrado com sucesso: $pontoSalvo")
            Result.success(pontoSalvo)

        } catch (e: Exception) {
            Timber.e(e, "Erro ao registrar ponto")
            Result.failure(e)
        }
    }

    /**
     * Determina automaticamente o tipo de ponto baseado no último registro do dia.
     */
    private suspend fun determinarTipoAutomatico(dataHora: LocalDateTime): TipoPonto {
        val ultimoPonto = repository.buscarUltimoPontoDoDia(dataHora.toLocalDate())
        return TipoPonto.proximoTipo(ultimoPonto?.tipo)
    }
}
