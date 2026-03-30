package br.com.tlmacedo.meuponto.data.mapper

import br.com.tlmacedo.meuponto.data.local.database.entity.UsuarioEntity
import br.com.tlmacedo.meuponto.domain.model.Usuario

fun UsuarioEntity.toDomain(): Usuario {
    return Usuario(
        id = id,
        nome = nome,
        email = email,
        biometriaHabilitada = biometriaHabilitada
    )
}

fun Usuario.toEntity(senhaHash: String): UsuarioEntity {
    return UsuarioEntity(
        id = id,
        nome = nome,
        email = email,
        senhaHash = senhaHash,
        biometriaHabilitada = biometriaHabilitada
    )
}
