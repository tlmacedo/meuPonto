// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/versaojornada/VersaoJornadaUseCases.kt
package br.com.tlmacedo.meuponto.domain.usecase.versaojornada

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Facade que agrupa todos os UseCases de VersaoJornada.
 *
 * Fornece acesso unificado às operações de versões de jornada,
 * seguindo o princípio de responsabilidade única (SRP) onde cada
 * UseCase interno tem uma única responsabilidade.
 *
 * ## Uso
 * ```kotlin
 * @Inject lateinit var versaoJornadaUseCases: VersaoJornadaUseCases
 *
 * // Criar
 * val resultado = versaoJornadaUseCases.criar(params)
 *
 * // Atualizar
 * val resultado = versaoJornadaUseCases.atualizar(params)
 *
 * // Excluir
 * val resultado = versaoJornadaUseCases.excluir(versaoId)
 *
 * // Consultar
 * val versao = versaoJornadaUseCases.obter.porId(versaoId)
 * val versoes = versaoJornadaUseCases.obter.porEmprego(empregoId)
 * val vigente = versaoJornadaUseCases.obter.vigenteDoEmprego(empregoId)
 * ```
 *
 * @property criarUseCase UseCase para criação de versões
 * @property atualizarUseCase UseCase para atualização de versões
 * @property excluirUseCase UseCase para exclusão de versões
 * @property obterUseCase UseCase para consultas de versões
 *
 * @author Thiago
 * @since 4.0.0
 */
@Singleton
class VersaoJornadaUseCases @Inject constructor(
    private val criarUseCase: CriarVersaoJornadaUseCase,
    private val atualizarUseCase: AtualizarVersaoJornadaUseCase,
    private val excluirUseCase: ExcluirVersaoJornadaUseCase,
    private val obterUseCase: ObterVersaoJornadaUseCase
) {
    // ========================================================================
    // Acesso aos UseCases
    // ========================================================================

    /**
     * UseCase para consultas de versões de jornada.
     */
    val obter: ObterVersaoJornadaUseCase get() = obterUseCase

    // ========================================================================
    // Operações Delegadas
    // ========================================================================

    /**
     * Cria uma nova versão de jornada.
     *
     * @param params Parâmetros para criação
     * @return Result com a versão criada ou erro
     */
    suspend fun criar(params: CriarVersaoJornadaUseCase.Params) = criarUseCase(params)

    /**
     * Atualiza uma versão de jornada existente.
     *
     * @param params Parâmetros para atualização
     * @return Result com a versão atualizada ou erro
     */
    suspend fun atualizar(params: AtualizarVersaoJornadaUseCase.Params) = atualizarUseCase(params)

    /**
     * Exclui uma versão de jornada.
     *
     * @param versaoId ID da versão a ser excluída
     * @return Result com Unit ou erro
     */
    suspend fun excluir(versaoId: Long) = excluirUseCase(versaoId)
}
