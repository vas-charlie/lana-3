package hr.vascharlie.lana3.core.understanding

/**
 * Evidence gate for spoken understanding.
 *
 * This layer deliberately does not invent confidence. Missing recognizer evidence is
 * represented explicitly so downstream intent/reasoning can decide whether clarification
 * is required for the current context.
 */
data class UnderstandingEvidence(
    val transcript: String,
    val transcriptConfidence: Float? = null,
    val detectedLanguageTag: String? = null,
    val languageConfidenceLevel: Int? = null,
)

enum class UnderstandingEvidenceStatus {
    EVIDENCE_AVAILABLE,
    EVIDENCE_INCOMPLETE,
    NO_USABLE_TRANSCRIPT,
}

data class UnderstandingEvidenceAssessment(
    val status: UnderstandingEvidenceStatus,
    val evidence: UnderstandingEvidence,
) {
    val hasMeasuredTranscriptConfidence: Boolean
        get() = evidence.transcriptConfidence != null

    val hasMeasuredLanguageEvidence: Boolean
        get() = evidence.detectedLanguageTag != null
}

object UnderstandingEvidencePolicy {
    fun assess(evidence: UnderstandingEvidence): UnderstandingEvidenceAssessment {
        val normalized = evidence.copy(
            transcript = evidence.transcript.trim(),
            transcriptConfidence = evidence.transcriptConfidence?.takeIf { it in 0f..1f },
            detectedLanguageTag = evidence.detectedLanguageTag?.trim()?.takeIf { it.isNotEmpty() },
        )

        if (normalized.transcript.isEmpty()) {
            return UnderstandingEvidenceAssessment(
                status = UnderstandingEvidenceStatus.NO_USABLE_TRANSCRIPT,
                evidence = normalized,
            )
        }

        val hasTranscriptConfidence = normalized.transcriptConfidence != null
        val hasLanguageEvidence = normalized.detectedLanguageTag != null
        val status = if (hasTranscriptConfidence && hasLanguageEvidence) {
            UnderstandingEvidenceStatus.EVIDENCE_AVAILABLE
        } else {
            UnderstandingEvidenceStatus.EVIDENCE_INCOMPLETE
        }

        return UnderstandingEvidenceAssessment(status = status, evidence = normalized)
    }
}
