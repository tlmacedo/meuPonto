package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.UsuarioDao
import br.com.tlmacedo.meuponto.data.local.database.entity.UsuarioEntity
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    private lateinit var usuarioDao: UsuarioDao
    private lateinit var authRepository: AuthRepositoryImpl

    @Before
    fun setup() {
        usuarioDao = mockk(relaxed = true)
        authRepository = AuthRepositoryImpl(usuarioDao)
    }

    @Test
    fun `login deve retornar sucesso quando credenciais estao corretas`() = runTest {
        val email = "teste@teste.com"
        val senha = "123456"
        val usuarioEntity = UsuarioEntity(
            id = "1",
            nome = "Teste",
            email = email,
            senhaHash = senha,
            biometriaHabilitada = false
        )

        coEvery { usuarioDao.buscarPorEmail(email) } returns usuarioEntity

        val resultado = authRepository.login(email, senha)

        assertThat(resultado.isSuccess).isTrue()
        assertThat(resultado.getOrNull()?.email).isEqualTo(email)
    }

    @Test
    fun `login deve retornar falha quando usuario nao existe`() = runTest {
        val email = "naoexiste@teste.com"
        coEvery { usuarioDao.buscarPorEmail(email) } returns null

        val resultado = authRepository.login(email, "123456")

        assertThat(resultado.isFailure).isTrue()
        assertThat(resultado.exceptionOrNull()?.message).isEqualTo("Usuário não encontrado.")
    }

    @Test
    fun `login deve retornar falha quando senha esta incorreta`() = runTest {
        val email = "teste@teste.com"
        val usuarioEntity = UsuarioEntity(
            id = "1",
            nome = "Teste",
            email = email,
            senhaHash = "senha_correta",
            biometriaHabilitada = false
        )

        coEvery { usuarioDao.buscarPorEmail(email) } returns usuarioEntity

        val resultado = authRepository.login(email, "senha_errada")

        assertThat(resultado.isFailure).isTrue()
        assertThat(resultado.exceptionOrNull()?.message).isEqualTo("Senha incorreta.")
    }

    @Test
    fun `register deve retornar sucesso quando email nao esta em uso`() = runTest {
        val nome = "Novo Usuario"
        val email = "novo@teste.com"
        val senha = "password"

        coEvery { usuarioDao.buscarPorEmail(email) } returns null

        val resultado = authRepository.register(nome, email, senha)

        assertThat(resultado.isSuccess).isTrue()
        assertThat(resultado.getOrNull()?.nome).isEqualTo(nome)
        coVerify { usuarioDao.inserir(any()) }
    }

    @Test
    fun `register deve retornar falha quando email ja esta em uso`() = runTest {
        val email = "existente@teste.com"
        coEvery { usuarioDao.buscarPorEmail(email) } returns UsuarioEntity(
            id = "1", nome = "Existente", email = email, senhaHash = "123", biometriaHabilitada = false
        )

        val resultado = authRepository.register("Nome", email, "senha")

        assertThat(resultado.isFailure).isTrue()
        assertThat(resultado.exceptionOrNull()?.message).isEqualTo("E-mail já cadastrado.")
    }
}
