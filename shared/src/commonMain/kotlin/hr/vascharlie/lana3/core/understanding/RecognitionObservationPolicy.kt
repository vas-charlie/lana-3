package hr.vascharlie.lana3.core.understanding

/**
 * Recognition confidence is evidence about the recognizer output, not proof that
 * every word or the user's intended meaning is correct.
 */
data class RecognitionObservation(
    val transcript: String,
    val confidence: Float? = null,
)

data class RecognitionObservationAssessment(
    val transcript: String,
    val confidence: Float?,
    val hasMeasuredConfidence: Boolean,
    val meaningVerified: Boolean = false,
)

object RecognitionObservationPolicy {
    fun assess(observation: RecognitionObservation): RecognitionObservationAssessment {
        val transcript = observation.transcript.trim()
        val confidence = observation.confidence?.takeIf { it in 0f..1f }

        return RecognitionObservationAssessment(
            transcript = transcript,
            confidence = confidence,
            hasMeasuredConfidence = confidence != null,
            // STT confidence alone can never verify semantic correctness.
            meaningVerified = false,
        )
    }
}
