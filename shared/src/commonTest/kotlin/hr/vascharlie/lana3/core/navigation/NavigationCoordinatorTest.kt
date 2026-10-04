package hr.vascharlie.lana3.core.navigation

import hr.vascharlie.lana3.core.context.LanaContext
import hr.vascharlie.lana3.core.location.GeoPoint
import hr.vascharlie.lana3.core.location.LocationAssessment
import hr.vascharlie.lana3.core.location.LocationSnapshot
import hr.vascharlie.lana3.core.location.LocationStatus
import hr.vascharlie.lana3.core.model.LanguageContext
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail

class NavigationCoordinatorTest {
    private val origin = GeoPoint(45.55496, 18.69551)
    private val destination = GeoPoint(45.5600, 18.6800)
    private val language = LanguageContext("hr-HR", 1.0, false)

    @Test
    fun validRequestReturnsProviderRoute() {
        val expected = NavigationRoute(2400, 420, "fake")
        val coordinator = NavigationCoordinator(FakeProvider(expected))
        val result = runSuspend { coordinator.prepareRoute(availableContext(), destination) }

        assertEquals(NavigationExecutionStatus.ROUTE_READY, result.status)
        assertEquals(expected, result.route)
    }

    @Test
    fun invalidContextDoesNotCallRoutingProvider() {
        val provider = FakeProvider(NavigationRoute(1, 1, "fake"))
        val coordinator = NavigationCoordinator(provider)
        val result = runSuspend {
            coordinator.prepareRoute(
                availableContext(status = LocationStatus.STALE),
                destination,
            )
        }

        assertEquals(NavigationExecutionStatus.NOT_READY, result.status)
        assertEquals(0, provider.calls)
        assertNull(result.route)
    }

    @Test
    fun providerFailureIsExplicit() {
        val coordinator = NavigationCoordinator(FakeProvider(null))
        val result = runSuspend { coordinator.prepareRoute(availableContext(), destination) }

        assertEquals(NavigationExecutionStatus.ROUTE_NOT_FOUND, result.status)
        assertNull(result.route)
    }

    private fun availableContext(
        status: LocationStatus = LocationStatus.AVAILABLE,
    ) = LanaContext(
        language = language,
        location = LocationSnapshot(
            location = LocationAssessment(
                status = status,
                point = origin,
                accuracyMeters = 8.0,
                reason = status.name,
            )
        )
    )

    private class FakeProvider(
        private val route: NavigationRoute?,
    ) : NavigationProvider {
        var calls: Int = 0
        override suspend fun route(request: NavigationRequest): NavigationRoute? {
            calls += 1
            return route
        }
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
