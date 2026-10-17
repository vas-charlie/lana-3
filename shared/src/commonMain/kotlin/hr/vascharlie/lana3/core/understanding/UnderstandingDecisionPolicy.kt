package hr.vascharlie.lana3.core.understanding

/**
 * Context supplied by the caller. The understanding layer does not guess how risky
 * an action is; the feature asking to act must say whether ambiguity is acceptable.
 */
enum class UnderstandingRisk {
    CONVERSATION,
    CONSEQUENTIAL_ACTION,
}

sealed interface UnderstandingDecision {
    data class Proceed(
        val transcript: String,
        val evidenceStatus: UnderstandingEvidenceStatus,
    ) : UnderstandingDecision

    data class Clarify(
        val reason: ClarificationReason,
    ) : UnderstandingDecision
}

enum class ClarificationReason {
    NO_USABLE_TRANSCRIPT,
    MISSING_RECOGNITION_EVIDENCE,
}

/**
 * Decides only from evidence we actually possess.
 *
 * There is deliberately no numeric confidence threshold here yet. A threshold would
 * be an invented product rule until physical speech tests give us evidence for one.
 */
object UnderstandingDecisionPolicy {
    fun decide(
        assessment: UnderstandingEvidenceAssessment,
        risk: UnderstandingRisk,
    ): UnderstandingDecision {
        if (assessment.status == UnderstandingEvidenceStatus.NO_USABLE_TRANSCRIPT) {
            return UnderstandingDecision.Clarify(
                ClarificationReason.NO_USABLE_TRANSCRIPT
            )
        }

        if (
            risk == UnderstandingRisk.CONSEQUENTIAL_ACTION &&
            assessment.status == UnderstandingEvidenceStatus.EVIDENCE_INCOMPLETE
        ) {
            return UnderstandingDecision.Clarify(
                ClarificationReason.MISSING_RECOGNITION_EVIDENCE
            )
        }

        return UnderstandingDecision.Proceed(
            transcript = assessment.evidence.transcript,
            evidenceStatus = assessment.status,
        )
    }
}
