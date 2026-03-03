// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/versaojornada/ObterVersaoJornadaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.versaojornada

import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UseCase para consultas de versões de jornada.
 *
 * Centraliza operações de leitura e observação de versões de jornada,
 * incluindo busca por ID, emprego, data e observação reativa.
 *
 * @property versaoJornadaRepository Repositório de versões de jornada
 *
 * @author Thiago
 * @since 4.0.0
 */
@Singleton
class ObterVersaoJornadaUseCase @Inject constructor(
    private val versaoJornadaRepository: VersaoJornadaRepository
) {
    // ========================================================================
    // Consultas por ID
    // ========================================================================

    /**
     * Busca uma versão de jornada pelo ID.
     *
     * @param versaoId ID da versão
     * @return Versão encontrada ou null
     */
    suspend fun porId(versaoId: Long): VersaoJornada? {
        Timber.d("Buscando versão de jornada por ID: $versaoId")
        return versaoJornadaRepository.buscarPorId(versaoId)
    }

    // ========================================================================
    // Consultas por Emprego
    // ========================================================================

    /**
     * Lista todas as versões de jornada de um emprego.
     *
     * @param empregoId ID do emprego
     * @return Lista de versões ordenadas por data de início (mais recente primeiro)
     */
    suspend fun porEmprego(empregoId: Long): List<VersaoJornada> {
        Timber.d("Listando versões de jornada do emprego: $empregoId")
        return versaoJornadaRepository.listarPorEmprego(empregoId)
    }

    /**
     * Busca a versão vigente de um emprego.
     *
     * @param empregoId ID do emprego
     * @return Versão vigente ou null se não houver
     */
    suspend fun vigenteDoEmprego(empregoId: Long): VersaoJornada? {
        Timber.d("Buscando versão vigente do emprego: $empregoId")
        return versaoJornadaRepository.buscarVigente(empregoId)
    }

    /**
     * Busca a versão aplicável para uma data específica.
     *
     * @param empregoId ID do emprego
     * @param data Data de referência
     * @return Versão aplicável ou null
     */
    suspend fun porData(empregoId: Long, data: LocalDate): VersaoJornada? {
        Timber.d("Buscando versão do emprego $empregoId para data: $data")
        return versaoJornadaRepository.buscarPorData(empregoId, data)
    }

    /**
     * Conta quantas versões um emprego possui.
     *
     * @param empregoId ID do emprego
     * @return Quantidade de versões
     */
    suspend fun contarPorEmprego(empregoId: Long): Int {
        return versaoJornadaRepository.contarPorEmprego(empregoId)
    }

    /**
     * Verifica se um emprego possui alguma versão de jornada.
     *
     * @param empregoId ID do emprego
     * @return true se existir ao menos uma versão
     */
    suspend fun empregoTemVersao(empregoId: Long): Boolean {
        return versaoJornadaRepository.existeParaEmprego(empregoId)
    }

    // ========================================================================
    // Observação Reativa
    // ========================================================================

    /**
     * Observa todas as versões de um emprego de forma reativa.
     *
     * @param empregoId ID do emprego
     * @return Flow que emite a lista sempre que houver mudanças
     */
    fun observarPorEmprego(empregoId: Long): Flow<List<VersaoJornada>> {
        Timber.d("Iniciando observação de versões do emprego: $empregoId")
        return versaoJornadaRepository.observarPorEmprego(empregoId)
    }

    /**
     * Observa a versão vigente de um emprego de forma reativa.
     *
     * @param empregoId ID do emprego
     * @return Flow que emite a versão vigente sempre que houver mudanças
     */
    fun observarVigente(empregoId: Long): Flow<VersaoJornada?> {
        Timber.d("Iniciando observação da versão vigente do emprego: $empregoId")
        return versaoJornadaRepository.observarVigente(empregoId)
    }
}
