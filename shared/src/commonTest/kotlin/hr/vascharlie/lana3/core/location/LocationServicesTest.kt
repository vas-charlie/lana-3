package hr.vascharlie.lana3.core.location

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocationServicesTest {
    private val point = GeoPoint(45.55496, 18.69551)

    @Test
    fun unavailableOnlineEnrichmentDoesNotEraseRawLocation() {
        val snapshot = LocationSnapshot(
            location = LocationAssessment(
                status = LocationStatus.AVAILABLE,
                point = point,
                accuracyMeters = 8.0,
                reason = "Location is usable"
            ),
            enrichments = listOf(
                LocationEnrichmentAvailability(
                    feature = LocationEnrichmentFeature.REVERSE_GEOCODING,
                    available = false,
                    reason = "Internet connection is unavailable"
                ),
                LocationEnrichmentAvailability(
                    feature = LocationEnrichmentFeature.TRAFFIC,
                    available = false,
                    reason = "Internet connection is unavailable"
                )
            )
        )

        assertEquals(point, snapshot.usablePoint())
        assertEquals(2, snapshot.enrichments.count { !it.available })
    }

    @Test
    fun staleLocationIsNotExposedAsUsableEvenIfEnrichmentExists() {
        val snapshot = LocationSnapshot(
            location = LocationAssessment(
                status = LocationStatus.STALE,
                point = point,
                accuracyMeters = 8.0,
                reason = "Location reading is stale"
            ),
            enrichments = listOf(
                LocationEnrichmentAvailability(
                    feature = LocationEnrichmentFeature.REVERSE_GEOCODING,
                    available = true,
                    reason = "Service available"
                )
            )
        )

        assertNull(snapshot.usablePoint())
    }

    @Test
    fun degradedEnrichmentIsRepresentedExplicitly() {
        val item = LocationEnrichmentAvailability(
            feature = LocationEnrichmentFeature.ROUTING,
            available = true,
            degraded = true,
            reason = "Using cached offline route data"
        )

        assertEquals(true, item.available)
        assertEquals(true, item.degraded)
    }
}
