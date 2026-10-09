package hr.vascharlie.lana3.avatar

/**
 * Renderer-neutral source of playback-aligned mouth shapes.
 *
 * Implementations may use a TTS engine that returns phoneme/viseme timestamps
 * or a validated audio-driven solver. Android TTS text-range callbacks are not
 * sufficient to implement this contract by themselves.
 */
interface AvatarVisemeSource {
    val isAvailable: Boolean

    fun prepare(
        text: String,
        languageTag: String? = null,
        onResult: (AvatarVisemeResult) -> Unit,
    )
}

sealed interface AvatarVisemeResult {
    data class Ready(
        val timeline: AvatarVisemeTimeline,
    ) : AvatarVisemeResult

    data class Unavailable(
        val reason: String,
    ) : AvatarVisemeResult

    data class Error(
        val code: String,
        val message: String,
    ) : AvatarVisemeResult
}
