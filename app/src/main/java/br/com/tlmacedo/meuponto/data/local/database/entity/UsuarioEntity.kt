package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey
    val id: String,
    val nome: String,
    val email: String,
    val senhaHash: String, // Armazena a senha (em um app real, seria criptografada)
    val biometriaHabilitada: Boolean = false
)
