package hr.vascharlie.lana3

import android.content.Intent
import hr.vascharlie.lana3.core.ride.RideOffer

data class SmartRidePrefill(
    val offer: RideOffer,
    val source: String?,
)

/**
 * Android-only handoff boundary for structured Smart Ride offer data.
 *
 * Voice, screen-awareness or future integration adapters may populate these extras
 * after they have extracted/normalized values. Smart Ride remains responsible for
 * validating the resulting RideOffer and never treats missing values as known.
 */
object SmartRidePrefillContract {
    private const val PREFIX = "hr.vascharlie.lana3.smart_ride."

    const val SOURCE_VOICE = "voice"

    const val EXTRA_SOURCE = PREFIX + "source"
    const val EXTRA_PRICE_EUR = PREFIX + "price_eur"
    const val EXTRA_PICKUP_KM = PREFIX + "pickup_km"
    const val EXTRA_TRIP_KM = PREFIX + "trip_km"
    const val EXTRA_PICKUP_MINUTES = PREFIX + "pickup_minutes"
    const val EXTRA_TRIP_MINUTES = PREFIX + "trip_minutes"
    const val EXTRA_EMPTY_RETURN_KM = PREFIX + "empty_return_km"
    const val EXTRA_EMPTY_RETURN_MINUTES = PREFIX + "empty_return_minutes"

    private val numericKeys = listOf(
        EXTRA_PRICE_EUR,
        EXTRA_PICKUP_KM,
        EXTRA_TRIP_KM,
        EXTRA_PICKUP_MINUTES,
        EXTRA_TRIP_MINUTES,
        EXTRA_EMPTY_RETURN_KM,
        EXTRA_EMPTY_RETURN_MINUTES,
    )

    fun write(
        intent: Intent,
        offer: RideOffer,
        source: String? = null,
    ): Intent {
        source
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { intent.putExtra(EXTRA_SOURCE, it) }

        putIfKnown(intent, EXTRA_PRICE_EUR, offer.priceEur)
        putIfKnown(intent, EXTRA_PICKUP_KM, offer.pickupKm)
        putIfKnown(intent, EXTRA_TRIP_KM, offer.tripKm)
        putIfKnown(intent, EXTRA_PICKUP_MINUTES, offer.pickupMinutes)
        putIfKnown(intent, EXTRA_TRIP_MINUTES, offer.tripMinutes)
        putIfKnown(intent, EXTRA_EMPTY_RETURN_KM, offer.emptyReturnKm)
        putIfKnown(intent, EXTRA_EMPTY_RETURN_MINUTES, offer.emptyReturnMinutes)

        return intent
    }

    fun read(intent: Intent): SmartRidePrefill? {
        if (numericKeys.none(intent::hasExtra)) return null

        return SmartRidePrefill(
            offer = RideOffer(
                priceEur = readKnownDouble(intent, EXTRA_PRICE_EUR),
                pickupKm = readKnownDouble(intent, EXTRA_PICKUP_KM),
                tripKm = readKnownDouble(intent, EXTRA_TRIP_KM),
                pickupMinutes = readKnownDouble(intent, EXTRA_PICKUP_MINUTES),
                tripMinutes = readKnownDouble(intent, EXTRA_TRIP_MINUTES),
                emptyReturnKm = readKnownDouble(intent, EXTRA_EMPTY_RETURN_KM),
                emptyReturnMinutes =
                    readKnownDouble(intent, EXTRA_EMPTY_RETURN_MINUTES),
            ),
            source = intent
                .getStringExtra(EXTRA_SOURCE)
                ?.trim()
                ?.takeIf { it.isNotEmpty() },
        )
    }

    private fun putIfKnown(
        intent: Intent,
        key: String,
        value: Double?,
    ) {
        if (value != null) {
            intent.putExtra(key, value)
        }
    }

    private fun readKnownDouble(
        intent: Intent,
        key: String,
    ): Double? =
        if (intent.hasExtra(key)) {
            intent.getDoubleExtra(key, 0.0)
        } else {
            null
        }
}
