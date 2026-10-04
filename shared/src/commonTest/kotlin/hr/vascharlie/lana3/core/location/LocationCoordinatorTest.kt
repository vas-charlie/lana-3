package hr.vascharlie.lana3.core.location

import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail

class LocationCoordinatorTest {
    private val point = GeoPoint(45.55496, 18.69551)

    @Test
    fun offlineEnrichmentFailureKeepsValidRawGpsFix() {
        val provider = FakeLocationProvider(
            LocationReading(point, 7.0, 2_000, permissionGranted = true, locationEnabled = true)
        )
        val coordinator = LocationCoordinator(provider)

        val result = runSuspend {
            coordinator.currentLocation(
                listOf(
                    LocationEnrichmentAvailability(
                        LocationEnrichmentFeature.REVERSE_GEOCODING,
                        available = false,
                        reason = "Internet unavailable"
                    )
                )
            )
        }

        assertEquals(LocationStatus.AVAILABLE, result.snapshot.location.status)
        assertEquals(point, result.snapshot.usablePoint())
        assertEquals(false, result.snapshot.enrichments.single().available)
    }

    @Test
    fun missingGpsFixNeverGuessesCoordinates() {
        val provider = FakeLocationProvider(
            LocationReading(null, null, null, permissionGranted = true, locationEnabled = true)
        )
        val coordinator = LocationCoordinator(provider)

        val result = runSuspend { coordinator.currentLocation() }

        assertEquals(LocationStatus.UNAVAILABLE, result.snapshot.location.status)
        assertNull(result.snapshot.usablePoint())
    }

    private class FakeLocationProvider(
        private val reading: LocationReading
    ) : LocationProvider {
        override suspend fun currentReading(): LocationReading = reading
    }

    private fun <T> runSuspend(block: suspend () -> T): T {
        var outcome: Result<T>? = null
        block.startCoroutine(object : Continuation<T> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(result: Result<T>) {
                outcome = result
            }
        })
        return outcome?.getOrThrow() ?: fail("Suspend block did not complete synchronously")
    }
}
