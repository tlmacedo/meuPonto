package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.configuracoes

import com.google.android.gms.maps.model.LatLng

data class LocalizacaoTrabalhoUiState(
    val isLoading: Boolean = false,
    val nomeEmprego: String = "",
    val localizacaoInicial: LatLng? = null,
    val localizacaoSelecionada: LatLng? = null,
    val raioMetros: Int = 200,
    val enderecoAproximado: String? = null
)

sealed interface LocalizacaoTrabalhoAction {
    data class SelecionarLocalizacao(val latLng: LatLng) : LocalizacaoTrabalhoAction
    data class AlterarRaio(val raio: Int) : LocalizacaoTrabalhoAction
    data object Confirmar : LocalizacaoTrabalhoAction
    data object Voltar : LocalizacaoTrabalhoAction
}

sealed interface LocalizacaoTrabalhoEvent {
    data class Confirmado(val latitude: Double, val longitude: Double, val raio: Int) : LocalizacaoTrabalhoEvent
    data object Voltar : LocalizacaoTrabalhoEvent
}
