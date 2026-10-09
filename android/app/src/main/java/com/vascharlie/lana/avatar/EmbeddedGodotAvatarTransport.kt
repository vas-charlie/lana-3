package com.vascharlie.lana.avatar

/**
 * Concrete transport boundary for the embedded Godot avatar.
 *
 * Godot-specific embedding code supplies [messageSink] and [closeSink].
 * Keeping those details outside this class prevents renderer integration from
 * coupling itself to Android app lifecycle, signing or updater code.
 */
class EmbeddedGodotAvatarTransport(
    private val messageSink: (type: String, payload: Any) -> Unit,
    private val closeSink: () -> Unit
) : GodotAvatarTransport {

    private var closed = false

    override fun send(type: String, payload: Any) {
        if (closed) return
        require(type.isNotBlank()) { "Avatar message type must not be blank" }
        messageSink(type, payload)
    }

    override fun close() {
        if (closed) return
        closed = true
        closeSink()
    }
}
