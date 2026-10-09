package hr.vascharlie.lana3.avatar

/**
 * Platform-neutral avatar state. Renderers on Android, iOS, web and future
 * devices consume this model instead of owning conversation semantics.
 */
enum class AvatarMode { IDLE, LISTENING, THINKING, SPEAKING, OFFLINE, ERROR }

enum class AvatarAttention { USER, TASK, SCREEN, NEUTRAL }

data class AvatarFrame(
    val mode: AvatarMode,
    val attention: AvatarAttention,
    val speechLevel: Float = 0f,
    val isBlinkAllowed: Boolean = true,
) {
    init {
        require(speechLevel in 0f..1f)
        require(mode == AvatarMode.SPEAKING || speechLevel == 0f) {
            "Mouth activity is allowed only while Lana is speaking"
        }
    }
}
