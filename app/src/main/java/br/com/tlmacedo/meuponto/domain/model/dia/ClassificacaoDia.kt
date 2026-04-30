// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/dia/ClassificacaoDia.kt
package br.com.tlmacedo.meuponto.domain.model.dia

enum class ClassificacaoDia(
    val descricao: String
) {
    NORMAL("Dia normal"),
    FOLGA("Folga"),
    DESCANSO("Descanso"),
    AUSENCIA("Ausência")
}