package hr.vascharlie.lana3.core.navigation

import hr.vascharlie.lana3.core.intent.Intent
import hr.vascharlie.lana3.core.intent.Intent.NavigationStop
import hr.vascharlie.lana3.core.location.GeoPoint
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NavigationIntentCoordinatorTest {
    @Test
    fun uniquePlaceProducesResolvedNavigateIntent() {
        val point = GeoPoint(45.5592, 18.6937)
        val coordinator = coordinator(listOf(PlaceCandidate("Hotel Osijek", point)))

        val result = runSuspend { coordinator.resolve(Intent.Navigate("Hotel Osijek")) }

        assertEquals(
            NavigationIntentResolution.Ready(Intent.Navigate("Hotel Osijek", point)),
            result,
        )
    }

    @Test
    fun ambiguousPlaceRequiresChoiceInsteadOfGuessing() {
        val candidates = listOf(
            PlaceCandidate("Park A", GeoPoint(45.1, 18.1)),
            PlaceCandidate("Park B", GeoPoint(45.2, 18.2)),
        )
        val coordinator = coordinator(candidates)

        val result = runSuspend { coordinator.resolve(Intent.Navigate("Park")) }

        assertEquals(
            NavigationIntentResolution.NeedsChoice(0, "Park", candidates),
            result,
        )
    }

    @Test
    fun multipleStopsAreResolvedInSpokenOrder() {
        val hotel = PlaceCandidate("Hotel Osijek", GeoPoint(45.5592, 18.6937))
        val airport = PlaceCandidate("Zracna luka Osijek", GeoPoint(45.4627, 18.8102))
        val provider = object : PlaceResolver {
            override suspend fun resolve(query: String) = when (query) {
                "Hotel Osijek" -> listOf(hotel)
                "Zracna luka Osijek" -> listOf(airport)
                else -> emptyList()
            }
        }
        val coordinator = NavigationIntentCoordinator(DestinationResolver(provider))
        val intent = Intent.Navigate(
            listOf(
                NavigationStop("Hotel Osijek"),
                NavigationStop("Zracna luka Osijek"),
            )
        )

        val result = runSuspend { coordinator.resolve(intent) }

        assertEquals(
            NavigationIntentResolution.Ready(
                Intent.Navigate(
                    listOf(
                        NavigationStop("Hotel Osijek", hotel.point),
                        NavigationStop("Zracna luka Osijek", airport.point),
                    )
                )
            ),
            result,
        )
    }

    @Test
    fun missingPlaceStaysUnresolved() {
        val coordinator = coordinator(emptyList())
        val result = runSuspend { coordinator.resolve(Intent.Navigate("Nepostojece mjesto")) }

        assertEquals(
            NavigationIntentResolution.DestinationNotFound(0, "Nepostojece mjesto"),
            result,
        )
    }

    private fun coordinator(candidates: List<PlaceCandidate>) =
        NavigationIntentCoordinator(
            DestinationResolver(
                object : PlaceResolver {
                    override suspend fun resolve(query: String) = candidates
                }
            )
        )

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
