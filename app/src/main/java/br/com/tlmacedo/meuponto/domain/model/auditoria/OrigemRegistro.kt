package br.com.tlmacedo.meuponto.domain.model.auditoria

enum class OrigemRegistro {
    MANUAL,
    ATALHO_ANDROID,
    WIDGET,
    NOTIFICACAO,
    OCR,
    IMPORTACAO,
    SINCRONIZACAO
}