package hr.vascharlie.lana3.core.location

/**
 * Device-independent boundary for obtaining a raw location reading.
 *
 * A raw coordinate fix is not inherently an online service. A platform adapter may obtain
 * a reading from GNSS/GPS or another platform source even while the device is offline.
 */
interface LocationProvider {
    suspend fun currentReading(): LocationReading
}

enum class LocationEnrichmentFeature {
    REVERSE_GEOCODING,
    ROUTING,
    TRAFFIC,
    NEARBY_PLACES
}

/**
 * Network or locally cached enrichment is intentionally separate from raw device location.
 * Losing enrichment must never erase a valid raw coordinate fix.
 */
data class LocationEnrichmentAvailability(
    val feature: LocationEnrichmentFeature,
    val available: Boolean,
    val degraded: Boolean = false,
    val reason: String
)

data class LocationSnapshot(
    val location: LocationAssessment,
    val enrichments: List<LocationEnrichmentAvailability> = emptyList()
) {
    fun usablePoint(): GeoPoint? =
        if (location.status == LocationStatus.AVAILABLE) location.point else null
}
