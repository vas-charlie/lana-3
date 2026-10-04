package hr.vascharlie.lana3.core.navigation

import hr.vascharlie.lana3.core.context.LanaContext
import hr.vascharlie.lana3.core.location.GeoPoint
import hr.vascharlie.lana3.core.location.LocationAssessment
import hr.vascharlie.lana3.core.location.LocationEnrichmentAvailability
import hr.vascharlie.lana3.core.location.LocationEnrichmentFeature
import hr.vascharlie.lana3.core.location.LocationSnapshot
import hr.vascharlie.lana3.core.location.LocationStatus
import hr.vascharlie.lana3.core.model.LanguageContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NavigationPolicyTest {
    private val language = LanguageContext("hr-HR", 1.0, false)
    private val origin = GeoPoint(45.55496, 18.69551)
    private val destination = GeoPoint(45.5600, 18.6800)
    private val policy = NavigationPolicy()

    @Test
    fun validLocationAndDestinationPrepareNavigation() {
        val result = policy.prepare(context(LocationStatus.AVAILABLE), destination)
        assertEquals(NavigationStatus.READY, result.status)
        assertEquals(origin, result.request?.origin)
        assertEquals(destination, result.request?.destination)
    }

    @Test
    fun staleLocationCannotStartNavigation() {
        val result = policy.prepare(context(LocationStatus.STALE), destination)
        assertEquals(NavigationStatus.LOCATION_UNAVAILABLE, result.status)
        assertNull(result.request)
    }

    @Test
    fun missingDestinationIsExplicit() {
        val result = policy.prepare(context(LocationStatus.AVAILABLE), null)
        assertEquals(NavigationStatus.DESTINATION_UNAVAILABLE, result.status)
    }

    @Test
    fun unavailableRoutingDoesNotPretendRouteExists() {
        val result = policy.prepare(
            context(
                status = LocationStatus.AVAILABLE,
                routing = LocationEnrichmentAvailability(
                    feature = LocationEnrichmentFeature.ROUTING,
                    available = false,
                    reason = "No online or cached routing service is available",
                )
            ),
            destination,
        )
        assertEquals(NavigationStatus.ROUTING_UNAVAILABLE, result.status)
        assertNull(result.request)
    }

    @Test
    fun degradedCachedRoutingCanRemainUsable() {
        val result = policy.prepare(
            context(
                status = LocationStatus.AVAILABLE,
                routing = LocationEnrichmentAvailability(
                    feature = LocationEnrichmentFeature.ROUTING,
                    available = true,
                    degraded = true,
                    reason = "Using cached offline route data",
                )
            ),
            destination,
        )
        assertEquals(NavigationStatus.READY, result.status)
        assertEquals("Using cached offline route data", result.reason)
    }

    private fun context(
        status: LocationStatus,
        routing: LocationEnrichmentAvailability? = null,
    ) = LanaContext(
        language = language,
        location = LocationSnapshot(
            location = LocationAssessment(
                status = status,
                point = origin,
                accuracyMeters = 8.0,
                reason = status.name,
            ),
            enrichments = listOfNotNull(routing),
        )
    )
}
