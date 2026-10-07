package hr.vascharlie.lana3.core.ride

enum class RideRecommendation { ACCEPT, CONSIDER, SKIP }

enum class RideDecisionReason {
    MEETS_ACCEPT_THRESHOLDS,
    MEETS_CONSIDER_THRESHOLDS,
    BELOW_CONSIDER_THRESHOLDS,
}

enum class RideProfitabilityMetric {
    EUR_PER_KM,
    EUR_PER_HOUR,
}

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

enum class RideInvalidOfferReason {
    NON_FINITE_METRIC,
    NEGATIVE_PRICE,
    NEGATIVE_DISTANCE,
    NEGATIVE_TIME,
    NON_POSITIVE_TOTAL_DISTANCE,
    NON_POSITIVE_TOTAL_TIME,
}

enum class RideInvalidRuleReason {
    NON_FINITE_THRESHOLD,
    NEGATIVE_THRESHOLD,
    ACCEPT_KM_BELOW_CONSIDER,
    ACCEPT_HOUR_BELOW_CONSIDER,
}

data class RideDecisionExplanation(
    val reason: RideDecisionReason,
    val thresholdEurPerKm: Double,
    val thresholdEurPerHour: Double,
    val failedMetrics: List<RideProfitabilityMetric>,
)

data class RideAssessment(
    val recommendation: RideRecommendation,
    val priceEur: Double,
    val totalKilometers: Double,
    val totalMinutes: Double,
    val eurPerKm: Double,
    val eurPerHour: Double,
    val explanation: RideDecisionExplanation,
)

sealed interface RideAssessmentResult {
    data class Assessed(val assessment: RideAssessment) : RideAssessmentResult
    data class InsufficientData(val missingFields: List<String>) : RideAssessmentResult
    data class InvalidOffer(val reasons: List<RideInvalidOfferReason>) : RideAssessmentResult
    data class InvalidRules(val reasons: List<RideInvalidRuleReason>) : RideAssessmentResult
}

/**
 * Deterministic first slice of Smart Ride Acceptance.
 * Uses the full known work of an offer: pickup + passenger trip distance/time.
 * No threshold is invented by the engine; Charlie's configured rules are required.
 *
 * The shared core returns semantic result codes and numeric evidence only. Human-readable
 * wording belongs to the presentation layer so the same decision can be rendered in any
 * supported language.
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

        val recommendation: RideRecommendation
        val explanation: RideDecisionExplanation

        when {
            eurPerKm >= rules.acceptMinEurPerKm &&
                eurPerHour >= rules.acceptMinEurPerHour -> {
                recommendation = RideRecommendation.ACCEPT
                explanation = RideDecisionExplanation(
                    reason = RideDecisionReason.MEETS_ACCEPT_THRESHOLDS,
                    thresholdEurPerKm = rules.acceptMinEurPerKm,
                    thresholdEurPerHour = rules.acceptMinEurPerHour,
                    failedMetrics = emptyList(),
                )
            }

            eurPerKm >= rules.considerMinEurPerKm &&
                eurPerHour >= rules.considerMinEurPerHour -> {
                recommendation = RideRecommendation.CONSIDER
                explanation = RideDecisionExplanation(
                    reason = RideDecisionReason.MEETS_CONSIDER_THRESHOLDS,
                    thresholdEurPerKm = rules.considerMinEurPerKm,
                    thresholdEurPerHour = rules.considerMinEurPerHour,
                    failedMetrics = emptyList(),
                )
            }

            else -> {
                recommendation = RideRecommendation.SKIP
                explanation = RideDecisionExplanation(
                    reason = RideDecisionReason.BELOW_CONSIDER_THRESHOLDS,
                    thresholdEurPerKm = rules.considerMinEurPerKm,
                    thresholdEurPerHour = rules.considerMinEurPerHour,
                    failedMetrics = buildList {
                        if (eurPerKm < rules.considerMinEurPerKm) {
                            add(RideProfitabilityMetric.EUR_PER_KM)
                        }
                        if (eurPerHour < rules.considerMinEurPerHour) {
                            add(RideProfitabilityMetric.EUR_PER_HOUR)
                        }
                    },
                )
            }
        }

        return RideAssessmentResult.Assessed(
            RideAssessment(
                recommendation = recommendation,
                priceEur = price,
                totalKilometers = totalKm,
                totalMinutes = totalMinutes,
                eurPerKm = eurPerKm,
                eurPerHour = eurPerHour,
                explanation = explanation,
            )
        )
    }

    private fun validateRules(
        rules: RideAcceptanceRules,
    ): List<RideInvalidRuleReason>? {
        val reasons = buildList {
            val values = listOf(
                rules.acceptMinEurPerKm,
                rules.considerMinEurPerKm,
                rules.acceptMinEurPerHour,
                rules.considerMinEurPerHour,
            )

            if (values.any { !it.isFinite() }) {
                add(RideInvalidRuleReason.NON_FINITE_THRESHOLD)
            }
            if (values.any { it < 0.0 }) {
                add(RideInvalidRuleReason.NEGATIVE_THRESHOLD)
            }
            if (rules.acceptMinEurPerKm < rules.considerMinEurPerKm) {
                add(RideInvalidRuleReason.ACCEPT_KM_BELOW_CONSIDER)
            }
            if (rules.acceptMinEurPerHour < rules.considerMinEurPerHour) {
                add(RideInvalidRuleReason.ACCEPT_HOUR_BELOW_CONSIDER)
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
    ): List<RideInvalidOfferReason>? {
        val reasons = buildList {
            val values = listOf(
                price,
                pickupKm,
                tripKm,
                pickupMinutes,
                tripMinutes,
            )

            if (values.any { !it.isFinite() }) {
                add(RideInvalidOfferReason.NON_FINITE_METRIC)
            }
            if (price < 0.0) {
                add(RideInvalidOfferReason.NEGATIVE_PRICE)
            }
            if (pickupKm < 0.0 || tripKm < 0.0) {
                add(RideInvalidOfferReason.NEGATIVE_DISTANCE)
            }
            if (pickupMinutes < 0.0 || tripMinutes < 0.0) {
                add(RideInvalidOfferReason.NEGATIVE_TIME)
            }
            if (pickupKm + tripKm <= 0.0) {
                add(RideInvalidOfferReason.NON_POSITIVE_TOTAL_DISTANCE)
            }
            if (pickupMinutes + tripMinutes <= 0.0) {
                add(RideInvalidOfferReason.NON_POSITIVE_TOTAL_TIME)
            }
        }

        return reasons.takeIf { it.isNotEmpty() }
    }
}
