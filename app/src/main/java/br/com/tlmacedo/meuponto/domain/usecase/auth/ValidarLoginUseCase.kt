package br.com.tlmacedo.meuponto.domain.usecase.auth

import javax.inject.Inject

class ValidarLoginUseCase @Inject constructor() {

    sealed class ResultadoValidacao {
        data object Valido : ResultadoValidacao()
        data class Invalido(
            val emailErro: String? = null,
            val senhaErro: String? = null
        ) : ResultadoValidacao()

        val isValido: Boolean get() = this is Valido
    }

    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()

    operator fun invoke(email: String, senha: String): ResultadoValidacao {
        val emailInvalido = !emailRegex.matches(email)
        val senhaInvalida = senha.length < 4

        return if (!emailInvalido && !senhaInvalida) {
            ResultadoValidacao.Valido
        } else {
            ResultadoValidacao.Invalido(
                emailErro = if (emailInvalido) "E-mail inválido" else null,
                senhaErro = if (senhaInvalida) "Senha deve ter ao menos 4 caracteres" else null
            )
        }
    }
}
