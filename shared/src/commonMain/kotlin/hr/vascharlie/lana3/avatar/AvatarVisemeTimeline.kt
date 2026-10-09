package hr.vascharlie.lana3.avatar

/**
 * Renderer-independent facial rig contract. Channel names refer to actual
 * independent bones or morph targets in a future rigged 3D model.
 * No renderer, TTS vendor, or specific model format is required here.
 */
enum class AvatarViseme {
    SILENCE, CLOSED_LIPS, LABIODENTAL, DENTAL, ALVEOLAR,
    POSTALVEOLAR, VELAR, OPEN_VOWEL, MID_VOWEL, ROUNDED_VOWEL,
}

/** Relative position in the played audio, not time since synthesis started. */
data class TimedViseme(
    val startMs: Long,
    val endMs: Long,
    val shape: AvatarViseme,
    val weight: Float = 1f,
) {
    init {
        require(startMs >= 0L)
        require(endMs > startMs)
        require(weight in 0f..1f)
    }
}

/**
 * Validated playback timeline. It does not infer visemes from character count
 * or from Android TextToSpeech range callbacks.
 */
class AvatarVisemeTimeline(events: List<TimedViseme>) {
    val events: List<TimedViseme> = events.toList()

    init {
        var previousEnd = 0L
        this.events.forEach {
            require(it.startMs >= previousEnd) { "Visemes must be ordered and non-overlapping" }
            previousEnd = it.endMs
        }
    }

    fun atPlaybackPosition(positionMs: Long): TimedViseme? {
        if (positionMs < 0) return null
        return events.firstOrNull { positionMs >= it.startMs && positionMs < it.endMs }
    }
}
