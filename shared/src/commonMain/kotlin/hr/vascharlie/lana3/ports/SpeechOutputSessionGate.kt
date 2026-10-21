package hr.vascharlie.lana3.ports

/**
 * Routes TTS callbacks only to the utterance that created them.
 *
 * Some speech engines deliver completion or error callbacks after a queued
 * utterance is replaced, stopped or released. Without an utterance-id guard,
 * those stale events can accidentally complete a newer speech request.
 *
 * The owner must call [begin], [dispatch] and [cancel] on a single dispatcher
 * (Android's main thread in the current adapter). This class intentionally
 * does not know about Android, voices or audio providers.
 */
class SpeechOutputSessionGate {
    private data class Session(
        val utteranceId: String,
        val callback: (SpeechOutputEvent) -> Unit,
    )

    private var active: Session? = null

    /**
     * Replaces the previous active session. The previous callback will no
     * longer receive events, even if its TTS engine emits them later.
     */
    fun begin(utteranceId: String, callback: (SpeechOutputEvent) -> Unit) {
        require(utteranceId.isNotBlank()) { "Utterance ID must not be blank." }
        active = Session(utteranceId, callback)
    }

    /** Invalidates all callbacks for the current utterance. */
    fun cancel() {
        active = null
    }

    /**
     * Returns true only if this event belongs to the active utterance.
     * A completion or error terminates the session exactly once.
     *
     * Null and unknown IDs are deliberately ignored, not guessed.
     */
    fun dispatch(utteranceId: String?, event: SpeechOutputEvent): Boolean {
        val session = active ?: return false
        if (utteranceId != session.utteranceId) return false

        if (event == SpeechOutputEvent.Completed || event is SpeechOutputEvent.Error) {
            active = null
        }
        session.callback(event)
        return true
    }
}
