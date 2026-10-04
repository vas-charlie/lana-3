package hr.vascharlie.lana3.core.intent

sealed interface Intent {
    data class SaveNote(val text: String) : Intent
    data class TaxiProfitability(val revenue: Double, val totalKilometers: Double) : Intent
    data class Unknown(val originalText: String) : Intent
}
