package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.domain.repository.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth


class UsuarioRepositoryImpl(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : UsuarioRepository {

    override fun getEmailUsuarioLogado(): String? =
        firebaseAuth.currentUser?.email

    override fun getNomeUsuarioLogado(): String? =
        firebaseAuth.currentUser?.displayName
}