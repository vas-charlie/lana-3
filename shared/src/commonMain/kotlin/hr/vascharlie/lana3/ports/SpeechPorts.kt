package hr.vascharlie.lana3.ports

data class SpeechInputRequest(
    val languageTag: String? = null,
    val enablePartialResults: Boolean = true,
    /**
     * Requests language detection when the platform supports it. This is a
     * capability request, not a promise that every recognizer can detect every
     * language. Callers must still handle a missing detection result.
     */
    val enableLanguageDetection: Boolean = false,
)

sealed interface SpeechInputEvent {
    data object ListeningStarted : SpeechInputEvent
    data object SpeechEnded : SpeechInputEvent

    data class PartialTranscript(
        val text: String,
        val languageTag: String? = null,
    ) : SpeechInputEvent

    data class FinalTranscript(
        val text: String,
        val languageTag: String? = null,
        /**
         * Recognition confidence reported by the speech engine for this exact
         * transcript, normalized by Android to 0.0..1.0. Null means the engine
         * did not provide usable confidence evidence. LANA must not invent it.
         */
        val confidence: Float? = null,
    ) : SpeechInputEvent

    /**
     * Emitted only when the speech engine explicitly reports a detected
     * language. LANA must not infer or invent a language tag when this event is
     * absent.
     */
    data class LanguageDetected(
        val languageTag: String,
        val confidenceLevel: Int? = null,
    ) : SpeechInputEvent

    data class Error(
        val code: String,
        val message: String,
        val retryable: Boolean,
    ) : SpeechInputEvent
}

interface SpeechInputPort {
    val isAvailable: Boolean

    fun start(
        request: SpeechInputRequest,
        onEvent: (SpeechInputEvent) -> Unit,
    )

    fun stop()

    fun cancel()
}

data class SpeechOutputRequest(
    val text: String,
    val languageTag: String? = null,
)

sealed interface SpeechOutputEvent {
    data object Started : SpeechOutputEvent
    data object Completed : SpeechOutputEvent

    /**
     * Timing cue emitted by speech engines that expose progress inside the
     * utterance. This is not audio amplitude or a viseme. It gives the avatar
     * a truthful text-range timing signal without pretending to have phoneme
     * data that the engine did not provide.
     */
    data class RangeStarted(
        val start: Int,
        val end: Int,
    ) : SpeechOutputEvent

    data class Error(
        val code: String,
        val message: String,
    ) : SpeechOutputEvent
}

interface SpeechOutputPort {
    val isAvailable: Boolean

    fun speak(
        request: SpeechOutputRequest,
        onEvent: (SpeechOutputEvent) -> Unit,
    )

    fun stop()
}
