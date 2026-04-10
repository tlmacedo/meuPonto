// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/Emprego.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Modelo de domínio que representa um emprego/trabalho do usuário.
 *
 * @property id Identificador único do emprego
 * @property nome Nome do emprego
 * @property dataInicioTrabalho Data de início no trabalho (para férias/benefícios)
 * @property descricao Descrição opcional
 * @property ativo Indica se o emprego está ativo
 * @property arquivado Indica se o emprego foi arquivado
 * @property ordem Ordem de exibição
 * @property criadoEm Timestamp de criação
 * @property atualizadoEm Timestamp da última atualização
 */
data class Emprego(
    val id: Long = 0,
    val nome: String,
    val apelido: String? = null,
    val endereco: String? = null,
    val dataInicioTrabalho: LocalDate? = null,
    val dataTerminoTrabalho: LocalDate? = null,
    val descricao: String? = null,
    val ativo: Boolean = true,
    val arquivado: Boolean = false,
    val ordem: Int = 0,
    val logo: String? = null,
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
) {
    val isVisivel: Boolean get() = ativo && !arquivado
    val podeRegistrarPonto: Boolean get() = ativo && !arquivado
}
