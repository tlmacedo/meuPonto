// path: app/src/main/java/br/com/tlmacedo/meuponto/domain/repository/UsuarioRepository.kt
package br.com.tlmacedo.meuponto.domain.repository

interface UsuarioRepository {
    fun getEmailUsuarioLogado(): String?
    fun getNomeUsuarioLogado(): String?
}
