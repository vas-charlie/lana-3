package hr.vascharlie.lana3.core.navigation

import hr.vascharlie.lana3.core.location.GeoPoint
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DestinationResolverTest {
    @Test
    fun singleCandidateResolvesWithoutGuessing() {
        val hotel = PlaceCandidate("Hotel Osijek", GeoPoint(45.5592, 18.6937), 0.99)
        val result = runSuspend {
            DestinationResolver(FakeResolver(listOf(hotel))).resolve("Hotel Osijek")
        }
        assertEquals(DestinationResolution.Resolved(hotel), result)
    }

    @Test
    fun multipleCandidatesRemainAmbiguous() {
        val candidates = listOf(
            PlaceCandidate("Central Station A", GeoPoint(45.1, 18.1)),
            PlaceCandidate("Central Station B", GeoPoint(45.2, 18.2)),
        )
        val result = runSuspend {
            DestinationResolver(FakeResolver(candidates)).resolve("Central Station")
        }
        assertEquals(DestinationResolution.Ambiguous(candidates), result)
    }

    @Test
    fun noCandidateIsExplicitlyNotFound() {
        val result = runSuspend {
            DestinationResolver(FakeResolver(emptyList())).resolve("Unknown place")
        }
        assertEquals(DestinationResolution.NotFound("Unknown place"), result)
    }

    @Test
    fun blankQueryNeverCallsProvider() {
        val provider = FakeResolver(emptyList())
        val result = runSuspend { DestinationResolver(provider).resolve("   ") }
        assertEquals(DestinationResolution.NotFound("   "), result)
        assertEquals(0, provider.calls)
    }

    private class FakeResolver(
        private val candidates: List<PlaceCandidate>,
    ) : PlaceResolver {
        var calls = 0
        override suspend fun resolve(query: String): List<PlaceCandidate> {
            calls += 1
            return candidates
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
