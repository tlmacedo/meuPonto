// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/Usuario.kt
package br.com.tlmacedo.meuponto.domain.model

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
    val biometriaHabilitada: Boolean = false
)
