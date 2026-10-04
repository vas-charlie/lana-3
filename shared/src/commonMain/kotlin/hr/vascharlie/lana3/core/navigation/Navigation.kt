package hr.vascharlie.lana3.core.navigation

import hr.vascharlie.lana3.core.location.GeoPoint

data class NavigationRequest(
    val origin: GeoPoint,
    val destination: GeoPoint,
)

enum class NavigationStatus {
    READY,
    LOCATION_UNAVAILABLE,
    DESTINATION_UNAVAILABLE,
    ROUTING_UNAVAILABLE,
}

data class NavigationAssessment(
    val status: NavigationStatus,
    val request: NavigationRequest? = null,
    val reason: String,
)

/**
 * Platform-independent routing boundary. Platform adapters may use online routing,
 * cached/offline maps, or another provider without leaking provider APIs into core.
 */
interface NavigationProvider {
    suspend fun route(request: NavigationRequest): NavigationRoute?
}

data class NavigationRoute(
    val distanceMeters: Long,
    val durationSeconds: Long,
    val providerName: String,
)
