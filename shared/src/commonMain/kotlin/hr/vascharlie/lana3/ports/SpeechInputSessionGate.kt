package hr.vascharlie.lana3.ports

/**
 * Platform-independent STT session boundary.
 *
 * Android recognizer listeners must capture the token belonging to their own
 * recognizer instance, rather than reading the currently active token.
 * This class does not itself connect to AndroidSpeechInputAdapter.
 */
class SpeechInputSessionGate {
    private var generation = 0L
    private var activeGeneration: Long? = null
    private var callback: ((SpeechInputEvent) -> Unit)? = null

    fun begin(onEvent: (SpeechInputEvent) -> Unit): Long {
        generation += 1
        activeGeneration = generation
        callback = onEvent
        return generation
    }

    fun cancel() {
        activeGeneration = null
        callback = null
    }

    fun dispatch(token: Long, event: SpeechInputEvent): Boolean {
        if (activeGeneration != token) return false
        val receiver = callback ?: return false
        receiver(event)
        if (event is SpeechInputEvent.FinalTranscript || event is SpeechInputEvent.Error) {
            cancel()
        }
        return true
    }
}
