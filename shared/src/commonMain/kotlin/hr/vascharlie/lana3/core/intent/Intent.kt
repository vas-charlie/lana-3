package hr.vascharlie.lana3.core.intent

import hr.vascharlie.lana3.core.location.GeoPoint

sealed interface Intent {
    data class SaveNote(val text: String) : Intent
    data class TaxiProfitability(val revenue: Double, val totalKilometers: Double) : Intent

    data class NavigationStop(
        val destinationText: String,
        val resolvedDestination: GeoPoint? = null,
    )

    /**
     * Ordered navigation plan. One stop is ordinary navigation; multiple stops preserve
     * the user's spoken order, e.g. Hotel Osijek then Osijek Airport.
     */
    data class Navigate(
        val stops: List<NavigationStop>,
    ) : Intent {
        constructor(
            destinationText: String,
            resolvedDestination: GeoPoint? = null,
        ) : this(listOf(NavigationStop(destinationText, resolvedDestination)))
    }

    data class Unknown(val originalText: String) : Intent
}
