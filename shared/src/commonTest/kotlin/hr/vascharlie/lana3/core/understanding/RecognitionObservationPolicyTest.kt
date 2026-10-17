package hr.vascharlie.lana3.core.understanding

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RecognitionObservationPolicyTest {
    @Test
    fun highConfidenceDoesNotClaimMeaningIsVerified() {
        val result = RecognitionObservationPolicy.assess(
            RecognitionObservation(
                transcript = "Lana odvezi mi do tvrde u Osijeku",
                confidence = 0.93f,
            )
        )

        assertEquals(0.93f, result.confidence)
        assertTrue(result.hasMeasuredConfidence)
        assertFalse(result.meaningVerified)
    }

    @Test
    fun exactTranscriptStillIsNotSemanticProofByItself() {
        val result = RecognitionObservationPolicy.assess(
            RecognitionObservation(
                transcript = "Lana odvezi me do Tvrđe u Osijeku",
                confidence = 0.99f,
            )
        )

        assertFalse(result.meaningVerified)
    }

    @Test
    fun missingConfidenceRemainsMissing() {
        val result = RecognitionObservationPolicy.assess(
            RecognitionObservation(
                transcript = "Lana odvezi me do Tvrđe u Osijeku",
            )
        )

        assertNull(result.confidence)
        assertFalse(result.hasMeasuredConfidence)
    }

    @Test
    fun invalidConfidenceIsDiscarded() {
        val result = RecognitionObservationPolicy.assess(
            RecognitionObservation(
                transcript = "tekst",
                confidence = 1.4f,
            )
        )

        assertNull(result.confidence)
        assertFalse(result.hasMeasuredConfidence)
    }
}
