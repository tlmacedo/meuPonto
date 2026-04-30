// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/mapper/TipoAusenciaUiMapper.kt
package br.com.tlmacedo.meuponto.presentation.mapper

import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.presentation.model.TipoAusenciaCor

fun TipoAusencia.toTipoAusenciaCor(): TipoAusenciaCor {
    return when {
        this.zeraJornada -> TipoAusenciaCor.VERDE
        this.descontaDoBanco -> TipoAusenciaCor.VERMELHO
        else -> TipoAusenciaCor.AZUL
    }
}