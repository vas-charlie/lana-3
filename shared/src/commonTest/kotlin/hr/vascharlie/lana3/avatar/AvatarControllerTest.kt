package hr.vascharlie.lana3.avatar

import kotlin.test.Test
import kotlin.test.assertEquals

class AvatarControllerTest {
    @Test
    fun userSpeechNeverMovesLanasMouth() {
        val avatar = AvatarController()
        avatar.onUserSpeechStarted()
        avatar.onLanaSpeechLevel(1f)
        assertEquals(AvatarMode.LISTENING, avatar.frame.mode)
        assertEquals(0f, avatar.frame.speechLevel)
    }

    @Test
    fun lanaSpeechDrivesMouthOnlyWhileSpeaking() {
        val avatar = AvatarController()
        avatar.onLanaSpeechStarted()
        avatar.onLanaSpeechLevel(0.7f)
        assertEquals(AvatarMode.SPEAKING, avatar.frame.mode)
        assertEquals(0.7f, avatar.frame.speechLevel)
        avatar.onLanaSpeechEnded()
        assertEquals(AvatarMode.IDLE, avatar.frame.mode)
        assertEquals(0f, avatar.frame.speechLevel)
    }
}
