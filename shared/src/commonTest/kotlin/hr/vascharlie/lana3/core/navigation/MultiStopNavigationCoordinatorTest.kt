package hr.vascharlie.lana3.core.navigation

import hr.vascharlie.lana3.core.context.LanaContext
import hr.vascharlie.lana3.core.intent.Intent
import hr.vascharlie.lana3.core.intent.Intent.NavigationStop
import hr.vascharlie.lana3.core.language.LanguageContext
import hr.vascharlie.lana3.core.location.*
import kotlin.coroutines.*
import kotlin.test.*

class MultiStopNavigationCoordinatorTest {
    @Test
    fun routesEachStopInOrderUsingPreviousDestinationAsNextOrigin() {
        val start = GeoPoint(45.55, 18.67)
        val hotel = GeoPoint(45.5592, 18.6937)
        val airport = GeoPoint(45.4627, 18.8102)
        val requests = mutableListOf<NavigationRequest>()
        val provider = object : NavigationProvider {
            override suspend fun route(request: NavigationRequest): NavigationRoute {
                requests += request
                return NavigationRoute(1000, 120, "test")
            }
        }
        val coordinator = MultiStopNavigationCoordinator(provider)
        val intent = Intent.Navigate(listOf(
            NavigationStop("Hotel Osijek", hotel),
            NavigationStop("Zracna luka Osijek", airport),
        ))

        val result = runSuspend { coordinator.preparePlan(contextAt(start), intent) }

        assertTrue(result is NavigationPlanResult.Ready)
        assertEquals(listOf(
            NavigationRequest(start, hotel),
            NavigationRequest(hotel, airport),
        ), requests)
    }

    @Test
    fun unresolvedSecondStopPreventsRoutingThatStop() {
        val start = GeoPoint(45.55, 18.67)
        val hotel = GeoPoint(45.5592, 18.6937)
        var calls = 0
        val provider = object : NavigationProvider {
            override suspend fun route(request: NavigationRequest): NavigationRoute {
                calls++
                return NavigationRoute(1000, 120, "test")
            }
        }
        val result = runSuspend {
            MultiStopNavigationCoordinator(provider).preparePlan(
                contextAt(start),
                Intent.Navigate(listOf(
                    NavigationStop("Hotel Osijek", hotel),
                    NavigationStop("Zracna luka Osijek"),
                ))
            )
        }

        assertEquals(NavigationPlanResult.NotReady(1, "Navigation stop has not been resolved to coordinates."), result)
        assertEquals(1, calls)
    }

    private fun contextAt(point: GeoPoint) = LanaContext(
        language = LanguageContext("hr-HR"),
        location = LocationSnapshot(
            reading = LocationReading(
                point = point,
                capturedAtEpochMillis = 1000,
                accuracyMeters = 5.0,
            ),
            assessment = LocationAssessment(LocationStatus.AVAILABLE, "test"),
            enrichment = emptyList(),
        ),
    )

    private fun <T> runSuspend(block: suspend () -> T): T {
        var outcome: Result<T>? = null
        block.startCoroutine(object : Continuation<T> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(result: Result<T>) { outcome = result }
        })
        return outcome?.getOrThrow() ?: fail("Suspend block did not complete synchronously")
    }
}
