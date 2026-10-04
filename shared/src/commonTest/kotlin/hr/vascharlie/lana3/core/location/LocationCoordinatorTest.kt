package hr.vascharlie.lana3.core.location

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocationCoordinatorTest {
    private val point = GeoPoint(45.55496, 18.69551)

    @Test
    fun offlineEnrichmentFailureKeepsValidRawGpsFix() = kotlinx.coroutines.test.runTest {
        val provider = FakeLocationProvider(
            LocationReading(point, 7.0, 2_000, permissionGranted = true, locationEnabled = true)
        )
        val coordinator = LocationCoordinator(provider)

        val result = coordinator.currentLocation(
            listOf(
                LocationEnrichmentAvailability(
                    LocationEnrichmentFeature.REVERSE_GEOCODING,
                    available = false,
                    reason = "Internet unavailable"
                )
            )
        )

        assertEquals(LocationStatus.AVAILABLE, result.snapshot.location.status)
        assertEquals(point, result.snapshot.usablePoint())
        assertEquals(false, result.snapshot.enrichments.single().available)
    }

    @Test
    fun missingGpsFixNeverGuessesCoordinates() = kotlinx.coroutines.test.runTest {
        val provider = FakeLocationProvider(
            LocationReading(null, null, null, permissionGranted = true, locationEnabled = true)
        )
        val coordinator = LocationCoordinator(provider)

        val result = coordinator.currentLocation()

        assertEquals(LocationStatus.UNAVAILABLE, result.snapshot.location.status)
        assertNull(result.snapshot.usablePoint())
    }

    private class FakeLocationProvider(
        private val reading: LocationReading
    ) : LocationProvider {
        override suspend fun currentReading(): LocationReading = reading
    }
}
