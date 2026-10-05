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

data class AvatarPresentation(
    val state: LanaVisualState,
    val statusText: String,
    val cue: AvatarCue?,
)

object LanaAvatarPresenter {
    fun present(state: LanaVisualState): AvatarPresentation = when (state) {
        LanaVisualState.IDLE -> AvatarPresentation(state, "Tu sam, Charlie.", null)
        LanaVisualState.LISTENING -> AvatarPresentation(state, "Slusam.", AvatarCue.START_LISTENING)
        LanaVisualState.THINKING -> AvatarPresentation(state, "Razmisljam...", AvatarCue.START_THINKING)
        LanaVisualState.SPEAKING -> AvatarPresentation(state, "Govorim.", AvatarCue.START_SPEAKING)
        LanaVisualState.OFFLINE -> AvatarPresentation(
            state,
            "Offline sam. Dostupne su lokalne sposobnosti.",
            AvatarCue.ENTER_OFFLINE,
        )
        LanaVisualState.ERROR -> AvatarPresentation(
            state,
            "Nesto nije u redu. Necu pogadjati.",
            AvatarCue.SHOW_ERROR,
        )
    }
}
