package hr.vascharlie.lana3.core.search

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class UniversalSearchTest {
    private class FakeProvider(
        override val domain: SearchDomain,
        private val hits: List<SearchHit>,
    ) : SearchProvider {
        var receivedQuery: String? = null

        override fun search(query: String): List<SearchHit> {
            receivedQuery = query
            return hits
        }
    }

    @Test
    fun blankQueryIsRejectedWithoutCallingProviders() {
        val notes = FakeProvider(SearchDomain.NOTES, emptyList())
        val result = UniversalSearch(listOf(notes)).search("   ")

        assertEquals(
            SearchInvalidReason.QUERY_BLANK,
            assertIs<UniversalSearchResult.Invalid>(result).reason,
        )
        assertEquals(null, notes.receivedQuery)
    }

    @Test
    fun sameQueryIsSentToEveryRegisteredDomain() {
        val notes = FakeProvider(
            SearchDomain.NOTES,
            listOf(SearchHit(SearchDomain.NOTES, "n1", "Aerodrom")),
        )
        val rides = FakeProvider(
            SearchDomain.RIDES,
            listOf(SearchHit(SearchDomain.RIDES, "r1", "Vožnja Aerodrom")),
        )

        val result = assertIs<UniversalSearchResult.Found>(
            UniversalSearch(listOf(notes, rides)).search("Aerodrom")
        )

        assertEquals("Aerodrom", notes.receivedQuery)
        assertEquals("Aerodrom", rides.receivedQuery)
        assertEquals(setOf(SearchDomain.NOTES, SearchDomain.RIDES), result.hits.map { it.domain }.toSet())
    }

    @Test
    fun orderingIsDeterministicAcrossProviders() {
        val notes = FakeProvider(
            SearchDomain.NOTES,
            listOf(SearchHit(SearchDomain.NOTES, "n2", "Older", updatedAtEpochMillis = 100)),
        )
        val documents = FakeProvider(
            SearchDomain.DOCUMENTS,
            listOf(
                SearchHit(SearchDomain.DOCUMENTS, "d2", "Newest B", updatedAtEpochMillis = 300),
                SearchHit(SearchDomain.DOCUMENTS, "d1", "Newest A", updatedAtEpochMillis = 300),
            ),
        )

        val hits = assertIs<UniversalSearchResult.Found>(
            UniversalSearch(listOf(notes, documents)).search("x")
        ).hits

        assertEquals(listOf("d1", "d2", "n2"), hits.map { it.id })
    }

    @Test
    fun missingDomainProviderProducesNoInventedResults() {
        val result = assertIs<UniversalSearchResult.Found>(
            UniversalSearch(emptyList()).search("Charlie")
        )

        assertEquals(emptyList(), result.hits)
    }

    @Test
    fun duplicateDomainOwnershipIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            UniversalSearch(
                listOf(
                    FakeProvider(SearchDomain.NOTES, emptyList()),
                    FakeProvider(SearchDomain.NOTES, emptyList()),
                )
            )
        }
    }
}
