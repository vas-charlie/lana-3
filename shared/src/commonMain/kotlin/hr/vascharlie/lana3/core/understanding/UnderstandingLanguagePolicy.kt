package hr.vascharlie.lana3.core.understanding

/**
 * Keeps requested and detected language separate. A requested language is configuration,
 * not evidence that the user actually spoke that language.
 */
data class UnderstandingLanguageEvidence(
    val requestedLanguageTag: String? = null,
    val detectedLanguageTag: String? = null,
)

enum class UnderstandingLanguageStatus {
    DETECTED,
    NOT_DETECTED,
    REQUEST_DIFFERS_FROM_DETECTION,
}

data class UnderstandingLanguageAssessment(
    val status: UnderstandingLanguageStatus,
    val requestedLanguageTag: String?,
    val detectedLanguageTag: String?,
)

object UnderstandingLanguagePolicy {
    fun assess(evidence: UnderstandingLanguageEvidence): UnderstandingLanguageAssessment {
        val requested = normalize(evidence.requestedLanguageTag)
        val detected = normalize(evidence.detectedLanguageTag)

        val status = when {
            detected == null -> UnderstandingLanguageStatus.NOT_DETECTED
            requested != null && !sameLanguage(requested, detected) ->
                UnderstandingLanguageStatus.REQUEST_DIFFERS_FROM_DETECTION
            else -> UnderstandingLanguageStatus.DETECTED
        }

        return UnderstandingLanguageAssessment(
            status = status,
            requestedLanguageTag = requested,
            detectedLanguageTag = detected,
        )
    }

    private fun normalize(tag: String?): String? =
        tag?.trim()?.takeIf { it.isNotEmpty() }

    private fun sameLanguage(first: String, second: String): Boolean =
        primaryLanguage(first).equals(primaryLanguage(second), ignoreCase = true)

    private fun primaryLanguage(tag: String): String =
        tag.substringBefore('-').substringBefore('_')
}
