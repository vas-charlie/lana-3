package hr.vascharlie.lana3.core.navigation

import hr.vascharlie.lana3.core.location.GeoPoint

data class PlaceCandidate(
    val displayName: String,
    val point: GeoPoint,
    val confidence: Double? = null,
)

sealed interface DestinationResolution {
    data class Resolved(val candidate: PlaceCandidate) : DestinationResolution
    data class Ambiguous(val candidates: List<PlaceCandidate>) : DestinationResolution
    data class NotFound(val query: String) : DestinationResolution
}

/**
 * Provider-neutral place search boundary. Implementations may use an online geocoder,
 * an offline index, cached places, or a platform service.
 */
interface PlaceResolver {
    suspend fun resolve(query: String): List<PlaceCandidate>
}

class DestinationResolver(
    private val provider: PlaceResolver,
) {
    suspend fun resolve(query: String): DestinationResolution {
        val normalized = query.trim()
        if (normalized.isEmpty()) return DestinationResolution.NotFound(query)

        val candidates = provider.resolve(normalized)
        return when (candidates.size) {
            0 -> DestinationResolution.NotFound(normalized)
            1 -> DestinationResolution.Resolved(candidates.single())
            else -> DestinationResolution.Ambiguous(candidates)
        }
    }
}
