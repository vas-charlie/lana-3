package hr.vascharlie.lana3.avatar

/**
 * Truthful state machine for Lana's real avatar.
 *
 * Charlie/user speech puts Lana in LISTENING. Only Lana's own synthesized
 * speech can drive SPEAKING and the mouth/lip-sync channel.
 */
class AvatarController {
    var frame: AvatarFrame = AvatarFrame(AvatarMode.IDLE, AvatarAttention.NEUTRAL)
        private set

    fun onUserSpeechStarted() {
        frame = AvatarFrame(AvatarMode.LISTENING, AvatarAttention.USER)
    }

    fun onUserSpeechEnded() {
        frame = AvatarFrame(AvatarMode.THINKING, AvatarAttention.TASK)
    }

    fun onLanaSpeechStarted() {
        frame = AvatarFrame(AvatarMode.SPEAKING, AvatarAttention.USER)
    }

    fun onLanaSpeechLevel(level: Float) {
        if (frame.mode != AvatarMode.SPEAKING) return
        frame = frame.copy(speechLevel = level.coerceIn(0f, 1f))
    }

    fun onLanaSpeechEnded() {
        frame = AvatarFrame(AvatarMode.IDLE, AvatarAttention.USER)
    }

    fun onOffline() {
        frame = AvatarFrame(AvatarMode.OFFLINE, AvatarAttention.NEUTRAL)
    }

    fun onError() {
        frame = AvatarFrame(AvatarMode.ERROR, AvatarAttention.NEUTRAL)
    }
}
