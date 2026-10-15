package hr.vascharlie.lana3.core.taxi

import kotlin.math.max

enum class TaxiPricingRule {
    FLAT_UP_TO_DISTANCE_THEN_PER_KM,
    NEGOTIATED,
}

data class TaxiTariff(
    val slot: Int,
    val name: String,
    val enabled: Boolean,
    val pricingRule: TaxiPricingRule,
    val basePriceEur: Double? = null,
    val includedKm: Double? = null,
    val eurPerKmAfterIncluded: Double? = null,
    val eurPerMinute: Double? = null,
    val minimumPriceEur: Double? = null,
)

sealed interface TaxiFareResult {
    data class Calculated(val amountEur: Double) : TaxiFareResult
    data class Invalid(val reasons: List<String>) : TaxiFareResult
}

class TaxiFareCalculator {
    fun calculate(
        tariff: TaxiTariff,
        distanceKm: Double,
        minutes: Double = 0.0,
    ): TaxiFareResult {
        val reasons = buildList {
            if (!tariff.enabled) add("tariff_disabled")
            if (!distanceKm.isFinite() || distanceKm < 0.0) add("invalid_distance")
            if (!minutes.isFinite() || minutes < 0.0) add("invalid_minutes")
            if (tariff.pricingRule == TaxiPricingRule.NEGOTIATED) add("negotiated_price_required")
            if (tariff.pricingRule == TaxiPricingRule.FLAT_UP_TO_DISTANCE_THEN_PER_KM) {
                if (tariff.basePriceEur == null || tariff.basePriceEur < 0.0) add("invalid_base_price")
                if (tariff.includedKm == null || tariff.includedKm < 0.0) add("invalid_included_km")
                if (tariff.eurPerKmAfterIncluded == null || tariff.eurPerKmAfterIncluded < 0.0) add("invalid_per_km")
                if (tariff.eurPerMinute != null && tariff.eurPerMinute < 0.0) add("invalid_per_minute")
                if (tariff.minimumPriceEur != null && tariff.minimumPriceEur < 0.0) add("invalid_minimum")
            }
        }
        if (reasons.isNotEmpty()) return TaxiFareResult.Invalid(reasons)

        val distanceCharge =
            max(0.0, distanceKm - tariff.includedKm!!) * tariff.eurPerKmAfterIncluded!!
        val timeCharge = (tariff.eurPerMinute ?: 0.0) * minutes
        val raw = tariff.basePriceEur!! + distanceCharge + timeCharge
        return TaxiFareResult.Calculated(max(raw, tariff.minimumPriceEur ?: 0.0))
    }

    fun negotiated(amountEur: Double): TaxiFareResult =
        if (amountEur.isFinite() && amountEur >= 0.0) {
            TaxiFareResult.Calculated(amountEur)
        } else {
            TaxiFareResult.Invalid(listOf("invalid_negotiated_price"))
        }
}

object DefaultTaxiTariffs {
    val T1 = TaxiTariff(
        slot = 1,
        name = "1–4 putnika",
        enabled = true,
        pricingRule = TaxiPricingRule.FLAT_UP_TO_DISTANCE_THEN_PER_KM,
        basePriceEur = 5.0,
        includedKm = 3.0,
        eurPerKmAfterIncluded = 1.25,
    )
}
