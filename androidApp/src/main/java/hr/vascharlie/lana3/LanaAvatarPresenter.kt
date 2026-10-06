package hr.vascharlie.lana3

enum class AvatarCue {
    START_LISTENING,
    START_THINKING,
    START_SPEAKING,
    STOP_SPEAKING,
    ENTER_OFFLINE,
    SHOW_ERROR,
    WARM_SMILE,
    SMALL_NOD,
    ADJUST_GLASSES,
}

/**
 * Presentation-neutral semantic output for a future avatar renderer.
 *
 * User-facing text belongs to the Android resource layer. The avatar contract
 * carries state and semantic animation cues only.
 */
data class AvatarPresentation(
    val state: LanaVisualState,
    val cue: AvatarCue?,
)

object LanaAvatarPresenter {
    fun present(state: LanaVisualState): AvatarPresentation = when (state) {
        LanaVisualState.IDLE ->
            AvatarPresentation(state, null)

        LanaVisualState.LISTENING ->
            AvatarPresentation(state, AvatarCue.START_LISTENING)

        LanaVisualState.THINKING ->
            AvatarPresentation(state, AvatarCue.START_THINKING)

        LanaVisualState.SPEAKING ->
            AvatarPresentation(state, AvatarCue.START_SPEAKING)

        LanaVisualState.OFFLINE ->
            AvatarPresentation(state, AvatarCue.ENTER_OFFLINE)

        LanaVisualState.ERROR ->
            AvatarPresentation(state, AvatarCue.SHOW_ERROR)
    }
}
