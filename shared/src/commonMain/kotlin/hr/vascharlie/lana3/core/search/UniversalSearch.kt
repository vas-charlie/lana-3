package hr.vascharlie.lana3.core.search

enum class SearchDomain {
    NOTES,
    JOURNAL,
    CLIENTS,
    RIDES,
    BUSINESS,
    DOCUMENTS,
}

data class SearchHit(
    val domain: SearchDomain,
    val id: String,
    val title: String,
    val preview: String? = null,
    val updatedAtEpochMillis: Long? = null,
)

interface SearchProvider {
    val domain: SearchDomain

    fun search(query: String): List<SearchHit>
}

enum class SearchInvalidReason {
    QUERY_BLANK,
}

sealed interface UniversalSearchResult {
    data class Found(val hits: List<SearchHit>) : UniversalSearchResult
    data class Invalid(val reason: SearchInvalidReason) : UniversalSearchResult
}

/**
 * Platform-neutral search aggregator.
 *
 * Providers own domain-specific lookup. This layer only validates the query,
 * combines providers and applies a deterministic ordering. It does not invent
 * results for domains that do not yet have a provider.
 */
class UniversalSearch(
    providers: List<SearchProvider>,
) {
    private val providersByDomain: Map<SearchDomain, SearchProvider>

    init {
        val duplicates = providers
            .groupBy { it.domain }
            .filterValues { it.size > 1 }
            .keys
        require(duplicates.isEmpty()) {
            "Only one search provider may own a domain: " +
                duplicates.sortedBy { it.name }.joinToString()
        }
        providersByDomain = providers.associateBy { it.domain }
    }

    fun search(query: String): UniversalSearchResult {
        if (query.isBlank()) {
            return UniversalSearchResult.Invalid(SearchInvalidReason.QUERY_BLANK)
        }

        val hits = SearchDomain.entries
            .mapNotNull(providersByDomain::get)
            .flatMap { provider -> provider.search(query) }
            .sortedWith(
                compareByDescending<SearchHit> { it.updatedAtEpochMillis ?: Long.MIN_VALUE }
                    .thenBy { it.domain.ordinal }
                    .thenBy { it.id }
            )

        return UniversalSearchResult.Found(hits)
    }
}
