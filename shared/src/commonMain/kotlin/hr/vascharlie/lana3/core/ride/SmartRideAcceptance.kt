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
    data class InvalidOffer(val reasons: List<String>) : RideAssessmentResult
    data class InvalidRules(val reasons: List<String>) : RideAssessmentResult
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

        validateRules(rules)?.let {
            return RideAssessmentResult.InvalidRules(it)
        }

        val price = offer.priceEur!!
        val pickupKm = offer.pickupKm!!
        val tripKm = offer.tripKm!!
        val pickupMinutes = offer.pickupMinutes!!
        val tripMinutes = offer.tripMinutes!!

        validateOffer(
            price = price,
            pickupKm = pickupKm,
            tripKm = tripKm,
            pickupMinutes = pickupMinutes,
            tripMinutes = tripMinutes,
        )?.let {
            return RideAssessmentResult.InvalidOffer(it)
        }

        val totalKm = pickupKm + tripKm
        val totalMinutes = pickupMinutes + tripMinutes
        val eurPerKm = price / totalKm
        val eurPerHour = price / totalMinutes * 60.0

        val recommendation = when {
            eurPerKm >= rules.acceptMinEurPerKm &&
                eurPerHour >= rules.acceptMinEurPerHour ->
                RideRecommendation.ACCEPT

            eurPerKm >= rules.considerMinEurPerKm &&
                eurPerHour >= rules.considerMinEurPerHour ->
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

    private fun validateRules(rules: RideAcceptanceRules): List<String>? {
        val reasons = buildList {
            val values = listOf(
                rules.acceptMinEurPerKm,
                rules.considerMinEurPerKm,
                rules.acceptMinEurPerHour,
                rules.considerMinEurPerHour,
            )

            if (values.any { !it.isFinite() }) {
                add("Rule thresholds must be finite numbers.")
            }
            if (values.any { it < 0.0 }) {
                add("Rule thresholds must not be negative.")
            }
            if (rules.acceptMinEurPerKm < rules.considerMinEurPerKm) {
                add("Accept EUR/km threshold must be at least the consider threshold.")
            }
            if (rules.acceptMinEurPerHour < rules.considerMinEurPerHour) {
                add("Accept EUR/h threshold must be at least the consider threshold.")
            }
        }

        return reasons.takeIf { it.isNotEmpty() }
    }

    private fun validateOffer(
        price: Double,
        pickupKm: Double,
        tripKm: Double,
        pickupMinutes: Double,
        tripMinutes: Double,
    ): List<String>? {
        val reasons = buildList {
            val values = listOf(
                price,
                pickupKm,
                tripKm,
                pickupMinutes,
                tripMinutes,
            )

            if (values.any { !it.isFinite() }) {
                add("Offer metrics must be finite numbers.")
            }
            if (price < 0.0) {
                add("Price must not be negative.")
            }
            if (pickupKm < 0.0 || tripKm < 0.0) {
                add("Pickup and trip distance must not be negative.")
            }
            if (pickupMinutes < 0.0 || tripMinutes < 0.0) {
                add("Pickup and trip time must not be negative.")
            }
            if (pickupKm + tripKm <= 0.0) {
                add("Total distance must be greater than zero.")
            }
            if (pickupMinutes + tripMinutes <= 0.0) {
                add("Total time must be greater than zero.")
            }
        }

        return reasons.takeIf { it.isNotEmpty() }
    }
}
