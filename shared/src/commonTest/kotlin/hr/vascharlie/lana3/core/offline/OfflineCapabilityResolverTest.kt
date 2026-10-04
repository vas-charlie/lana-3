package hr.vascharlie.lana3.core.offline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OfflineCapabilityResolverTest {
    private val resolver = OfflineCapabilityResolver()

    @Test
    fun localCapabilityWorksOffline() {
        val result = resolver.resolve(ConnectivityState.OFFLINE, NetworkRequirement.LOCAL_ONLY)
        assertTrue(result.available)
        assertEquals(AvailabilityMode.LOCAL, result.mode)
    }

    @Test
    fun networkRequiredCapabilityDoesNotPretendToWorkOffline() {
        val result = resolver.resolve(ConnectivityState.OFFLINE, NetworkRequirement.NETWORK_REQUIRED)
        assertFalse(result.available)
        assertEquals(AvailabilityMode.UNAVAILABLE, result.mode)
    }

    @Test
    fun optionalNetworkFallsBackToDegradedMode() {
        val result = resolver.resolve(ConnectivityState.OFFLINE, NetworkRequirement.NETWORK_OPTIONAL)
        assertTrue(result.available)
        assertEquals(AvailabilityMode.DEGRADED, result.mode)
    }

    @Test
    fun unknownConnectivityNeverClaimsNetworkIsAvailable() {
        val result = resolver.resolve(ConnectivityState.UNKNOWN, NetworkRequirement.NETWORK_REQUIRED)
        assertFalse(result.available)
        assertEquals(AvailabilityMode.UNAVAILABLE, result.mode)
    }
}
