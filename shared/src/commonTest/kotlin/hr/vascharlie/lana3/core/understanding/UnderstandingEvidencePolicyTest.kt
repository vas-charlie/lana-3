package hr.vascharlie.lana3.core.understanding

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UnderstandingEvidencePolicyTest {
    @Test
    fun missingConfidenceIsNotInvented() {
        val result = UnderstandingEvidencePolicy.assess(
            UnderstandingEvidence(
                transcript = "Odvezi me na kolodvor",
                detectedLanguageTag = "hr-HR",
            )
        )

        assertEquals(UnderstandingEvidenceStatus.EVIDENCE_INCOMPLETE, result.status)
        assertNull(result.evidence.transcriptConfidence)
        assertFalse(result.hasMeasuredTranscriptConfidence)
        assertTrue(result.hasMeasuredLanguageEvidence)
    }

    @Test
    fun completeRecognizerEvidenceIsPreserved() {
        val result = UnderstandingEvidencePolicy.assess(
            UnderstandingEvidence(
                transcript = " Take me to the airport ",
                transcriptConfidence = 0.82f,
                detectedLanguageTag = " en-US ",
                languageConfidenceLevel = 2,
            )
        )

        assertEquals(UnderstandingEvidenceStatus.EVIDENCE_AVAILABLE, result.status)
        assertEquals("Take me to the airport", result.evidence.transcript)
        assertEquals(0.82f, result.evidence.transcriptConfidence)
        assertEquals("en-US", result.evidence.detectedLanguageTag)
        assertEquals(2, result.evidence.languageConfidenceLevel)
    }

    @Test
    fun blankTranscriptCannotBeTreatedAsUnderstood() {
        val result = UnderstandingEvidencePolicy.assess(
            UnderstandingEvidence(
                transcript = "   ",
                transcriptConfidence = 0.95f,
                detectedLanguageTag = "hr-HR",
            )
        )

        assertEquals(UnderstandingEvidenceStatus.NO_USABLE_TRANSCRIPT, result.status)
    }

    @Test
    fun invalidConfidenceIsDiscardedRatherThanClampedOrGuessed() {
        val result = UnderstandingEvidencePolicy.assess(
            UnderstandingEvidence(
                transcript = "Osijek",
                transcriptConfidence = 1.4f,
                detectedLanguageTag = "hr-HR",
            )
        )

        assertEquals(UnderstandingEvidenceStatus.EVIDENCE_INCOMPLETE, result.status)
        assertNull(result.evidence.transcriptConfidence)
    }
}
