package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.usecase.validacao.ValidarRegistroPontoUseCase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

class RegistrarPontoUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val validarRegistroPontoUseCase: ValidarRegistroPontoUseCase
) {
    data class Parametros(
        val empregoId: Long,
        val dataHora: LocalDateTime = LocalDateTime.now(),
        val tipo: TipoPonto? = null,
        val observacao: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val endereco: String? = null
    )

    sealed class Resultado {
        data class Sucesso(val pontoId: Long) : Resultado()
        data class Erro(val mensagem: String) : Resultado()
        data class Validacao(val erros: List<String>) : Resultado()
    }

    suspend operator fun invoke(parametros: Parametros): Resultado {
        val data = parametros.dataHora.toLocalDate()
        val hora = parametros.dataHora.toLocalTime()
        
        // Determina o tipo automaticamente se não informado
        val tipo = parametros.tipo ?: determinarTipoAutomatico(parametros.empregoId, data)

        // Valida o registro
        val resultadoValidacao = validarRegistroPontoUseCase(
            empregoId = parametros.empregoId,
            data = data,
            hora = hora,
            tipo = tipo
        )

        if (resultadoValidacao is ValidarRegistroPontoUseCase.ResultadoValidacao.Invalido) {
            return Resultado.Validacao(resultadoValidacao.erros.map { it.mensagem })
        }

        val ponto = Ponto(
            empregoId = parametros.empregoId,
            dataHora = parametros.dataHora,
            tipo = tipo,
            observacao = parametros.observacao,
            latitude = parametros.latitude,
            longitude = parametros.longitude,
            endereco = parametros.endereco
        )

        return try {
            val id = pontoRepository.inserir(ponto)
            Resultado.Sucesso(id)
        } catch (e: Exception) {
            Resultado.Erro("Erro ao registrar ponto: ${e.message}")
        }
    }

    private suspend fun determinarTipoAutomatico(empregoId: Long, data: LocalDate): TipoPonto {
        val pontosHoje = pontoRepository.buscarPorEmpregoEData(empregoId, data)
        
        return if (pontosHoje.isEmpty()) {
            TipoPonto.ENTRADA
        } else {
            val ultimoPonto = pontosHoje.maxByOrNull { it.hora }
            if (ultimoPonto?.tipo == TipoPonto.ENTRADA) {
                TipoPonto.SAIDA
            } else {
                TipoPonto.ENTRADA
            }
        }
    }
}
