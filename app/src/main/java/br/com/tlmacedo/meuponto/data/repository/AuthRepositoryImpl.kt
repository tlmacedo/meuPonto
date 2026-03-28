// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/AuthRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.domain.model.Usuario
import br.com.tlmacedo.meuponto.domain.repository.AuthRepository
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de autenticação.
 * 
 * Por enquanto, utiliza um simulador de autenticação local.
 * Em uma aplicação real, aqui seriam feitas as chamadas à API REST ou Firebase.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository
) : AuthRepository {

    private val _usuarioLogado = MutableStateFlow<Usuario?>(null)

    override suspend fun login(email: String, senha: String): Result<Usuario> {
        // Simulação de delay de rede
        delay(1500)

        // Simulação de lógica de autenticação
        return if (email == "teste@meuponto.com" && senha == "123456") {
            val usuario = Usuario(
                id = "1",
                nome = "Usuário de Teste",
                email = email,
                biometriaHabilitada = preferenciasRepository.isBiometriaHabilitada()
            )
            _usuarioLogado.value = usuario
            
            // Salva o último e-mail se o login for bem-sucedido
            preferenciasRepository.definirUltimoEmailLogado(email)
            
            Result.success(usuario)
        } else {
            Result.failure(Exception("E-mail ou senha inválidos"))
        }
    }

    override suspend fun cadastrar(nome: String, email: String, senha: String): Result<Usuario> {
        delay(1500)
        val usuario = Usuario(id = "2", nome = nome, email = email)
        _usuarioLogado.value = usuario
        return Result.success(usuario)
    }

    override suspend fun recuperarSenha(email: String): Result<Unit> {
        delay(1000)
        return Result.success(Unit)
    }

    override suspend fun logout() {
        _usuarioLogado.value = null
    }

    override fun observarUsuarioLogado(): Flow<Usuario?> = _usuarioLogado.asStateFlow()

    override suspend fun isUsuarioLogado(): Boolean = _usuarioLogado.value != null
}
