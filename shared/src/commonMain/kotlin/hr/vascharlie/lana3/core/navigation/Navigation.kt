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


enum class NavigationExecutionStatus {
    ROUTE_READY,
    NOT_READY,
    ROUTE_NOT_FOUND,
}

data class NavigationExecutionResult(
    val status: NavigationExecutionStatus,
    val route: NavigationRoute? = null,
    val reason: String,
)

/**
 * Orchestrates navigation without knowing which map/routing implementation is behind
 * the provider. A route is requested only after the policy has validated the context.
 */
class NavigationCoordinator(
    private val provider: NavigationProvider,
    private val policy: NavigationPolicy = NavigationPolicy(),
) {
    suspend fun prepareRoute(
        context: hr.vascharlie.lana3.core.context.LanaContext,
        destination: GeoPoint?,
    ): NavigationExecutionResult {
        val assessment = policy.prepare(context, destination)
        val request = assessment.request
            ?: return NavigationExecutionResult(
                status = NavigationExecutionStatus.NOT_READY,
                reason = assessment.reason,
            )

        val route = provider.route(request)
            ?: return NavigationExecutionResult(
                status = NavigationExecutionStatus.ROUTE_NOT_FOUND,
                reason = "Routing provider could not produce a route.",
            )

        return NavigationExecutionResult(
            status = NavigationExecutionStatus.ROUTE_READY,
            route = route,
            reason = assessment.reason,
        )
    }
}
