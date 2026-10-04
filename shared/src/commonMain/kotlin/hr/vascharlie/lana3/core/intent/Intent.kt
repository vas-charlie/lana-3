package hr.vascharlie.lana3.core.intent

import hr.vascharlie.lana3.core.location.GeoPoint

sealed interface Intent {
    data class SaveNote(val text: String) : Intent
    data class TaxiProfitability(val revenue: Double, val totalKilometers: Double) : Intent

    /**
     * Semantic navigation intent. Destination resolution is intentionally separate:
     * speech/NLU may supply a place name first, while a geocoder or user selection
     * later supplies coordinates.
     */
    data class Navigate(
        val destinationText: String,
        val resolvedDestination: GeoPoint? = null,
    ) : Intent

    data class Unknown(val originalText: String) : Intent
}
