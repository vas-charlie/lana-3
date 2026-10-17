package hr.vascharlie.lana3.core.understanding

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class UnderstandingDecisionPolicyTest {
    @Test
    fun blankTranscriptAlwaysRequiresClarification() {
        val assessment = UnderstandingEvidencePolicy.assess(
            UnderstandingEvidence(transcript = "   ")
        )

        val decision = UnderstandingDecisionPolicy.decide(
            assessment = assessment,
            risk = UnderstandingRisk.CONVERSATION,
        )

        val clarify = assertIs<UnderstandingDecision.Clarify>(decision)
        assertEquals(ClarificationReason.NO_USABLE_TRANSCRIPT, clarify.reason)
    }

    @Test
    fun consequentialActionDoesNotProceedWhenRecognizerEvidenceIsMissing() {
        val assessment = UnderstandingEvidencePolicy.assess(
            UnderstandingEvidence(
                transcript = "Pošalji poruku Ivanu",
                detectedLanguageTag = "hr-HR",
            )
        )

        val decision = UnderstandingDecisionPolicy.decide(
            assessment = assessment,
            risk = UnderstandingRisk.CONSEQUENTIAL_ACTION,
        )

        val clarify = assertIs<UnderstandingDecision.Clarify>(decision)
        assertEquals(ClarificationReason.MISSING_RECOGNITION_EVIDENCE, clarify.reason)
    }

    @Test
    fun ordinaryConversationCanContinueWithoutInventingMissingConfidence() {
        val assessment = UnderstandingEvidencePolicy.assess(
            UnderstandingEvidence(
                transcript = "Kakvo je vrijeme?",
                detectedLanguageTag = "hr-HR",
            )
        )

        val decision = UnderstandingDecisionPolicy.decide(
            assessment = assessment,
            risk = UnderstandingRisk.CONVERSATION,
        )

        val proceed = assertIs<UnderstandingDecision.Proceed>(decision)
        assertEquals("Kakvo je vrijeme?", proceed.transcript)
        assertEquals(UnderstandingEvidenceStatus.EVIDENCE_INCOMPLETE, proceed.evidenceStatus)
    }

    @Test
    fun completeEvidenceCanProceedForConsequentialAction() {
        val assessment = UnderstandingEvidencePolicy.assess(
            UnderstandingEvidence(
                transcript = "Nazovi Ivana",
                transcriptConfidence = 0.91f,
                detectedLanguageTag = "hr-HR",
                languageConfidenceLevel = 3,
            )
        )

        val decision = UnderstandingDecisionPolicy.decide(
            assessment = assessment,
            risk = UnderstandingRisk.CONSEQUENTIAL_ACTION,
        )

        val proceed = assertIs<UnderstandingDecision.Proceed>(decision)
        assertEquals("Nazovi Ivana", proceed.transcript)
        assertEquals(UnderstandingEvidenceStatus.EVIDENCE_AVAILABLE, proceed.evidenceStatus)
    }
}
