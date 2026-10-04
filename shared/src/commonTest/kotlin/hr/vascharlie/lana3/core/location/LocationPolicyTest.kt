package hr.vascharlie.lana3.core.location

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocationPolicyTest {
    private val policy = LocationPolicy(maxAgeMillis = 30_000, maxAccuracyMeters = 100.0)
    private val point = GeoPoint(45.8150, 15.9819)

    @Test
    fun validGpsReadingIsAvailableWithoutNetworkState() {
        val result = policy.assess(LocationReading(point, 12.0, 5_000, true, true))
        assertEquals(LocationStatus.AVAILABLE, result.status)
        assertEquals(point, result.point)
    }

    @Test
    fun deniedPermissionIsExplicit() {
        val result = policy.assess(LocationReading(point, 10.0, 1_000, false, true))
        assertEquals(LocationStatus.PERMISSION_DENIED, result.status)
        assertNull(result.point)
    }

    @Test
    fun disabledLocationIsExplicit() {
        val result = policy.assess(LocationReading(point, 10.0, 1_000, true, false))
        assertEquals(LocationStatus.LOCATION_DISABLED, result.status)
    }

    @Test
    fun staleReadingIsNotPresentedAsCurrent() {
        val result = policy.assess(LocationReading(point, 10.0, 60_000, true, true))
        assertEquals(LocationStatus.STALE, result.status)
    }

    @Test
    fun inaccurateReadingIsNotPresentedAsReliable() {
        val result = policy.assess(LocationReading(point, 250.0, 1_000, true, true))
        assertEquals(LocationStatus.INACCURATE, result.status)
    }

    @Test
    fun missingReadingIsUnavailable() {
        val result = policy.assess(LocationReading(null, null, null, true, true))
        assertEquals(LocationStatus.UNAVAILABLE, result.status)
    }
}
