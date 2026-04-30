// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/Usuario.kt
package br.com.tlmacedo.meuponto.domain.model

import br.com.tlmacedo.meuponto.domain.model.assinatura.AssinaturaUsuario
import br.com.tlmacedo.meuponto.domain.model.assinatura.PlanoAssinatura
import br.com.tlmacedo.meuponto.domain.model.assinatura.RecursoPremium

/**
 * Representa um usuário do sistema.
 *
 * @property id Identificador único do usuário
 * @property nome Nome completo do usuário
 * @property email Endereço de e-mail (usado para login)
 * @property biometriaHabilitada Indica se a autenticação biométrica está ativa para este usuário
 */
data class Usuario(
    val id: String,
    val nome: String,
    val email: String,
    val biometriaHabilitada: Boolean = false,
    val assinatura: AssinaturaUsuario? = null,

    ) {
    val planoAtual: PlanoAssinatura
        get() = assinatura?.plano ?: PlanoAssinatura.FREE

    val isPremium: Boolean
        get() = assinatura?.isPremiumAtivo == true

    fun podeUsar(recurso: RecursoPremium): Boolean = assinatura?.podeUsar(recurso) == true
}