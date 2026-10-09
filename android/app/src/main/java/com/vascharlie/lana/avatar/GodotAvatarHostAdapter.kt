package com.vascharlie.lana.avatar

/**
 * Android-side adapter between LANA orchestration and an embedded Godot renderer.
 *
 * The transport is intentionally abstract so production Android lifecycle,
 * signing and updater code remain independent from the renderer implementation.
 */
class GodotAvatarHostAdapter(
    private val transport: GodotAvatarTransport
) : AvatarBridgeContract {

    override fun setConversationState(state: ConversationState) =
        transport.send("conversation_state", state.name.lowercase())

    override fun setSpeechActivity(active: Boolean) =
        transport.send("speech_activity", active)

    override fun setViseme(id: String, intensity: Float) =
        transport.send("viseme", mapOf("id" to id, "intensity" to intensity.coerceIn(0f, 1f)))

    override fun setFacialControl(id: String, value: Float) =
        transport.send("facial_control", mapOf("id" to id, "value" to value.coerceIn(0f, 1f)))

    override fun setLocale(languageTag: String) =
        transport.send("locale", languageTag)

    override fun pause() = transport.send("lifecycle", "paused")
    override fun resume() = transport.send("lifecycle", "resumed")
    override fun close() = transport.close()
}

/**
 * Small boundary that the concrete embedded-Godot integration will implement.
 */
interface GodotAvatarTransport {
    fun send(type: String, payload: Any)
    fun close()
}
