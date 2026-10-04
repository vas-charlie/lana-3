package hr.vascharlie.lana3.core.navigation

import hr.vascharlie.lana3.core.context.LanaContext
import hr.vascharlie.lana3.core.location.GeoPoint
import hr.vascharlie.lana3.core.location.LocationEnrichmentFeature

class NavigationPolicy {
    fun prepare(
        context: LanaContext,
        destination: GeoPoint?,
    ): NavigationAssessment {
        val origin = context.location?.usablePoint()
            ?: return NavigationAssessment(
                status = NavigationStatus.LOCATION_UNAVAILABLE,
                reason = "A reliable current location is required for navigation.",
            )

        if (destination == null) {
            return NavigationAssessment(
                status = NavigationStatus.DESTINATION_UNAVAILABLE,
                reason = "A destination is required for navigation.",
            )
        }

        val routing = context.location.enrichments
            .firstOrNull { it.feature == LocationEnrichmentFeature.ROUTING }

        if (routing != null && !routing.available) {
            return NavigationAssessment(
                status = NavigationStatus.ROUTING_UNAVAILABLE,
                reason = routing.reason,
            )
        }

        return NavigationAssessment(
            status = NavigationStatus.READY,
            request = NavigationRequest(origin, destination),
            reason = if (routing?.degraded == true) routing.reason else "Navigation request is ready.",
        )
    }
}
