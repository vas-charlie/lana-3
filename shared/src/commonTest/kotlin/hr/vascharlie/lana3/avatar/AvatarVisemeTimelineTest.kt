package hr.vascharlie.lana3.avatar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class AvatarVisemeTimelineTest {
    @Test
    fun selectsVisemeUsingPlaybackTime() {
        val timeline = AvatarVisemeTimeline(
            listOf(
                TimedViseme(0, 90, AvatarViseme.CLOSED_LIPS),
                TimedViseme(90, 210, AvatarViseme.OPEN_VOWEL),
            )
        )
        assertEquals(AvatarViseme.CLOSED_LIPS, timeline.atPlaybackPosition(89)?.shape)
        assertEquals(AvatarViseme.OPEN_VOWEL, timeline.atPlaybackPosition(90)?.shape)
        assertNull(timeline.atPlaybackPosition(210))
        assertNull(timeline.atPlaybackPosition(-1))
    }

    @Test
    fun rejectsOverlappingAndInvalidEvents() {
        assertFailsWith<IllegalArgumentException> {
            AvatarVisemeTimeline(
                listOf(
                    TimedViseme(0, 100, AvatarViseme.OPEN_VOWEL),
                    TimedViseme(90, 120, AvatarViseme.ROUNDED_VOWEL),
                )
            )
        }
        assertFailsWith<IllegalArgumentException> {
            TimedViseme(10, 10, AvatarViseme.SILENCE)
        }
    }
}
