package hr.vascharlie.lana3.avatar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AvatarRigPoseTest {
    @Test
    fun mouthShapesAreIndependent() {
        val closed = AvatarVisemeRigMapper.map(AvatarViseme.CLOSED_LIPS)
        assertEquals(1f, closed.weight(AvatarRigChannel.LIPS_CLOSED))
        assertEquals(0f, closed.weight(AvatarRigChannel.JAW_OPEN))

        val rounded = AvatarVisemeRigMapper.map(AvatarViseme.ROUNDED_VOWEL, .5f)
        assertEquals(.5f, rounded.weight(AvatarRigChannel.LIPS_ROUNDED))
        assertEquals(.175f, rounded.weight(AvatarRigChannel.JAW_OPEN))
        assertEquals(0f, rounded.weight(AvatarRigChannel.EYE_BLINK_LEFT))
    }

    @Test
    fun silenceDoesNotInventSpeechMovement() {
        assertEquals(emptyMap(), AvatarVisemeRigMapper.map(AvatarViseme.SILENCE).weights)
    }

    @Test
    fun rejectsInvalidWeights() {
        assertFailsWith<IllegalArgumentException> {
            AvatarRigPose(mapOf(AvatarRigChannel.JAW_OPEN to Float.NaN))
        }
        assertFailsWith<IllegalArgumentException> {
            AvatarVisemeRigMapper.map(AvatarViseme.OPEN_VOWEL, 1.5f)
        }
    }
}
