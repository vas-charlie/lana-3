package hr.vascharlie.lana3.core.ride

enum class RideRecommendation { ACCEPT, CONSIDER, SKIP }

data class RideOffer(
    val priceEur: Double?,
    val pickupKm: Double?,
    val tripKm: Double?,
    val pickupMinutes: Double?,
    val tripMinutes: Double?,
)

data class RideAcceptanceRules(
    val acceptMinEurPerKm: Double,
    val considerMinEurPerKm: Double,
    val acceptMinEurPerHour: Double,
    val considerMinEurPerHour: Double,
)

data class RideAssessment(
    val recommendation: RideRecommendation,
    val eurPerKm: Double,
    val eurPerHour: Double,
    val reasons: List<String>,
)

sealed interface RideAssessmentResult {
    data class Assessed(val assessment: RideAssessment) : RideAssessmentResult
    data class InsufficientData(val missingFields: List<String>) : RideAssessmentResult
}

/**
 * Deterministic first slice of Smart Ride Acceptance.
 * Uses the full known work of an offer: pickup + passenger trip distance/time.
 * No threshold is invented by the engine; Charlie's configured rules are required.
 */
class SmartRideAcceptance {
    fun assess(offer: RideOffer, rules: RideAcceptanceRules): RideAssessmentResult {
        val missing = buildList {
            if (offer.priceEur == null) add("priceEur")
            if (offer.pickupKm == null) add("pickupKm")
            if (offer.tripKm == null) add("tripKm")
            if (offer.pickupMinutes == null) add("pickupMinutes")
            if (offer.tripMinutes == null) add("tripMinutes")
        }
        if (missing.isNotEmpty()) return RideAssessmentResult.InsufficientData(missing)

        val price = offer.priceEur!!
        val totalKm = offer.pickupKm!! + offer.tripKm!!
        val totalMinutes = offer.pickupMinutes!! + offer.tripMinutes!!
        if (price < 0.0 || totalKm <= 0.0 || totalMinutes <= 0.0) {
            return RideAssessmentResult.InsufficientData(listOf("validPositiveOfferMetrics"))
        }

        val eurPerKm = price / totalKm
        val eurPerHour = price / totalMinutes * 60.0

        val recommendation = when {
            eurPerKm >= rules.acceptMinEurPerKm && eurPerHour >= rules.acceptMinEurPerHour ->
                RideRecommendation.ACCEPT
            eurPerKm >= rules.considerMinEurPerKm && eurPerHour >= rules.considerMinEurPerHour ->
                RideRecommendation.CONSIDER
            else -> RideRecommendation.SKIP
        }

        return RideAssessmentResult.Assessed(
            RideAssessment(
                recommendation = recommendation,
                eurPerKm = eurPerKm,
                eurPerHour = eurPerHour,
                reasons = listOf(
                    "Price: $price EUR",
                    "Total distance including pickup: $totalKm km",
                    "Total time including pickup: $totalMinutes min",
                    "EUR/km: $eurPerKm",
                    "EUR/h: $eurPerHour",
                ),
            )
        )
    }
}
