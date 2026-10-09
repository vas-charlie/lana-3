package hr.vascharlie.lana3.avatar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AvatarVisemeSourceContractTest {
    @Test
    fun unavailableSourceCanFailTruthfullyWithoutInventingMouthShapes() {
        val source = object : AvatarVisemeSource {
            override val isAvailable = false
            override fun prepare(
                text: String,
                languageTag: String?,
                onResult: (AvatarVisemeResult) -> Unit,
            ) {
                onResult(AvatarVisemeResult.Unavailable("No playback-aligned viseme provider"))
            }
        }

        var result: AvatarVisemeResult? = null
        source.prepare("Pozdrav Charlie", "hr-HR") { result = it }

        assertFalse(source.isAvailable)
        assertEquals(
            "No playback-aligned viseme provider",
            (result as AvatarVisemeResult.Unavailable).reason,
        )
    }
}
