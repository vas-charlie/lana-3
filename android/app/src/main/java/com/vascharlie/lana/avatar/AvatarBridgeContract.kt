package com.vascharlie.lana.avatar

/**
 * Host-side contract for driving the embedded Godot avatar.
 *
 * This is deliberately renderer-neutral: Android owns app lifecycle,
 * permissions and speech orchestration; the avatar consumes semantic state.
 */
interface AvatarBridgeContract {
    fun setConversationState(state: ConversationState)
    fun setSpeechActivity(active: Boolean)
    fun setViseme(id: String, intensity: Float)
    fun setFacialControl(id: String, value: Float)
    fun setLocale(languageTag: String)
    fun pause()
    fun resume()
    fun close()
}

enum class ConversationState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

sealed interface AvatarBridgeEvent {
    data object RendererReady : AvatarBridgeEvent
    data object ModelLoaded : AvatarBridgeEvent
    data class ModelRejected(val reason: ModelRejectionReason) : AvatarBridgeEvent
    data class RuntimeError(val code: String) : AvatarBridgeEvent
}

enum class ModelRejectionReason {
    MISSING_REQUIRED_FACIAL_CONTROLS,
    INVALID_ASSET,
    UNSUPPORTED_MODEL
}
