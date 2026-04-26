package br.com.tlmacedo.meuponto.domain.repository

import br.com.tlmacedo.meuponto.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, senha: String): Result<Usuario>
    suspend fun loginPorEmail(email: String): Result<Usuario>
    suspend fun register(nome: String, email: String, senha: String): Result<Usuario>
    suspend fun recuperarSenha(email: String): Result<Unit>
    suspend fun logout()
    fun observarUsuarioLogado(): Flow<Usuario?>
    suspend fun isUsuarioLogado(): Boolean
}
