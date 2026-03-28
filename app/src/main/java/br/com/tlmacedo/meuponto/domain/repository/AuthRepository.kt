// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/repository/AuthRepository.kt
package br.com.tlmacedo.meuponto.domain.repository

import br.com.tlmacedo.meuponto.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de autenticação.
 *
 * Define as operações necessárias para o fluxo de autenticação do usuário.
 */
interface AuthRepository {

    /**
     * Realiza o login do usuário.
     *
     * @param email E-mail do usuário
     * @param senha Senha do usuário
     * @return Result contendo o Usuario em caso de sucesso
     */
    suspend fun login(email: String, senha: String): Result<Usuario>

    /**
     * Realiza o cadastro de um novo usuário.
     *
     * @param nome Nome completo
     * @param email E-mail
     * @param senha Senha
     * @return Result contendo o Usuario em caso de sucesso
     */
    suspend fun cadastrar(nome: String, email: String, senha: String): Result<Usuario>

    /**
     * Envia um e-mail para recuperação de senha.
     *
     * @param email E-mail do usuário
     * @return Result indicando sucesso ou falha no envio
     */
    suspend fun recuperarSenha(email: String): Result<Unit>

    /**
     * Realiza o logout do usuário.
     */
    suspend fun logout()

    /**
     * Obtém o usuário atualmente logado.
     *
     * @return Flow que emite o usuário logado ou null
     */
    fun observarUsuarioLogado(): Flow<Usuario?>

    /**
     * Verifica se existe um usuário logado.
     */
    suspend fun isUsuarioLogado(): Boolean
}
