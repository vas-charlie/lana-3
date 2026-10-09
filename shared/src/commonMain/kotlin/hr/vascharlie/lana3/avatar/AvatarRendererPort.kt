package hr.vascharlie.lana3.avatar

/**
 * Boundary between LANA conversation state and the production avatar renderer.
 *
 * The Android host owns lifecycle and supplies semantic frames plus independent
 * rig poses. A Godot/other 3D implementation may sit behind this interface
 * without leaking engine types into shared business logic.
 */
interface AvatarRendererPort {
    val isAvailable: Boolean

    fun attach()
    fun renderFrame(frame: AvatarFrame)
    fun renderRigPose(pose: AvatarRigPose)
    fun detach()
}

/**
 * Safe fallback used when no production 3D renderer is packaged.
 * It deliberately does nothing instead of pretending that a flat image is a
 * working avatar.
 */
object UnavailableAvatarRenderer : AvatarRendererPort {
    override val isAvailable: Boolean = false
    override fun attach() = Unit
    override fun renderFrame(frame: AvatarFrame) = Unit
    override fun renderRigPose(pose: AvatarRigPose) = Unit
    override fun detach() = Unit
}
