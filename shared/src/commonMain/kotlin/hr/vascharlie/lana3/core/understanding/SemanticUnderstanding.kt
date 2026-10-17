package hr.vascharlie.lana3.core.understanding

/**
 * Semantic understanding is kept separate from the raw transcript.
 * A recognizer may get filler/grammar wrong while the user's intent and a key slot
 * remain recoverable. Extracted slots are candidates, not verified real-world entities.
 */
enum class UnderstoodIntentKind {
    NAVIGATE,
    UNKNOWN,
}

data class UnderstoodSlot(
    val name: String,
    val value: String,
    val verified: Boolean = false,
)

data class SemanticUnderstanding(
    val intent: UnderstoodIntentKind,
    val slots: List<UnderstoodSlot> = emptyList(),
    val sourceTranscript: String,
)

object CroatianNavigationUnderstanding {
    private val navigationPrefixes = listOf(
        "lana odvezi me do ",
        "lana odvezi mi do ",
        "odvezi me do ",
        "odvezi mi do ",
    )

    /**
     * Narrow deterministic baseline for the physically tested Croatian navigation shape.
     * This does not correct spelling or resolve a place. It only separates the navigation
     * intent from the destination text actually present in the transcript.
     */
    fun understand(transcript: String): SemanticUnderstanding {
        val normalized = transcript.trim()
        val lower = normalized.lowercase()

        val prefix = navigationPrefixes.firstOrNull { lower.startsWith(it) }
            ?: return SemanticUnderstanding(
                intent = UnderstoodIntentKind.UNKNOWN,
                sourceTranscript = normalized,
            )

        val destination = normalized
            .substring(prefix.length)
            .trim()
            .takeIf { it.isNotEmpty() }
            ?: return SemanticUnderstanding(
                intent = UnderstoodIntentKind.UNKNOWN,
                sourceTranscript = normalized,
            )

        return SemanticUnderstanding(
            intent = UnderstoodIntentKind.NAVIGATE,
            slots = listOf(
                UnderstoodSlot(
                    name = "destination",
                    value = destination,
                    verified = false,
                )
            ),
            sourceTranscript = normalized,
        )
    }
}
