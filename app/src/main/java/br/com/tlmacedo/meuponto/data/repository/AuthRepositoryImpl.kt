package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.UsuarioDao
import br.com.tlmacedo.meuponto.data.local.database.entity.UsuarioEntity
import br.com.tlmacedo.meuponto.domain.model.Usuario
import br.com.tlmacedo.meuponto.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val usuarioDao: UsuarioDao
) : AuthRepository {

    private val _usuarioLogado = MutableStateFlow<Usuario?>(null)

    override suspend fun login(email: String, senha: String): Result<Usuario> {
        return try {
            val entity = usuarioDao.buscarPorEmail(email)
                ?: return Result.failure(Exception("Usuário não encontrado."))

            if (entity.senhaHash != senha) {
                return Result.failure(Exception("Senha incorreta."))
            }

            val usuario = Usuario(
                id = entity.id,
                nome = entity.nome,
                email = entity.email,
                biometriaHabilitada = entity.biometriaHabilitada
            )
            _usuarioLogado.value = usuario
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginPorEmail(email: String): Result<Usuario> {
        return try {
            val entity = usuarioDao.buscarPorEmail(email)
                ?: return Result.failure(Exception("Usuário não encontrado."))

            val usuario = Usuario(
                id = entity.id,
                nome = entity.nome,
                email = entity.email,
                biometriaHabilitada = entity.biometriaHabilitada
            )
            _usuarioLogado.value = usuario
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(nome: String, email: String, senha: String): Result<Usuario> {
        return try {
            val existente = usuarioDao.buscarPorEmail(email)
            if (existente != null) {
                return Result.failure(Exception("E-mail já cadastrado."))
            }

            val novoId = UUID.randomUUID().toString()
            val entity = UsuarioEntity(
                id = novoId,
                nome = nome,
                email = email,
                senhaHash = senha,
                biometriaHabilitada = false
            )
            usuarioDao.inserir(entity)

            val usuario = Usuario(
                id = novoId,
                nome = nome,
                email = email,
                biometriaHabilitada = false
            )
            _usuarioLogado.value = usuario
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recuperarSenha(email: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun logout() {
        _usuarioLogado.value = null
    }

    override fun observarUsuarioLogado(): Flow<Usuario?> = _usuarioLogado.asStateFlow()

    override suspend fun isUsuarioLogado(): Boolean = _usuarioLogado.value != null
}
