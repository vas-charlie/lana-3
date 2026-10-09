package hr.vascharlie.lana3.avatar

/**
 * Renderer-neutral facial rig channels. A production 3D renderer maps these
 * controls to actual mesh blend shapes / skeleton bones, not portrait transforms.
 */
enum class AvatarRigChannel {
    EYE_BLINK_LEFT, EYE_BLINK_RIGHT,
    EYE_LOOK_LEFT, EYE_LOOK_RIGHT, EYE_LOOK_UP, EYE_LOOK_DOWN,
    BROW_RAISE_LEFT, BROW_RAISE_RIGHT,
    JAW_OPEN, LIPS_CLOSED, LIPS_WIDE, LIPS_ROUNDED,
    LIPS_LOWER_BITE, TONGUE_UP,
    SMILE_LEFT, SMILE_RIGHT,
    HEAD_YAW, HEAD_PITCH, HEAD_ROLL,
    BREATH
}

/** Normalized channel values; renderer interprets the signed head rotations. */
data class AvatarRigPose(val weights: Map<AvatarRigChannel, Float>) {
    init {
        require(weights.values.all { it.isFinite() && it in -1f..1f }) {
            "Avatar rig weights must be finite and within [-1, 1]"
        }
    }

    fun weight(channel: AvatarRigChannel): Float = weights[channel] ?: 0f
}

/**
 * Maps timed visemes to independent mouth channels. This is a pose mapping,
 * NOT a viseme detector. Call only with a real playback-aligned viseme.
 */
object AvatarVisemeRigMapper {
    fun map(viseme: AvatarViseme, strength: Float = 1f): AvatarRigPose {
        require(strength.isFinite() && strength in 0f..1f)
        val channels = when (viseme) {
            AvatarViseme.SILENCE -> emptyMap()
            AvatarViseme.CLOSED_LIPS -> mapOf(AvatarRigChannel.LIPS_CLOSED to 1f)
            AvatarViseme.LABIODENTAL -> mapOf(AvatarRigChannel.LIPS_LOWER_BITE to 1f)
            AvatarViseme.DENTAL, AvatarViseme.ALVEOLAR ->
                mapOf(AvatarRigChannel.JAW_OPEN to .25f, AvatarRigChannel.TONGUE_UP to .8f)
            AvatarViseme.POSTALVEOLAR ->
                mapOf(AvatarRigChannel.JAW_OPEN to .3f, AvatarRigChannel.LIPS_ROUNDED to .55f)
            AvatarViseme.VELAR -> mapOf(AvatarRigChannel.JAW_OPEN to .45f)
            AvatarViseme.OPEN_VOWEL -> mapOf(AvatarRigChannel.JAW_OPEN to 1f)
            AvatarViseme.MID_VOWEL ->
                mapOf(AvatarRigChannel.JAW_OPEN to .55f, AvatarRigChannel.LIPS_WIDE to .3f)
            AvatarViseme.ROUNDED_VOWEL ->
                mapOf(AvatarRigChannel.JAW_OPEN to .35f, AvatarRigChannel.LIPS_ROUNDED to 1f)
        }
        return AvatarRigPose(channels.mapValues { (_, value) -> value * strength })
    }
}
