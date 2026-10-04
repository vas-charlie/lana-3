package hr.vascharlie.lana3.core.location

/**
 * Result returned by the location coordinator. Raw device location and enrichment
 * remain separate so an online-service failure cannot destroy a valid coordinate fix.
 */
data class LocationResult(
    val snapshot: LocationSnapshot
)

class LocationCoordinator(
    private val provider: LocationProvider,
    private val policy: LocationPolicy = LocationPolicy()
) {
    suspend fun currentLocation(
        enrichments: List<LocationEnrichmentAvailability> = emptyList()
    ): LocationResult {
        val assessment = policy.assess(provider.currentReading())
        return LocationResult(
            LocationSnapshot(
                location = assessment,
                enrichments = enrichments
            )
        )
    }
}
