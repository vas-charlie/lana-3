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


data class NavigationLeg(
    val stopIndex: Int,
    val destinationText: String,
    val request: NavigationRequest,
    val route: NavigationRoute,
)

sealed interface NavigationPlanResult {
    data class Ready(val legs: List<NavigationLeg>) : NavigationPlanResult
    data class NotReady(val stopIndex: Int, val reason: String) : NavigationPlanResult
    data class RouteNotFound(val stopIndex: Int, val reason: String) : NavigationPlanResult
}

/**
 * Builds an ordered route plan for every resolved stop. The first leg starts at the
 * current reliable location; each following leg starts at the previous destination.
 */
class MultiStopNavigationCoordinator(
    private val provider: NavigationProvider,
    private val policy: NavigationPolicy = NavigationPolicy(),
) {
    suspend fun preparePlan(
        context: hr.vascharlie.lana3.core.context.LanaContext,
        intent: hr.vascharlie.lana3.core.intent.Intent.Navigate,
    ): NavigationPlanResult {
        if (intent.stops.isEmpty()) {
            return NavigationPlanResult.NotReady(
                stopIndex = 0,
                reason = "At least one navigation stop is required.",
            )
        }

        var origin = context.location?.usablePoint()
            ?: return NavigationPlanResult.NotReady(
                stopIndex = 0,
                reason = "A reliable current location is required for navigation.",
            )

        val legs = mutableListOf<NavigationLeg>()
        intent.stops.forEachIndexed { index, stop ->
            val destination = stop.resolvedDestination
                ?: return NavigationPlanResult.NotReady(
                    stopIndex = index,
                    reason = "Navigation stop has not been resolved to coordinates.",
                )

            val legContext = context.copy(
                location = context.location?.copy(
                    location = context.location.location.copy(point = origin),
                )
            )
            val assessment = policy.prepare(legContext, destination)
            val request = assessment.request
                ?: return NavigationPlanResult.NotReady(
                    stopIndex = index,
                    reason = assessment.reason,
                )

            val route = provider.route(request)
                ?: return NavigationPlanResult.RouteNotFound(
                    stopIndex = index,
                    reason = "Routing provider could not produce route for stop " + (index + 1) + ".",
                )

            legs += NavigationLeg(
                stopIndex = index,
                destinationText = stop.destinationText,
                request = request,
                route = route,
            )
            origin = destination
        }

        return NavigationPlanResult.Ready(legs)
    }
}
