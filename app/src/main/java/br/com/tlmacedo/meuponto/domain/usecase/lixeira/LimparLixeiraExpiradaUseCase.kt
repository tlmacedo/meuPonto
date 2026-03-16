// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/lixeira/LimparLixeiraExpiradaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.lixeira

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.LixeiraRepository
import javax.inject.Inject

/**
 * UseCase para limpeza automática de itens expirados na lixeira.
 *
 * @author Thiago
 * @since 11.0.0
 */
class LimparLixeiraExpiradaUseCase @Inject constructor(
    private val lixeiraRepository: LixeiraRepository
) {
    companion object {
        const val DIAS_RETENCAO_PADRAO = 30
        const val DIAS_ALERTA_EXPIRACAO = 3
    }

    /**
     * Remove pontos que expiraram o período de retenção.
     *
     * @param diasRetencao Dias de retenção (padrão: 30)
     * @return Quantidade de pontos removidos
     */
    suspend operator fun invoke(diasRetencao: Int = DIAS_RETENCAO_PADRAO): Result<Int> {
        return lixeiraRepository.limparExpirados(diasRetencao)
    }

    /**
     * Lista pontos prestes a expirar.
     *
     * @param diasRestantes Dias restantes até expiração
     */
    suspend fun listarPrestesAExpirar(diasRestantes: Int = DIAS_ALERTA_EXPIRACAO): List<Ponto> {
        return lixeiraRepository.listarPrestesAExpirar(diasRestantes)
    }
}
