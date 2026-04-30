// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/feriado/TipoFeriadoMapper.kt
package br.com.tlmacedo.meuponto.domain.model.feriado

import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia

fun TipoFeriado.toTipoAusencia(): TipoAusencia {
    return when (this) {
        TipoFeriado.NACIONAL, TipoFeriado.ESTADUAL, TipoFeriado.MUNICIPAL -> TipoAusencia.Feriado.Oficial
        TipoFeriado.PONTE -> TipoAusencia.Feriado.DiaPonte
        TipoFeriado.FACULTATIVO -> TipoAusencia.Feriado.Facultativo
    }
}